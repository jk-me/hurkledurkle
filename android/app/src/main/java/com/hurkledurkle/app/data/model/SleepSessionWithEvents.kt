package com.hurkledurkle.app.data.model

import com.hurkledurkle.app.data.local.entity.SleepEventEntity
import com.hurkledurkle.app.data.local.entity.SleepSessionEntity

data class SleepSessionWithEvents(
    val session: SleepSessionEntity,
    val events: List<SleepEventEntity>
) {
    val sleepEvents: List<SleepEventEntity>
        get() = events.filter { it.eventType == SleepEventEntity.TYPE_SLEEP }
            .sortedBy { it.occurredAt }

    val wakeEvents: List<SleepEventEntity>
        get() = events.filter { it.eventType == SleepEventEntity.TYPE_WAKE }
            .sortedBy { it.occurredAt }

    /** First sleep event – used as the primary sleep time for charting. */
    val primarySleepAt: Long?
        get() = sleepEvents.firstOrNull()?.occurredAt

    /** Last wake event – used as the primary wake time for charting. */
    val primaryWakeAt: Long?
        get() = wakeEvents.lastOrNull()?.occurredAt

    /**
     * Total rest time: sum of each sleep→wake pair.
     * Pairs are zipped in chronological order, so interrupted sleep adds up correctly.
     */
    val restMinutes: Long?
        get() {
            val s = sleepEvents
            val w = wakeEvents
            if (s.isEmpty() || w.isEmpty()) return null
            return s.zip(w).sumOf { (sleep, wake) ->
                (wake.occurredAt - sleep.occurredAt) / 60_000L
            }
        }

    /** Wind-down → first sleep */
    val preSleepMinutes: Long?
        get() {
            val sleepAt = primarySleepAt ?: return null
            return (sleepAt - session.windDownAt) / 60_000L
        }

    /** Last wake → rise */
    val hurkledurkleMinutes: Long?
        get() {
            val wakeAt = primaryWakeAt ?: return null
            val riseAt = session.riseAt ?: return null
            return (riseAt - wakeAt) / 60_000L
        }
}
