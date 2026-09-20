package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.UsageSummary
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckDailyLimitUseCaseTest {

    private lateinit var restrictedAppRepository: FakeRestrictedAppRepository
    private lateinit var dailyUsageRepository: FakeDailyUsageRepository
    private lateinit var useCase: CheckDailyLimitUseCase

    @Before
    fun setup() {
        restrictedAppRepository = FakeRestrictedAppRepository()
        dailyUsageRepository = FakeDailyUsageRepository()

        useCase = CheckDailyLimitUseCase(
            restrictedAppRepository,
            dailyUsageRepository
        )
    }

    @Test
    fun usageBelowLimitReturnsFalse() = runTest {
        // Daily limit = 60 minutes
        // Usage = 3500 seconds = 58 minutes 20 seconds
        restrictedAppRepository.app = createApp(60)
        dailyUsageRepository.usage = createUsage(3500)

        val result = useCase(1, 1000L)

        assertFalse(result)
    }

    @Test
    fun usageEqualsLimitReturnsTrue() = runTest {
        // Daily limit = 60 minutes
        // Usage = 3600 seconds = 60 minutes
        restrictedAppRepository.app = createApp(60)
        dailyUsageRepository.usage = createUsage(3600)

        val result = useCase(1, 1000L)

        assertTrue(result)
    }

    @Test
    fun usageExceedsLimitReturnsTrue() = runTest {
        // Daily limit = 60 minutes
        // Usage = 3700 seconds = 61 minutes 40 seconds
        restrictedAppRepository.app = createApp(60)
        dailyUsageRepository.usage = createUsage(3700)

        val result = useCase(1, 1000L)

        assertTrue(result)
    }

    @Test
    fun noUsageReturnsFalse() = runTest {
        restrictedAppRepository.app = createApp(60)
        dailyUsageRepository.usage = null

        val result = useCase(1, 1000L)

        assertFalse(result)
    }

    @Test
    fun appNotFoundReturnsFalse() = runTest {
        restrictedAppRepository.app = null

        val result = useCase(1, 1000L)

        assertFalse(result)
    }

    private fun createApp(dailyLimit: Int): RestrictedApp {
        return RestrictedApp(
            id = 1,
            userId = 1,
            appName = "TikTok",
            packageName = "com.tiktok",
            dailyLimit = dailyLimit,
            isEnabled = true,
            addedAt = 0L
        )
    }

    private fun createUsage(totalUsage: Int): UsageSummary {
        return UsageSummary(
            id = 1,
            appId = 1,
            date = 1000L,
            totalUsage = totalUsage,
            savedTime = 0,
            challengeCount = 0
        )
    }

    private class FakeRestrictedAppRepository : RestrictedAppRepository {

        var app: RestrictedApp? = null

        override suspend fun getAppsByUserId(
            userId: Int
        ): List<RestrictedApp> = emptyList()

        override suspend fun getAppById(
            appId: Int
        ): RestrictedApp? = app

        override suspend fun getAppByPackageName(
            userId: Int,
            packageName: String
        ): RestrictedApp? = null

        override suspend fun addApp(
            app: RestrictedApp
        ): Long = 1L

        override suspend fun updateApp(
            app: RestrictedApp
        ) {
        }

        override suspend fun deleteApp(
            app: RestrictedApp
        ) {
        }
    }

    private class FakeDailyUsageRepository : DailyUsageRepository {

        var usage: UsageSummary? = null

        override suspend fun addDailyUsage(
            usage: UsageSummary
        ): Long = 1L

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
        }
    }
}