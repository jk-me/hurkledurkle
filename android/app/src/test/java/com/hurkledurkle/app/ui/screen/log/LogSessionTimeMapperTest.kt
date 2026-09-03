package com.hurkledurkle.app.ui.screen.log

import com.hurkledurkle.app.data.local.entity.SleepEventEntity
import com.hurkledurkle.app.util.TimeUtils
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class LogSessionTimeMapperTest {
    private val timezone = "America/Toronto"

    @Test
    fun mapsEveryRecordToTheCorrectDateAcrossMidnight() {
        val timestamps = LogSessionTimeMapper.map(
            date = LocalDate.of(2026, 9, 1),
            windDown = TimeEntry(23, 0),
            sleep = TimeEntry(1, 0),
            wake = TimeEntry(7, 0),
            rise = TimeEntry(7, 30),
            extraPairs = listOf(InterruptedPair(TimeEntry(8, 0), TimeEntry(8, 30))),
            timezone = timezone,
            resetHour = 12,
            resetMinute = 0
        )

        assertLocalDateTime(timestamps.windDownAt, "2026-09-01T23:00")
        assertLocalDateTime(timestamps.events[0].occurredAt, "2026-09-02T01:00")
        assertLocalDateTime(timestamps.events[1].occurredAt, "2026-09-02T07:00")
        assertLocalDateTime(timestamps.events[2].occurredAt, "2026-09-02T08:00")
        assertLocalDateTime(timestamps.events[3].occurredAt, "2026-09-02T08:30")
        assertLocalDateTime(timestamps.riseAt!!, "2026-09-02T07:30")
        assertEquals("2026-09-01", timestamps.bucketedDate)
        assertEquals(SleepEventEntity.TYPE_SLEEP, timestamps.events[0].eventType)
        assertEquals(SleepEventEntity.TYPE_WAKE, timestamps.events[1].eventType)
    }

    @Test
    fun bucketsWindDownBeforeResetToPreviousDate() {
        val timestamps = LogSessionTimeMapper.map(
            date = LocalDate.of(2026, 9, 2),
            windDown = TimeEntry(2, 0),
            sleep = null,
            wake = null,
            rise = null,
            extraPairs = emptyList(),
            timezone = timezone,
            resetHour = 12,
            resetMinute = 0
        )

        assertLocalDateTime(timestamps.windDownAt, "2026-09-02T02:00")
        assertEquals("2026-09-01", timestamps.bucketedDate)
    }

    private fun assertLocalDateTime(epochMs: Long, expected: String) {
        assertEquals(expected, TimeUtils.epochToLocalDateTime(epochMs, timezone).toString())
    }
}