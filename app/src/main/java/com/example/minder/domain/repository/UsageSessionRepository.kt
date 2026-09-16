package com.example.minder.domain.repository

import com.example.minder.domain.model.UsageSession

interface UsageSessionRepository {

    suspend fun addSession(session: UsageSession): Long

    suspend fun getSessionsByAppId(appId: Int): List<UsageSession>

    suspend fun getSessionsForPeriod(
        appId: Int,
        startTime: Long,
        endTime: Long
    ): List<UsageSession>
}