
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.HistoricalDailyUsage
import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.UsageBaseline
import com.example.minder.domain.repository.HistoricalUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UsageBaselineRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SaveHistoricalBaselinesUseCaseTest {

    private val calculator = CalculateHistoricalBaselineUseCase()

    private val app = RestrictedApp(
        id = 1,
        userId = 1,
        appName = "Test App",
        packageName = "com.example.testapp",
        dailyLimit = 60,
        isEnabled = true,
        addedAt = createAddedAt()
    )

    private val restrictedAppRepository =
        FakeRestrictedAppRepository(listOf(app))

    private val historicalUsageRepository =
        FakeHistoricalUsageRepository()

    private val usageBaselineRepository =
        FakeUsageBaselineRepository()

    private val useCase = SaveHistoricalBaselinesUseCase(
        restrictedAppRepository = restrictedAppRepository,
        historicalUsageRepository = historicalUsageRepository,
        calculateHistoricalBaselineUseCase = calculator,
        usageBaselineRepository = usageBaselineRepository
    )

    @Test
    fun invoke_withValidHistoricalData_savesSevenBaselines() = runTest {
        historicalUsageRepository.usages[app.packageName] =
            createValidHistoricalUsages(app)

        val result = useCase(userId = 1)

        assertEquals(listOf(app.id), result)
        assertEquals(7, usageBaselineRepository.savedRecords[app.id]?.size)
        //Ragahd-: Verify all seven weekdays have exactly two historical samples.
        val savedBaselines = usageBaselineRepository.savedRecords[app.id].orEmpty()

        assertEquals(
            (Calendar.SUNDAY..Calendar.SATURDAY).toSet(),
            savedBaselines.map { it.dayOfWeek }.toSet()
        )

        assertTrue(savedBaselines.all { it.sampleCount == 2 })

        assertTrue(savedBaselines.all { it.averageUsageSeconds == 600 })
        assertEquals(1, historicalUsageRepository.callCount)
    }

    @Test
    fun invoke_withIncompleteHistoricalData_doesNotSaveBaseline() = runTest {
        historicalUsageRepository.usages[app.packageName] =
            createValidHistoricalUsages(app).take(13)

        val result = useCase(userId = 1)

        assertTrue(result.isEmpty())
        assertTrue(usageBaselineRepository.savedRecords.isEmpty())
    }

    @Test
    fun invoke_whenBaselineAlreadyExists_skipsHistoricalRetrieval() = runTest {
        usageBaselineRepository.existingBaselines[app.id] =
            listOf(createExistingBaseline(app.id))

        historicalUsageRepository.usages[app.packageName] =
            createValidHistoricalUsages(app)

        val result = useCase(userId = 1)

        assertTrue(result.isEmpty())
        assertEquals(0, historicalUsageRepository.callCount)
        assertTrue(usageBaselineRepository.savedRecords.isEmpty())
    }

    @Test
    fun invoke_withDisabledApp_stillSavesBaseline() = runTest {
        val disabledApp = app.copy(isEnabled = false)

        restrictedAppRepository.apps = listOf(disabledApp)

        historicalUsageRepository.usages[disabledApp.packageName] =
            createValidHistoricalUsages(disabledApp)

        val result = useCase(userId = 1)

        assertEquals(listOf(disabledApp.id), result)
        assertEquals(
            7,
            usageBaselineRepository.savedRecords[disabledApp.id]?.size
        )
    }

    @Test
    fun invoke_whenSavingFails_doesNotReturnAppAsSaved() = runTest {
        historicalUsageRepository.usages[app.packageName] =
            createValidHistoricalUsages(app)

        usageBaselineRepository.allowSave = false

        val result = useCase(userId = 1)

        assertTrue(result.isEmpty())
        assertTrue(usageBaselineRepository.savedRecords.isEmpty())
    }

    //Ragahd-: Create a fixed app addition timestamp for testing.
    private fun createAddedAt(): Long {
        return Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 10, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    //Ragahd-: Reject duplicated dates even when fourteen records are provided.
    @Test
    fun invoke_withDuplicateHistoricalDate_doesNotSaveBaseline() = runTest {
        val validUsages = createValidHistoricalUsages(app)

        historicalUsageRepository.usages[app.packageName] =
            validUsages.dropLast(1) + validUsages.first()

        val result = useCase(userId = 1)

        assertTrue(result.isEmpty())
        assertTrue(usageBaselineRepository.savedRecords.isEmpty())
    }

    //Ragahd-: Generate valid historical usage for all 14 complete days.
    private fun createValidHistoricalUsages(
        targetApp: RestrictedApp
    ): List<HistoricalDailyUsage> {
        val (periodStart, _) =
            calculator.getHistoricalPeriod(targetApp.addedAt)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = periodStart
        }

        return List(14) {
            HistoricalDailyUsage(
                packageName = targetApp.packageName,
                date = calendar.timeInMillis,
                usageSeconds = 600
            ).also {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    //Ragahd-: Create an existing baseline to verify duplicate protection.
    private fun createExistingBaseline(appId: Int) = UsageBaseline(
        baselineId = 1,
        appId = appId,
        dayOfWeek = Calendar.SUNDAY,
        averageUsageSeconds = 900,
        sampleCount = 2,
        periodStart = 1_790_000_000_000L,
        periodEnd = 1_791_209_600_000L,
        capturedAt = System.currentTimeMillis()
    )

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

        override suspend fun addApp(app: RestrictedApp): Long = app.id.toLong()

        override suspend fun updateApp(app: RestrictedApp) {}

        override suspend fun deleteApp(app: RestrictedApp) {}
    }

    private class FakeHistoricalUsageRepository :
        HistoricalUsageRepository {

        val usages = mutableMapOf<String, List<HistoricalDailyUsage>>()
        var callCount = 0

        override suspend fun getDailyUsage(
            packageName: String,
            startDate: Long,
            endDateExclusive: Long
        ): List<HistoricalDailyUsage> {
            callCount++

            return usages[packageName].orEmpty()
        }
    }

    private class FakeUsageBaselineRepository :
        UsageBaselineRepository {

        val savedRecords = mutableMapOf<Int, List<UsageBaseline>>()
        val existingBaselines = mutableMapOf<Int, List<UsageBaseline>>()

        var allowSave = true

        override suspend fun saveBaselines(
            records: List<UsageBaseline>
        ): Boolean {
            val appId = records.firstOrNull()?.appId ?: return false

            if (!allowSave || existingBaselines[appId]?.isNotEmpty() == true) {
                return false
            }

            savedRecords[appId] = records
            return true
        }

        override suspend fun getBaselineForDay(
            appId: Int,
            dayOfWeek: Int
        ): UsageBaseline? {
            return (
                    savedRecords[appId] ?: existingBaselines[appId].orEmpty()
                    ).find { it.dayOfWeek == dayOfWeek }
        }

        override suspend fun getBaselineByAppId(
            appId: Int
        ): List<UsageBaseline> {
            return savedRecords[appId]
                ?: existingBaselines[appId].orEmpty()
        }
    }
}
