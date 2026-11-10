
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.SavedTimeStats
import com.example.minder.domain.model.UsageBaseline
import com.example.minder.domain.model.UsageSummary
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UsageBaselineRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class GetSavedTimeStatsUseCaseTest {

    private val app = RestrictedApp(
        id = 1,
        userId = 1,
        appName = "Test App",
        packageName = "com.example.testapp",
        dailyLimit = 60,
        isEnabled = true,
        addedAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -10)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    )

    private val restrictedAppRepository =
        FakeRestrictedAppRepository(listOf(app))

    private val dailyUsageRepository =
        FakeDailyUsageRepository()

    private val usageBaselineRepository =
        FakeUsageBaselineRepository()

    private val useCase = GetSavedTimeStatsUseCase(
        restrictedAppRepository = restrictedAppRepository,
        dailyUsageRepository = dailyUsageRepository,
        usageBaselineRepository = usageBaselineRepository
    )

    //Ragahd-: Create a local midnight timestamp for a specific date.
    private fun createDate(
        year: Int,
        month: Int,
        day: Int
    ): Long {
        return Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    //Ragahd-: Create the start timestamp of the current day.
    private fun todayStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    //Ragahd-: Create a baseline for a specific weekday.
    private fun createBaseline(
        appId: Int,
        dayOfWeek: Int,
        averageUsageSeconds: Int
    ) = UsageBaseline(
        baselineId = 0,
        appId = appId,
        dayOfWeek = dayOfWeek,
        averageUsageSeconds = averageUsageSeconds,
        sampleCount = 2,
        periodStart = 1L,
        periodEnd = 2L,
        capturedAt = 3L
    )

    //Ragahd-: Create a daily usage record.
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

    //Ragahd-: Add baselines for all seven weekdays.
    private fun addSevenBaselines(
        appId: Int,
        averageUsageSeconds: Int
    ) {
        usageBaselineRepository.baselines[appId] =
            (Calendar.SUNDAY..Calendar.SATURDAY).map { day ->
                createBaseline(
                    appId = appId,
                    dayOfWeek = day,
                    averageUsageSeconds = averageUsageSeconds
                )
            }
    }

    @Test
    fun invoke_withTodayUsage_calculatesTodaySavedTime() = runTest {

        val today = todayStart()

        val dayOfWeek = Calendar.getInstance().apply {
            timeInMillis = today
        }.get(Calendar.DAY_OF_WEEK)

        addSevenBaselines(app.id, 3600)

        dailyUsageRepository.records[app.id] =
            mutableListOf(
                createUsage(
                    appId = app.id,
                    date = today,
                    totalUsage = 2400
                )
            )

        val result = useCase(userId = 1)

        assertEquals(
            1200,
            result.todaySavedTimeSeconds
        )
    }

    @Test
    fun invoke_withCompletedDays_calculatesTotalAndExcludesToday() = runTest {

        val today = todayStart()

        addSevenBaselines(app.id, 3600)

        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis

        val twoDaysAgo = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_MONTH, -2)
        }.timeInMillis

        dailyUsageRepository.records[app.id] =
            mutableListOf(
                createUsage(app.id, yesterday, 2400),
                createUsage(app.id, twoDaysAgo, 1800),
                createUsage(app.id, today, 1000)
            )

        val result = useCase(userId = 1)

        assertEquals(
            3000,
            result.totalSavedTimeSeconds
        )

        assertEquals(
            2600,
            result.todaySavedTimeSeconds
        )
    }

    @Test
    fun invoke_withMissingUsageDays_calculatesAvailableDaysOnly() = runTest {

        val today = todayStart()

        addSevenBaselines(app.id, 3600)

        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis

        val threeDaysAgo = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_MONTH, -3)
        }.timeInMillis

        dailyUsageRepository.records[app.id] =
            mutableListOf(
                createUsage(app.id, yesterday, 2400),
                createUsage(app.id, threeDaysAgo, 1800)
            )

        val result = useCase(userId = 1)

        assertEquals(
            3000,
            result.totalSavedTimeSeconds
        )
    }

    @Test
    fun invoke_withDisabledApp_excludesItsSavedTime() = runTest {

        val disabledApp = app.copy(
            id = 2,
            isEnabled = false
        )

        restrictedAppRepository.apps =
            listOf(app, disabledApp)

        addSevenBaselines(app.id, 3600)
        addSevenBaselines(disabledApp.id, 5000)

        val yesterday = Calendar.getInstance().apply {
            timeInMillis = todayStart()
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis

        dailyUsageRepository.records[app.id] =
            mutableListOf(createUsage(app.id, yesterday, 2400))

        dailyUsageRepository.records[disabledApp.id] =
            mutableListOf(createUsage(disabledApp.id, yesterday, 0))

        val result = useCase(userId = 1)

        assertEquals(
            1200,
            result.totalSavedTimeSeconds
        )
    }

    @Test
    fun invoke_withoutBaseline_excludesAppFromCalculation() = runTest {

        val today = todayStart()

        dailyUsageRepository.records[app.id] =
            mutableListOf(createUsage(app.id, today, 0))

        val result = useCase(userId = 1)

        assertEquals(0, result.todaySavedTimeSeconds)
        assertEquals(0, result.totalSavedTimeSeconds)
    }

    @Test
    fun invoke_withUsageAboveBaseline_returnsZeroSavedTime() = runTest {

        val today = todayStart()

        addSevenBaselines(app.id, 3600)

        dailyUsageRepository.records[app.id] =
            mutableListOf(createUsage(app.id, today, 5000))

        val result = useCase(userId = 1)

        assertEquals(0, result.todaySavedTimeSeconds)
    }

    @Test
    fun invoke_excludesPartialAdditionDayFromTotal() = runTest {

        val today = todayStart()

        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis

        val twoDaysAgo = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_MONTH, -2)
        }.timeInMillis

        val addedAt = Calendar.getInstance().apply {
            timeInMillis = twoDaysAgo
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        restrictedAppRepository.apps =
            listOf(app.copy(addedAt = addedAt))

        addSevenBaselines(app.id, 3600)

        dailyUsageRepository.records[app.id] =
            mutableListOf(
                createUsage(app.id, twoDaysAgo, 0),
                createUsage(app.id, yesterday, 2400)
            )

        val result = useCase(userId = 1)

        assertEquals(
            1200,
            result.totalSavedTimeSeconds
        )
    }

    //Ragahd-: Fake restricted app repository for saved time tests.
    private class FakeRestrictedAppRepository(
        var apps: List<RestrictedApp>
    ) : RestrictedAppRepository {

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

        override suspend fun addApp(app: RestrictedApp): Long =
            app.id.toLong()

        override suspend fun updateApp(app: RestrictedApp) {}

        override suspend fun deleteApp(app: RestrictedApp) {}
    }

    //Ragahd-: Fake daily usage repository for saved time tests.
    private class FakeDailyUsageRepository : DailyUsageRepository {

        val records = mutableMapOf<Int, MutableList<UsageSummary>>()

        override suspend fun addDailyUsage(usage: UsageSummary): Long {
            records.getOrPut(usage.appId) { mutableListOf() }.add(usage)
            return usage.id.toLong()
        }

        override suspend fun getDailyUsageByAppId(appId: Int) =
            records[appId].orEmpty()

        override suspend fun getUsageForPeriod(
            appId: Int,
            startDate: Long,
            endDate: Long
        ) = records[appId].orEmpty().filter {
            it.date >= startDate && it.date < endDate
        }

        override suspend fun getUsageForDate(
            appId: Int,
            date: Long
        ) = records[appId].orEmpty().find {
            it.date == date
        }

        override suspend fun updateDailyUsage(usage: UsageSummary) {
            val appRecords = records.getOrPut(usage.appId) {
                mutableListOf()
            }

            val index = appRecords.indexOfFirst {
                it.date == usage.date
            }

            if (index >= 0) {
                appRecords[index] = usage
            }
        }
    }

    //Ragahd-: Fake usage baseline repository for saved time tests.
    private class FakeUsageBaselineRepository : UsageBaselineRepository {

        val baselines = mutableMapOf<Int, List<UsageBaseline>>()

        override suspend fun saveBaselines(
            records: List<UsageBaseline>
        ): Boolean {
            val appId = records.firstOrNull()?.appId ?: return false

            if (baselines[appId].orEmpty().isNotEmpty()) return false

            baselines[appId] = records
            return true
        }

        override suspend fun getBaselineForDay(
            appId: Int,
            dayOfWeek: Int
        ) = baselines[appId].orEmpty().find {
            it.dayOfWeek == dayOfWeek
        }

        override suspend fun getBaselineByAppId(
            appId: Int
        ) = baselines[appId].orEmpty()
    }
}
