package com.example.minder.data.repository

import com.example.minder.data.local.dao.UsageSessionDao
import com.example.minder.data.local.entities.UsageSessionEntity
import com.example.minder.domain.model.UsageSession
import com.example.minder.domain.repository.UsageSessionRepository

class UsageSessionRepositoryImpl(
    private val usageSessionDao: UsageSessionDao
) : UsageSessionRepository {

    override suspend fun addSession(
        session: UsageSession
    ): Long {
        return usageSessionDao.insertSession(
            session.toEntity()
        )
    }

    override suspend fun getSessionsByAppId(
        appId: Int
    ): List<UsageSession> {
        return usageSessionDao
            .getSessionsByAppId(appId)
            .map { it.toDomain() }
    }

    override suspend fun getSessionsForPeriod(
        appId: Int,
        startTime: Long,
        endTime: Long
    ): List<UsageSession> {
        return usageSessionDao
            .getSessionsForPeriod(
                appId,
                startTime,
                endTime
            )
            .map { it.toDomain() }
    }
}

private fun UsageSessionEntity.toDomain(): UsageSession {
    return UsageSession(
        id = sessionId,
        appId = appId,
        startTime = startTime,
        endTime = endTime,
        duration = duration
    )
}

private fun UsageSession.toEntity(): UsageSessionEntity {
    return UsageSessionEntity(
        sessionId = id,
        appId = appId,
        startTime = startTime,
        endTime = endTime,
        duration = duration
    )
}