package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.UsageSession
import com.example.minder.domain.model.UsageSummary
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.UsageSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateDailyUsageUseCaseTest {

    private lateinit var usageSessionRepository: FakeUsageSessionRepository
    private lateinit var dailyUsageRepository: FakeDailyUsageRepository
    private lateinit var useCase: UpdateDailyUsageUseCase

    @Before
    fun setup() {
        usageSessionRepository = FakeUsageSessionRepository()
        dailyUsageRepository = FakeDailyUsageRepository()

        useCase = UpdateDailyUsageUseCase(
            usageSessionRepository,
            dailyUsageRepository
        )
    }

    @Test
    fun createsDailyUsageWhenNoRecordExists() = runTest {

        usageSessionRepository.sessions = listOf(
            createSession(duration = 120),
            createSession(duration = 180)
        )

        dailyUsageRepository.usage = null

        useCase(
            appId = 1,
            date = 1000L,
            startTime = 0L,
            endTime = 2000L
        )

        assertEquals(300, dailyUsageRepository.insertedUsage?.totalUsage)
    }

    @Test
    fun calculatesTotalUsageFromMultipleSessions() = runTest {

        usageSessionRepository.sessions = listOf(
            createSession(duration = 100),
            createSession(duration = 200),
            createSession(duration = 300)
        )

        dailyUsageRepository.usage = null

        useCase(
            appId = 1,
            date = 1000L,
            startTime = 0L,
            endTime = 2000L
        )

        assertEquals(600, dailyUsageRepository.insertedUsage?.totalUsage)
    }

    @Test
    fun updatesExistingDailyUsage() = runTest {

        usageSessionRepository.sessions = listOf(
            createSession(duration = 400),
            createSession(duration = 200)
        )

        dailyUsageRepository.usage = UsageSummary(
            id = 5,
            appId = 1,
            date = 1000L,
            totalUsage = 100,
            savedTime = 50,
            challengeCount = 2
        )

        useCase(
            appId = 1,
            date = 1000L,
            startTime = 0L,
            endTime = 2000L
        )

        assertEquals(
            600,
            dailyUsageRepository.updatedUsage?.totalUsage
        )
    }

    @Test
    fun preservesSavedTimeWhenUpdating() = runTest {

        usageSessionRepository.sessions = listOf(
            createSession(duration = 300)
        )

        dailyUsageRepository.usage = UsageSummary(
            id = 5,
            appId = 1,
            date = 1000L,
            totalUsage = 100,
            savedTime = 120,
            challengeCount = 2
        )

        useCase(
            appId = 1,
            date = 1000L,
            startTime = 0L,
            endTime = 2000L
        )

        assertEquals(
            120,
            dailyUsageRepository.updatedUsage?.savedTime
        )
    }

    @Test
    fun preservesChallengeCountWhenUpdating() = runTest {

        usageSessionRepository.sessions = listOf(
            createSession(duration = 300)
        )

        dailyUsageRepository.usage = UsageSummary(
            id = 5,
            appId = 1,
            date = 1000L,
            totalUsage = 100,
            savedTime = 120,
            challengeCount = 3
        )

        useCase(
            appId = 1,
            date = 1000L,
            startTime = 0L,
            endTime = 2000L
        )

        assertEquals(
            3,
            dailyUsageRepository.updatedUsage?.challengeCount
        )
    }

    private fun createSession(duration: Int): UsageSession {
        return UsageSession(
            id = 0,
            appId = 1,
            startTime = 0L,
            endTime = duration * 1000L,
            duration = duration
        )
    }

    private class FakeUsageSessionRepository : UsageSessionRepository {

        var sessions: List<UsageSession> = emptyList()

        override suspend fun addSession(
            session: UsageSession
        ): Long = 1L

        override suspend fun getSessionsByAppId(
            appId: Int
        ): List<UsageSession> = sessions

        override suspend fun getSessionsForPeriod(
            appId: Int,
            startTime: Long,
            endTime: Long
        ): List<UsageSession> = sessions
    }

    private class FakeDailyUsageRepository : DailyUsageRepository {

        var usage: UsageSummary? = null
        var insertedUsage: UsageSummary? = null
        var updatedUsage: UsageSummary? = null

        override suspend fun addDailyUsage(
            usage: UsageSummary
        ): Long {
            insertedUsage = usage
            return 1L
        }

        override suspend fun getDailyUsageByAppId(
            appId: Int
        ): List<UsageSummary> = emptyList()

        override suspend fun getUsageForPeriod(
            appId: Int,
            startDate: Long,
            endDate: Long
        ): List<UsageSummary> = emptyList()

        override suspend fun getUsageForDate(
            appId: Int,
            date: Long
        ): UsageSummary? = usage

        override suspend fun updateDailyUsage(
            usage: UsageSummary
        ) {
            updatedUsage = usage
        }
    }
}