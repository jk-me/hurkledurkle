package com.hurkledurkle.app.ui.screen.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hurkledurkle.app.data.local.entity.SleepEventEntity
import com.hurkledurkle.app.data.local.entity.SleepSessionEntity
import com.hurkledurkle.app.data.preferences.UserPreferences
import com.hurkledurkle.app.data.repository.SleepRepository
import com.hurkledurkle.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.TimeZone

data class TimeEntry(val hour: Int, val minute: Int)

data class InterruptedPair(
    val sleepTime: TimeEntry? = null,
    val wakeTime: TimeEntry? = null
)

data class LogSessionUiState(
    val isEdit: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val windDownTime: TimeEntry? = null,
    val primarySleepTime: TimeEntry? = null,
    val primaryWakeTime: TimeEntry? = null,
    val riseTime: TimeEntry? = null,
    val extraPairs: List<InterruptedPair> = emptyList(),
    val timezone: String = TimeZone.getDefault().id,
    val isNap: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null
)

class LogSessionViewModel(
    private val repository: SleepRepository,
    private val prefs: UserPreferences,
    private val editSessionId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogSessionUiState())
    val uiState: StateFlow<LogSessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val defaultTz = prefs.timezone.first()
            _uiState.update { it.copy(timezone = defaultTz) }

            if (editSessionId != null) {
                repository.getSessionWithEvents(editSessionId)?.let { swe ->
                    val tz = swe.session.timezone
                    _uiState.update { _ ->
                        LogSessionUiState(
                            isEdit = true,
                            date = LocalDate.parse(swe.session.bucketedDate),
                            windDownTime = epochToEntry(swe.session.windDownAt, tz),
                            primarySleepTime = swe.primarySleepAt?.let { epochToEntry(it, tz) },
                            primaryWakeTime = swe.primaryWakeAt?.let { epochToEntry(it, tz) },
                            riseTime = swe.session.riseAt?.let { epochToEntry(it, tz) },
                            timezone = tz,
                            isNap = swe.session.isNap
                        )
                    }
                }
            }
        }
    }

    private fun epochToEntry(epochMs: Long, timezone: String): TimeEntry {
        val dt = TimeUtils.epochToLocalDateTime(epochMs, timezone)
        return TimeEntry(dt.hour, dt.minute)
    }

    fun setDate(d: LocalDate) = _uiState.update { it.copy(date = d) }
    fun setWindDownTime(e: TimeEntry) = _uiState.update { it.copy(windDownTime = e) }
    fun setPrimarySleepTime(e: TimeEntry?) = _uiState.update { it.copy(primarySleepTime = e) }
    fun setPrimaryWakeTime(e: TimeEntry?) = _uiState.update { it.copy(primaryWakeTime = e) }
    fun setRiseTime(e: TimeEntry?) = _uiState.update { it.copy(riseTime = e) }
    fun setTimezone(tz: String) = _uiState.update { it.copy(timezone = tz) }
    fun setIsNap(b: Boolean) = _uiState.update { it.copy(isNap = b) }

    fun addExtraPair() = _uiState.update {
        it.copy(extraPairs = it.extraPairs + InterruptedPair())
    }

    fun removeExtraPair(index: Int) = _uiState.update {
        it.copy(extraPairs = it.extraPairs.toMutableList().also { l -> l.removeAt(index) })
    }

    fun updateExtraPairSleep(index: Int, e: TimeEntry?) = _uiState.update {
        it.copy(extraPairs = it.extraPairs.toMutableList().also { l ->
            l[index] = l[index].copy(sleepTime = e)
        })
    }

    fun updateExtraPairWake(index: Int, e: TimeEntry?) = _uiState.update {
        it.copy(extraPairs = it.extraPairs.toMutableList().also { l ->
            l[index] = l[index].copy(wakeTime = e)
        })
    }

    fun save() {
        val state = _uiState.value
        val windDown = state.windDownTime
        if (windDown == null) {
            _uiState.update { it.copy(error = "Wind-down time is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val resetH = prefs.bedtimeResetHour.first()
                val resetM = prefs.bedtimeResetMinute.first()
                val tz = state.timezone
                val zoneId = ZoneId.of(tz)
                val baseDate = state.date

                /** Converts a TimeEntry on baseDate to epoch millis, bumping to the next day if
                 *  the result would land before [notBefore]. */
                fun entryToEpoch(entry: TimeEntry, notBefore: Long = 0L): Long {
                    val candidate = LocalDateTime.of(baseDate, LocalTime.of(entry.hour, entry.minute))
                        .atZone(zoneId).toInstant().toEpochMilli()
                    return if (candidate < notBefore) {
                        LocalDateTime.of(baseDate.plusDays(1), LocalTime.of(entry.hour, entry.minute))
                            .atZone(zoneId).toInstant().toEpochMilli()
                    } else candidate
                }

                val windDownEpoch = entryToEpoch(windDown)
                val riseEpoch = state.riseTime?.let { entryToEpoch(it, windDownEpoch) }
                val bucketedDate = TimeUtils.computeBucketedDate(windDownEpoch, tz, resetH, resetM)

                val sessionEntity = SleepSessionEntity(
                    id = editSessionId ?: 0,
                    bucketedDate = bucketedDate,
                    windDownAt = windDownEpoch,
                    riseAt = riseEpoch,
                    timezone = tz,
                    isNap = state.isNap
                )

                // Build chronologically ordered event list
                val events = mutableListOf<SleepEventEntity>()
                var cursor = windDownEpoch

                fun addEvent(type: Int, entry: TimeEntry) {
                    val epoch = entryToEpoch(entry, cursor)
                    events.add(SleepEventEntity(sleepSessionId = 0, eventType = type, occurredAt = epoch))
                    cursor = epoch
                }

                state.primarySleepTime?.let { addEvent(SleepEventEntity.TYPE_SLEEP, it) }
                state.primaryWakeTime?.let { addEvent(SleepEventEntity.TYPE_WAKE, it) }
                for (pair in state.extraPairs) {
                    pair.sleepTime?.let { addEvent(SleepEventEntity.TYPE_SLEEP, it) }
                    pair.wakeTime?.let { addEvent(SleepEventEntity.TYPE_WAKE, it) }
                }

                if (state.isEdit) {
                    repository.updateSession(sessionEntity, events)
                } else {
                    repository.insertSession(sessionEntity, events)
                }

                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save.") }
            }
        }
    }

    class Factory(
        private val repository: SleepRepository,
        private val prefs: UserPreferences,
        private val editSessionId: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LogSessionViewModel(repository, prefs, editSessionId) as T
    }
}
