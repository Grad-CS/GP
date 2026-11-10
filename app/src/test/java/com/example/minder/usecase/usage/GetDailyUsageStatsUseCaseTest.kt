
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.UsageSummary
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetDailyUsageStatsUseCaseTest {

    private lateinit var restrictedAppRepository: FakeRestrictedAppRepository
    private lateinit var dailyUsageRepository: FakeDailyUsageRepository
    private lateinit var useCase: GetDailyUsageStatsUseCase

    private val userId = 1
    private val testDate = 1000L

    @Before
    fun setup() {
        restrictedAppRepository = FakeRestrictedAppRepository()
        dailyUsageRepository = FakeDailyUsageRepository()

        useCase = GetDailyUsageStatsUseCase(
            restrictedAppRepository = restrictedAppRepository,
            dailyUsageRepository = dailyUsageRepository
        )
    }

    @Test
    fun invoke_returnsTotalUsageAndAppDetails() = runTest {
        restrictedAppRepository.apps = listOf(
            createApp(id = 1, appName = "Instagram"),
            createApp(id = 2, appName = "TikTok")
        )

        dailyUsageRepository.usages = listOf(
            createUsage(appId = 1, totalUsage = 120),
            createUsage(appId = 2, totalUsage = 180)
        )

        val result = useCase(userId, testDate)

        assertEquals(testDate, result.date)
        assertEquals(300, result.totalUsageSeconds)
        assertEquals(2, result.apps.size)

        assertEquals("Instagram", result.apps[1].appName)
        assertEquals(120, result.apps[1].totalUsageSeconds)

        assertEquals("TikTok", result.apps[0].appName)
        assertEquals(180, result.apps[0].totalUsageSeconds)
    }

    @Test
    fun invoke_excludesDisabledApps() = runTest {
        restrictedAppRepository.apps = listOf(
            createApp(id = 1, appName = "Instagram", isEnabled = true),
            createApp(id = 2, appName = "TikTok", isEnabled = false)
        )

        dailyUsageRepository.usages = listOf(
            createUsage(appId = 1, totalUsage = 120),
            createUsage(appId = 2, totalUsage = 180)
        )

        val result = useCase(userId, testDate)

        assertEquals(120, result.totalUsageSeconds)
        assertEquals(1, result.apps.size)
        assertEquals(1, result.apps[0].appId)
    }

    @Test
    fun invoke_returnsZeroForAppWithoutUsageRecord() = runTest {
        restrictedAppRepository.apps = listOf(
            createApp(id = 1, appName = "Instagram")
        )

        val result = useCase(userId, testDate)

        assertEquals(0, result.totalUsageSeconds)
        assertEquals(1, result.apps.size)
        assertEquals(0, result.apps[0].totalUsageSeconds)
    }

    @Test
    fun invoke_returnsEmptyStatsWhenUserHasNoApps() = runTest {
        restrictedAppRepository.apps = emptyList()

        val result = useCase(userId, testDate)

        assertEquals(0, result.totalUsageSeconds)
        assertEquals(0, result.apps.size)
    }

    @Test
    fun invoke_returnsAppsInDescendingUsageOrder() = runTest {
        restrictedAppRepository.apps = listOf(
            createApp(id = 1, appName = "Instagram"),
            createApp(id = 2, appName = "TikTok"),
            createApp(id = 3, appName = "YouTube")
        )

        dailyUsageRepository.usages = listOf(
            createUsage(appId = 1, totalUsage = 100),
            createUsage(appId = 2, totalUsage = 300),
            createUsage(appId = 3, totalUsage = 200)
        )

        val result = useCase(userId, testDate)

        assertEquals(3, result.apps.size)
        assertEquals(2, result.apps[0].appId)
        assertEquals(3, result.apps[1].appId)
        assertEquals(1, result.apps[2].appId)
    }

    @Test
    fun invoke_requestsOnlyAppsBelongingToSpecifiedUser() = runTest {
        restrictedAppRepository.apps = listOf(
            createApp(id = 1, userId = 1, appName = "Instagram"),
            createApp(id = 2, userId = 2, appName = "TikTok")
        )

        val result = useCase(userId, testDate)

        assertEquals(1, result.apps.size)
        assertEquals(1, result.apps[0].appId)
    }

    private fun createApp(
        id: Int,
        userId: Int = this.userId,
        appName: String,
        isEnabled: Boolean = true
    ) = RestrictedApp(
        id = id,
        userId = userId,
        appName = appName,
        packageName = "com.example.$appName",
        dailyLimit = 60,
        isEnabled = isEnabled,
        addedAt = testDate
    )

    private fun createUsage(
        appId: Int,
        totalUsage: Int
    ) = UsageSummary(
        id = appId,
        appId = appId,
        date = testDate,
        totalUsage = totalUsage,
        savedTime = 0,
        challengeCount = 0
    )

    private class FakeRestrictedAppRepository : RestrictedAppRepository {

        var apps: List<RestrictedApp> = emptyList()

        override suspend fun getAppsByUserId(userId: Int): List<RestrictedApp> {
            return apps.filter { it.userId == userId }
        }

        override suspend fun getAppById(appId: Int): RestrictedApp? =
            apps.find { it.id == appId }

        override suspend fun getAppByPackageName(
            userId: Int,
            packageName: String
        ): RestrictedApp? = apps.find {
            it.userId == userId && it.packageName == packageName
        }

        override suspend fun addApp(app: RestrictedApp): Long = 1L

        override suspend fun updateApp(app: RestrictedApp) {}

        override suspend fun deleteApp(app: RestrictedApp) {}
    }

    private class FakeDailyUsageRepository : DailyUsageRepository {

        var usages: List<UsageSummary> = emptyList()

        override suspend fun addDailyUsage(usage: UsageSummary): Long = 1L

        override suspend fun getDailyUsageByAppId(
            appId: Int
        ): List<UsageSummary> = usages.filter { it.appId == appId }

        override suspend fun getUsageForPeriod(
            appId: Int,
            startDate: Long,
            endDate: Long
        ): List<UsageSummary> = usages.filter {
            it.appId == appId && it.date >= startDate && it.date < endDate
        }

        override suspend fun getUsageForDate(
            appId: Int,
            date: Long
        ): UsageSummary? = usages.find {
            it.appId == appId && it.date == date
        }

        override suspend fun updateDailyUsage(usage: UsageSummary) {}
    }
}
