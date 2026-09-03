package com.hurkledurkle.app.ui.screen.log

import com.hurkledurkle.app.data.local.entity.SleepEventEntity
import com.hurkledurkle.app.util.TimeUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class SessionTimestamps(
    val windDownAt: Long,
    val riseAt: Long?,
    val events: List<SleepEventEntity>,
    val bucketedDate: String
)

object LogSessionTimeMapper {
    fun map(
        date: LocalDate,
        windDown: TimeEntry,
        sleep: TimeEntry?,
        wake: TimeEntry?,
        rise: TimeEntry?,
        extraPairs: List<InterruptedPair>,
        timezone: String,
        resetHour: Int,
        resetMinute: Int
    ): SessionTimestamps {
        val zoneId = ZoneId.of(timezone)

        fun entryToEpoch(entry: TimeEntry, notBefore: Long = 0L): Long {
            val candidate = LocalDateTime.of(date, LocalTime.of(entry.hour, entry.minute))
                .atZone(zoneId).toInstant().toEpochMilli()
            return if (candidate < notBefore) {
                LocalDateTime.of(date.plusDays(1), LocalTime.of(entry.hour, entry.minute))
                    .atZone(zoneId).toInstant().toEpochMilli()
            } else candidate
        }

        val windDownAt = entryToEpoch(windDown)
        val riseAt = rise?.let { entryToEpoch(it, windDownAt) }
        val events = mutableListOf<SleepEventEntity>()
        var cursor = windDownAt

        fun addEvent(type: Int, entry: TimeEntry) {
            val occurredAt = entryToEpoch(entry, cursor)
            events.add(SleepEventEntity(sleepSessionId = 0, eventType = type, occurredAt = occurredAt))
            cursor = occurredAt
        }

        sleep?.let { addEvent(SleepEventEntity.TYPE_SLEEP, it) }
        wake?.let { addEvent(SleepEventEntity.TYPE_WAKE, it) }
        for (pair in extraPairs) {
            pair.sleepTime?.let { addEvent(SleepEventEntity.TYPE_SLEEP, it) }
            pair.wakeTime?.let { addEvent(SleepEventEntity.TYPE_WAKE, it) }
        }

        return SessionTimestamps(
            windDownAt = windDownAt,
            riseAt = riseAt,
            events = events,
            bucketedDate = TimeUtils.computeBucketedDate(windDownAt, timezone, resetHour, resetMinute)
        )
    }
}