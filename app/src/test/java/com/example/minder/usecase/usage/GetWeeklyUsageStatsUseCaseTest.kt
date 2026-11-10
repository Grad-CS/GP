
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.UsageSummary
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GetWeeklyUsageStatsUseCaseTest {

    private lateinit var restrictedAppRepository: FakeRestrictedAppRepository
    private lateinit var dailyUsageRepository: FakeDailyUsageRepository
    private lateinit var useCase: GetWeeklyUsageStatsUseCase

    private val userId = 1

    @Before
    fun setup() {
        restrictedAppRepository = FakeRestrictedAppRepository()
        dailyUsageRepository = FakeDailyUsageRepository()

        useCase = GetWeeklyUsageStatsUseCase(
            restrictedAppRepository,
            dailyUsageRepository
        )
    }

    @Test
    fun invoke_calculatesWeeklyTotalUsage() = runTest {
        val sunday = createDate(2026, Calendar.NOVEMBER, 8)

        restrictedAppRepository.apps = listOf(
            createApp(1, "Instagram"),
            createApp(2, "TikTok")
        )

        dailyUsageRepository.usages = listOf(
            createUsage(1, sunday, 120),
            createUsage(2, sunday, 180)
        )

        val result = useCase(userId, sunday)

        assertEquals(300, result.totalUsageSeconds)
        assertEquals(2, result.apps.size)
    }

    @Test
    fun invoke_excludesDisabledApps() = runTest {
        val sunday = createDate(2026, Calendar.NOVEMBER, 8)

        restrictedAppRepository.apps = listOf(
            createApp(1, "Instagram"),
            createApp(2, "TikTok", isEnabled = false)
        )

        dailyUsageRepository.usages = listOf(
            createUsage(1, sunday, 120),
            createUsage(2, sunday, 180)
        )

        val result = useCase(userId, sunday)

        assertEquals(120, result.totalUsageSeconds)
        assertEquals(1, result.apps.size)
    }

    @Test
    fun invoke_calculatesSundayAsWeekStart() = runTest {
        val tuesday = createDate(2026, Calendar.NOVEMBER, 10)

        val result = useCase(userId, tuesday)

        val expectedSunday = createDate(2026, Calendar.NOVEMBER, 8)

        assertEquals(expectedSunday, result.weekStartDate)
    }

    @Test
    fun invoke_calculatesSaturdayAsWeekEnd() = runTest {
        val tuesday = createDate(2026, Calendar.NOVEMBER, 10)

        val result = useCase(userId, tuesday)

        val expectedSaturdayEnd = createDate(
            2026,
            Calendar.NOVEMBER,
            14,
            23,
            59,
            59
        ) + 999L

        assertEquals(expectedSaturdayEnd, result.weekEndDate)
    }

    @Test
    fun invoke_calculatesAverageUsingElapsedDays() = runTest {
        val tuesday = createDate(2026, Calendar.NOVEMBER, 10)

        restrictedAppRepository.apps = listOf(
            createApp(1, "Instagram")
        )

        dailyUsageRepository.usages = listOf(
            createUsage(1, createDate(2026, Calendar.NOVEMBER, 8), 100),
            createUsage(1, createDate(2026, Calendar.NOVEMBER, 9), 200),
            createUsage(1, createDate(2026, Calendar.NOVEMBER, 10), 300)
        )

        val result = useCase(userId, tuesday)

        assertEquals(600, result.totalUsageSeconds)
        assertEquals(200, result.averageDailyUsageSeconds)
        assertEquals(200, result.apps[0].averageDailyUsageSeconds)
    }

    @Test
    fun invoke_returnsZeroWhenNoUsageRecordsExist() = runTest {
        val sunday = createDate(2026, Calendar.NOVEMBER, 8)

        restrictedAppRepository.apps = listOf(
            createApp(1, "Instagram")
        )

        val result = useCase(userId, sunday)

        assertEquals(0, result.totalUsageSeconds)
        assertEquals(0, result.averageDailyUsageSeconds)
        assertEquals(0, result.apps[0].totalUsageSeconds)
    }

    @Test
    fun invoke_sortsAppsByDescendingUsage() = runTest {
        val sunday = createDate(2026, Calendar.NOVEMBER, 8)

        restrictedAppRepository.apps = listOf(
            createApp(1, "Instagram"),
            createApp(2, "TikTok"),
            createApp(3, "YouTube")
        )

        dailyUsageRepository.usages = listOf(
            createUsage(1, sunday, 100),
            createUsage(2, sunday, 300),
            createUsage(3, sunday, 200)
        )

        val result = useCase(userId, sunday)

        assertEquals(2, result.apps[0].appId)
        assertEquals(3, result.apps[1].appId)
        assertEquals(1, result.apps[2].appId)
    }

    @Test
    fun invoke_returnsOnlyAppsBelongingToSpecifiedUser() = runTest {
        val sunday = createDate(2026, Calendar.NOVEMBER, 8)

        restrictedAppRepository.apps = listOf(
            createApp(1, "Instagram", userId = 1),
            createApp(2, "TikTok", userId = 2)
        )

        val result = useCase(userId, sunday)

        assertEquals(1, result.apps.size)
        assertEquals(1, result.apps[0].appId)
    }

    private fun createDate(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0
    ): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun createApp(
        id: Int,
        appName: String,
        userId: Int = this.userId,
        isEnabled: Boolean = true
    ) = RestrictedApp(
        id = id,
        userId = userId,
        appName = appName,
        packageName = "com.example.$appName",
        dailyLimit = 60,
        isEnabled = isEnabled,
        addedAt = 0L
    )

    private fun createUsage(
        appId: Int,
        date: Long,
        totalUsage: Int
    ) = UsageSummary(
        id = 0,
        appId = appId,
        date = date,
        totalUsage = totalUsage,
        savedTime = 0,
        challengeCount = 0
    )

    private class FakeRestrictedAppRepository : RestrictedAppRepository {

        var apps: List<RestrictedApp> = emptyList()

        override suspend fun getAppsByUserId(userId: Int) =
            apps.filter { it.userId == userId }

        override suspend fun getAppById(appId: Int) =
            apps.find { it.id == appId }

        override suspend fun getAppByPackageName(
            userId: Int,
            packageName: String
        ) = apps.find {
            it.userId == userId && it.packageName == packageName
        }

        override suspend fun addApp(app: RestrictedApp) = 1L

        override suspend fun updateApp(app: RestrictedApp) {}

        override suspend fun deleteApp(app: RestrictedApp) {}
    }

    private class FakeDailyUsageRepository : DailyUsageRepository {

        var usages: List<UsageSummary> = emptyList()

        override suspend fun addDailyUsage(usage: UsageSummary) = 1L

        override suspend fun getDailyUsageByAppId(appId: Int) =
            usages.filter { it.appId == appId }

        override suspend fun getUsageForPeriod(
            appId: Int,
            startDate: Long,
            endDate: Long
        ) = usages.filter {
            it.appId == appId &&
                    it.date >= startDate &&
                    it.date < endDate
        }

        override suspend fun getUsageForDate(
            appId: Int,
            date: Long
        ) = usages.find {
            it.appId == appId && it.date == date
        }

        override suspend fun updateDailyUsage(usage: UsageSummary) {}
    }
}
