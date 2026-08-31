package com.hurkledurkle.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_events",
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sleep_session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sleep_session_id")]
)
data class SleepEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "sleep_session_id")
    val sleepSessionId: Long,
    @ColumnInfo(name = "event_type")
    val eventType: Int,             // 0 = sleep, 1 = wake
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,           // epoch millis (UTC)
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_SLEEP = 0
        const val TYPE_WAKE = 1
    }
}
