package com.hurkledurkle.app.data.repository

import com.hurkledurkle.app.data.local.dao.SleepEventDao
import com.hurkledurkle.app.data.local.dao.SleepSessionDao
import com.hurkledurkle.app.data.local.entity.SleepEventEntity
import com.hurkledurkle.app.data.local.entity.SleepSessionEntity
import com.hurkledurkle.app.data.model.SleepSessionWithEvents
import kotlinx.coroutines.flow.Flow

class SleepRepository(
    private val sessionDao: SleepSessionDao,
    private val eventDao: SleepEventDao
) {
    fun getAllSessions(): Flow<List<SleepSessionEntity>> =
        sessionDao.getAllSessions()

    fun getSessionsBetween(fromDate: String, toDate: String): Flow<List<SleepSessionEntity>> =
        sessionDao.getSessionsBetween(fromDate, toDate)

    fun getEventsForSession(sessionId: Long): Flow<List<SleepEventEntity>> =
        eventDao.getEventsForSession(sessionId)

    suspend fun getSessionWithEvents(sessionId: Long): SleepSessionWithEvents? {
        val session = sessionDao.getById(sessionId) ?: return null
        val events = eventDao.getEventsForSessionSync(sessionId)
        return SleepSessionWithEvents(session, events)
    }

    suspend fun insertSession(
        session: SleepSessionEntity,
        events: List<SleepEventEntity>
    ): Long {
        val sessionId = sessionDao.insert(session)
        eventDao.insertAll(events.map { it.copy(sleepSessionId = sessionId) })
        return sessionId
    }

    suspend fun updateSession(
        session: SleepSessionEntity,
        events: List<SleepEventEntity>
    ) {
        sessionDao.update(session)
        eventDao.deleteForSession(session.id)
        eventDao.insertAll(events.map { it.copy(sleepSessionId = session.id) })
    }

    suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteById(sessionId)
    }
}
