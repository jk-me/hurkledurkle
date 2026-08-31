package com.hurkledurkle.app.data.model

/**
 * Aggregated metrics for a single bucketed day, used to populate the charts.
 * All "minutes" fields for time-of-day are minutes from midnight in local time.
 * Duration fields are absolute minutes.
 */
data class DayMetrics(
    val date: String,               // "YYYY-MM-DD"
    val windDownMinutes: Int?,      // time-of-day, minutes from midnight
    val sleepMinutes: Int?,
    val wakeMinutes: Int?,
    val riseMinutes: Int?,
    val restMinutes: Long?,         // duration
    val preSleepMinutes: Long?,     // duration
    val hurkledurkleMinutes: Long?, // duration
    val isNap: Boolean = false
)
