package com.hurkledurkle.app.util

import com.hurkledurkle.app.data.local.entity.SleepEventEntity
import com.hurkledurkle.app.data.model.SleepSessionWithEvents
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /**
     * Produces a two-section CSV: one row per session, then one row per event.
     * All times are in the session's recorded timezone.
     */
    fun buildCsv(sessions: List<SleepSessionWithEvents>): String {
        val sorted = sessions.sortedWith(
            compareBy({ it.session.bucketedDate }, { it.session.windDownAt })
        )
        return buildString {
            appendSessionSection(sorted)
            append("\n")
            appendEventSection(sorted)
        }
    }

    private fun StringBuilder.appendSessionSection(sessions: List<SleepSessionWithEvents>) {
        appendLine("date,timezone,is_nap,wind_down_at,sleep_at,wake_at,rise_at,rest_minutes,pre_sleep_minutes,hurkle_durkle_minutes")
        for (s in sessions) {
            val tz = s.session.timezone
            appendLine(
                row(
                    s.session.bucketedDate,
                    tz,
                    s.session.isNap.toString(),
                    fmt(s.session.windDownAt, tz),
                    s.primarySleepAt?.let { fmt(it, tz) } ?: "",
                    s.primaryWakeAt?.let { fmt(it, tz) } ?: "",
                    s.session.riseAt?.let { fmt(it, tz) } ?: "",
                    s.restMinutes?.toString() ?: "",
                    s.preSleepMinutes?.toString() ?: "",
                    s.hurkledurkleMinutes?.toString() ?: ""
                )
            )
        }
    }

    private fun StringBuilder.appendEventSection(sessions: List<SleepSessionWithEvents>) {
        appendLine("id,date,timezone,event_type,occurred_at")
        for (s in sessions) {
            val tz = s.session.timezone
            for (event in s.events.sortedBy { it.occurredAt }) {
                appendLine(
                    row(
                        s.session.id.toString(),
                        s.session.bucketedDate,
                        tz,
                        if (event.eventType == SleepEventEntity.TYPE_SLEEP) "sleep" else "wake",
                        fmt(event.occurredAt, tz)
                    )
                )
            }
        }
    }

    private fun fmt(epochMs: Long, timezone: String): String =
        TS.format(TimeUtils.epochToLocalDateTime(epochMs, timezone))

    private fun row(vararg fields: String): String =
        fields.joinToString(",") { it.csvEscape() }

    private fun String.csvEscape(): String =
        if (contains(',') || contains('"') || contains('\n')) "\"${replace("\"", "\"\"")}\"" else this
}
