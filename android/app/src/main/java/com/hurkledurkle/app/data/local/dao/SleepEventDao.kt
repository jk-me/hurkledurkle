package com.hurkledurkle.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hurkledurkle.app.data.local.entity.SleepEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepEventDao {

    @Query("SELECT * FROM sleep_events WHERE sleep_session_id = :sessionId ORDER BY occurred_at ASC")
    fun getEventsForSession(sessionId: Long): Flow<List<SleepEventEntity>>

    @Query("SELECT * FROM sleep_events WHERE sleep_session_id = :sessionId ORDER BY occurred_at ASC")
    suspend fun getEventsForSessionSync(sessionId: Long): List<SleepEventEntity>

    @Insert
    suspend fun insert(event: SleepEventEntity): Long

    @Insert
    suspend fun insertAll(events: List<SleepEventEntity>)

    @Update
    suspend fun update(event: SleepEventEntity)

    @Delete
    suspend fun delete(event: SleepEventEntity)

    @Query("DELETE FROM sleep_events WHERE sleep_session_id = :sessionId")
    suspend fun deleteForSession(sessionId: Long)
}
