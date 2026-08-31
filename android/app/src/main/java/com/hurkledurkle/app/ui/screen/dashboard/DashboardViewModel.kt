package com.hurkledurkle.app.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hurkledurkle.app.data.model.DayMetrics
import com.hurkledurkle.app.data.model.SleepSessionWithEvents
import com.hurkledurkle.app.data.preferences.UserPreferences
import com.hurkledurkle.app.data.repository.SleepRepository
import com.hurkledurkle.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val sessions: List<SleepSessionWithEvents> = emptyList(),
    val dayMetrics: List<DayMetrics> = emptyList(),
    val graphDays: Int = 30,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val repository: SleepRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.graphDays,
                prefs.timezone,
                prefs.bedtimeResetHour,
                prefs.bedtimeResetMinute
            ) { days, tz, resetH, resetM ->
                Quadruple(days, tz, resetH, resetM)
            }.collectLatest { (days, tz, _, _) ->
                val (fromDate, toDate) = TimeUtils.getDateRange(days)
                repository.getSessionsBetween(fromDate, toDate).collectLatest { sessions ->
                    val withEvents = sessions.map { session ->
                        val events = repository.getSessionWithEvents(session.id)?.events
                            ?: emptyList()
                        SleepSessionWithEvents(session, events)
                    }
                    _uiState.update {
                        it.copy(
                            sessions = withEvents.sortedByDescending { s -> s.session.windDownAt },
                            dayMetrics = buildDayMetrics(withEvents, fromDate, toDate, tz),
                            graphDays = days,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun buildDayMetrics(
        sessions: List<SleepSessionWithEvents>,
        fromDate: String,
        toDate: String,
        timezone: String
    ): List<DayMetrics> {
        val byDate = sessions.groupBy { it.session.bucketedDate }
        val result = mutableListOf<DayMetrics>()
        var date = LocalDate.parse(fromDate)
        val end = LocalDate.parse(toDate)
        while (!date.isAfter(end)) {
            val key = TimeUtils.DATE_KEY_FORMATTER.format(date)
            val daySessions = byDate[key] ?: emptyList()
            if (daySessions.isEmpty()) {
                result.add(DayMetrics(key, null, null, null, null, null, null, null))
            } else {
                // Primary = non-nap session; fall back to first
                val primary = daySessions.firstOrNull { !it.session.isNap } ?: daySessions.first()
                result.add(
                    DayMetrics(
                        date = key,
                        windDownMinutes = TimeUtils.minutesFromMidnight(primary.session.windDownAt, timezone),
                        sleepMinutes = primary.primarySleepAt?.let { TimeUtils.minutesFromMidnight(it, timezone) },
                        wakeMinutes = primary.primaryWakeAt?.let { TimeUtils.minutesFromMidnight(it, timezone) },
                        riseMinutes = primary.session.riseAt?.let { TimeUtils.minutesFromMidnight(it, timezone) },
                        restMinutes = primary.restMinutes,
                        preSleepMinutes = primary.preSleepMinutes,
                        hurkledurkleMinutes = primary.hurkledurkleMinutes,
                        isNap = primary.session.isNap
                    )
                )
            }
            date = date.plusDays(1)
        }
        return result
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch { repository.deleteSession(sessionId) }
    }

    class Factory(
        private val repository: SleepRepository,
        private val prefs: UserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(repository, prefs) as T
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
