package com.hurkledurkle.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "bucketed_date")
    val bucketedDate: String,       // ISO date "YYYY-MM-DD"
    @ColumnInfo(name = "wind_down_at")
    val windDownAt: Long,           // epoch millis (UTC)
    @ColumnInfo(name = "rise_at")
    val riseAt: Long?,              // epoch millis (UTC), nullable until set
    val timezone: String,           // IANA tz name e.g. "America/Toronto"
    @ColumnInfo(name = "is_nap")
    val isNap: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
