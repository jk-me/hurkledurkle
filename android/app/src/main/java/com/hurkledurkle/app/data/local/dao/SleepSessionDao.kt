package com.hurkledurkle.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hurkledurkle.app.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {

    @Query("SELECT * FROM sleep_sessions ORDER BY wind_down_at DESC")
    fun getAllSessions(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE bucketed_date BETWEEN :fromDate AND :toDate ORDER BY wind_down_at ASC")
    fun getSessionsBetween(fromDate: String, toDate: String): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getById(id: Long): SleepSessionEntity?

    @Insert
    suspend fun insert(session: SleepSessionEntity): Long

    @Update
    suspend fun update(session: SleepSessionEntity)

    @Delete
    suspend fun delete(session: SleepSessionEntity)

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
