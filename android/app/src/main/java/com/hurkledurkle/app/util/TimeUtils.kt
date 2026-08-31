package com.hurkledurkle.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {

    val DATE_KEY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val DATE_DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    fun epochToLocalDateTime(epochMs: Long, timezone: String): LocalDateTime =
        Instant.ofEpochMilli(epochMs).atZone(ZoneId.of(timezone)).toLocalDateTime()

    fun epochToLocalTime(epochMs: Long, timezone: String): LocalTime =
        epochToLocalDateTime(epochMs, timezone).toLocalTime()

    fun localDateTimeToEpoch(dateTime: LocalDateTime, timezone: String): Long =
        dateTime.atZone(ZoneId.of(timezone)).toInstant().toEpochMilli()

    fun formatTime(epochMs: Long, timezone: String): String =
        TIME_FORMATTER.format(epochToLocalTime(epochMs, timezone))

    fun formatDate(dateStr: String): String =
        DATE_DISPLAY_FORMATTER.format(LocalDate.parse(dateStr))

    /**
     * Returns minutes from midnight for a given epoch time in the given timezone.
     * Used as the y-axis value in the sleep times chart.
     */
    fun minutesFromMidnight(epochMs: Long, timezone: String): Int {
        val t = epochToLocalTime(epochMs, timezone)
        return t.hour * 60 + t.minute
    }

    /**
     * Computes the bucketed date for a session.
     * If wind-down time is before the reset time, the session belongs to the previous calendar day.
     * Example: reset = noon, wind-down at 2 AM → bucketed to the day before.
     */
    fun computeBucketedDate(
        windDownAtMs: Long,
        timezone: String,
        resetHour: Int,
        resetMinute: Int
    ): String {
        val zoneId = ZoneId.of(timezone)
        val windDown = Instant.ofEpochMilli(windDownAtMs).atZone(zoneId)
        val resetTime = LocalTime.of(resetHour, resetMinute)
        val date = if (windDown.toLocalTime().isBefore(resetTime)) {
            windDown.toLocalDate().minusDays(1)
        } else {
            windDown.toLocalDate()
        }
        return DATE_KEY_FORMATTER.format(date)
    }

    fun formatDuration(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    /** Returns the ISO date string for today minus (days - 1) days, paired with today. */
    fun getDateRange(days: Int): Pair<String, String> {
        val today = LocalDate.now()
        val from = today.minusDays(days.toLong() - 1)
        return DATE_KEY_FORMATTER.format(from) to DATE_KEY_FORMATTER.format(today)
    }
}
