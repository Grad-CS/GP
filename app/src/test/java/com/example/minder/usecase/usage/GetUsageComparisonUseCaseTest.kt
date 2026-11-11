
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.UsageSummary
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.model.UsageTrend
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GetUsageComparisonUseCaseTest {

    private lateinit var restrictedAppRepository: FakeRestrictedAppRepository
    private lateinit var dailyUsageRepository: FakeDailyUsageRepository
    private lateinit var useCase: GetUsageComparisonUseCase

    private val userId = 1

    @Before
    fun setup() {
        restrictedAppRepository = FakeRestrictedAppRepository()
        dailyUsageRepository = FakeDailyUsageRepository()

        val weeklyUsageUseCase = GetWeeklyUsageStatsUseCase(
            restrictedAppRepository,
            dailyUsageRepository
        )

        useCase = GetUsageComparisonUseCase(weeklyUsageUseCase)
    }

    @Test
    fun invoke_calculatesUsageDecrease() = runTest {
        prepareApp()

        addPreviousWeekUsage(1000)
        addCurrentWeekUsage(800)

        val result = useCase(userId, testDate())

        assertEquals(800, result.currentWeekUsageSeconds)
        assertEquals(1000, result.previousWeekUsageSeconds)
        assertEquals(-200, result.usageChangeSeconds)
        assertEquals(-20.0, result.usageChangePercentage, 0.01)
        assertEquals(UsageTrend.DECREASED, result.trend)
    }

    @Test
    fun invoke_calculatesUsageIncrease() = runTest {
        prepareApp()

        addPreviousWeekUsage(1000)
        addCurrentWeekUsage(1200)

        val result = useCase(userId, testDate())

        assertEquals(200, result.usageChangeSeconds)
        assertEquals(20.0, result.usageChangePercentage, 0.01)
        assertEquals(UsageTrend.INCREASED, result.trend)
    }

    @Test
    fun invoke_returnsUnchangedWhenUsageIsEqual() = runTest {
        prepareApp()

        addPreviousWeekUsage(1000)
        addCurrentWeekUsage(1000)

        val result = useCase(userId, testDate())

        assertEquals(0, result.usageChangeSeconds)
        assertEquals(0.0, result.usageChangePercentage, 0.01)
        assertEquals(UsageTrend.UNCHANGED, result.trend)
    }

    @Test
    fun invoke_handlesZeroUsageInBothPeriods() = runTest {
        prepareApp()

        val result = useCase(userId, testDate())

        assertEquals(0, result.usageChangeSeconds)
        assertEquals(0.0, result.usageChangePercentage, 0.01)
        assertEquals(UsageTrend.UNCHANGED, result.trend)
    }

    @Test
    fun invoke_handlesZeroPreviousUsage() = runTest {
        prepareApp()

        addCurrentWeekUsage(500)

        val result = useCase(userId, testDate())

        assertEquals(500, result.usageChangeSeconds)
        assertEquals(0.0, result.usageChangePercentage, 0.01)
        assertEquals(UsageTrend.INCREASED, result.trend)
    }

    @Test
    fun invoke_comparesMatchingWeekdaysFromPreviousWeek() = runTest {
        prepareApp()

        addPreviousWeekUsage(1000)
        addCurrentWeekUsage(800)

        val result = useCase(userId, testDate())

        assertEquals(800, result.currentWeekUsageSeconds)
        assertEquals(1000, result.previousWeekUsageSeconds)
    }

    //Ragahd-: Create a fixed Tuesday reference date
    private fun testDate(): Long {
        return createDate(2026, Calendar.NOVEMBER, 10)
    }

    //Ragahd-: Prepare one enabled restricted app
    private fun prepareApp() {
        restrictedAppRepository.apps = listOf(
            createApp(1, "Instagram")
        )
    }

    //Ragahd-: Add usage records for Sunday through Tuesday of the current week
    private fun addCurrentWeekUsage(totalUsage: Int) {
        val dailyUsage = totalUsage / 3
        val remainder = totalUsage % 3

        for (day in 8..10) {
            val usage = dailyUsage + if (day == 8) remainder else 0

            dailyUsageRepository.usages += createUsage(
                appId = 1,
                date = createDate(2026, Calendar.NOVEMBER, day),
                totalUsage = usage
            )
        }
    }

    //Ragahd-: Add usage records for the matching period in the previous week
    private fun addPreviousWeekUsage(totalUsage: Int) {
        val dailyUsage = totalUsage / 3
        val remainder = totalUsage % 3

        for (day in 1..3) {
            val usage = dailyUsage + if (day == 1) remainder else 0

            dailyUsageRepository.usages += createUsage(
                appId = 1,
                date = createDate(2026, Calendar.NOVEMBER, day),
                totalUsage = usage
            )
        }
    }

    private fun createDate(
        year: Int,
        month: Int,
        day: Int
    ): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun createApp(
        id: Int,
        appName: String
    ) = RestrictedApp(
        id = id,
        userId = userId,
        appName = appName,
        packageName = "com.example.$appName",
        dailyLimit = 60,
        isEnabled = true,
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
