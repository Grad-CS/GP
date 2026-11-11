
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.HistoricalDailyUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class CalculateHistoricalBaselineUseCaseTest {

    private val useCase = CalculateHistoricalBaselineUseCase()

    private val appId = 5
    private val packageName = "com.example.testapp"
    private val capturedAt = 1794305000000L

    //Ragahd-: Create a fixed application addition timestamp for reproducible tests.
    private fun createAddedAt(): Long {
        return Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 10, 15, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    //Ragahd-: Generate 14 historical daily records with configurable usage values.
    private fun createDailyUsages(
        addedAt: Long,
        usageProvider: (Int) -> Int? = {
            if (it < 7) 100 else 200
        }
    ): List<HistoricalDailyUsage> {

        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = addedAt
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startCalendar = endCalendar.clone() as Calendar
        startCalendar.add(Calendar.DAY_OF_MONTH, -14)

        return (0 until 14).map { index ->
            val dateCalendar = startCalendar.clone() as Calendar
            dateCalendar.add(Calendar.DAY_OF_MONTH, index)

            HistoricalDailyUsage(
                packageName = packageName,
                date = dateCalendar.timeInMillis,
                usageSeconds = usageProvider(index)
            )
        }
    }

    @Test
    fun calculate_withCompleteHistoricalData_returnsSevenBaselines() {
        val addedAt = createAddedAt()
        val dailyUsages = createDailyUsages(addedAt)

        val result = useCase.calculate(
            appId = appId,
            packageName = packageName,
            addedAt = addedAt,
            dailyUsages = dailyUsages,
            capturedAt = capturedAt
        )

        assertNotNull(result)
        assertEquals(7, result!!.size)

        //Ragahd-: Verify that each weekday has two samples and the expected average.
        result.forEach { baseline ->
            assertEquals(2, baseline.sampleCount)
            assertEquals(150, baseline.averageUsageSeconds)
            assertEquals(appId, baseline.appId)
            assertEquals(0, baseline.baselineId)
            assertEquals(capturedAt, baseline.capturedAt)
        }
    }

    @Test
    fun calculate_withMissingUsageRecord_returnsNull() {
        val addedAt = createAddedAt()
        val dailyUsages = createDailyUsages(addedAt) {
            if (it == 3) null else 100
        }

        val result = useCase.calculate(
            appId,
            packageName,
            addedAt,
            dailyUsages,
            capturedAt
        )

        assertNull(result)
    }

    @Test
    fun calculate_withZeroUsageRecord_acceptsBaseline() {
        val addedAt = createAddedAt()
        val dailyUsages = createDailyUsages(addedAt) { 0 }

        val result = useCase.calculate(
            appId,
            packageName,
            addedAt,
            dailyUsages,
            capturedAt
        )

        assertNotNull(result)
        assertEquals(7, result!!.size)
        result.forEach {
            assertEquals(0, it.averageUsageSeconds)
        }
    }

    @Test
    fun calculate_withDuplicatedDate_returnsNull() {
        val addedAt = createAddedAt()
        val dailyUsages = createDailyUsages(addedAt).toMutableList()

        //Ragahd-: Replace the last date with a duplicate historical date.
        dailyUsages[13] = dailyUsages[12].copy()

        val result = useCase.calculate(
            appId,
            packageName,
            addedAt,
            dailyUsages,
            capturedAt
        )

        assertNull(result)
    }

    @Test
    fun calculate_withWrongPackageName_returnsNull() {
        val addedAt = createAddedAt()
        val dailyUsages = createDailyUsages(addedAt).toMutableList()

        dailyUsages[0] = dailyUsages[0].copy(
            packageName = "com.example.otherapp"
        )

        val result = useCase.calculate(
            appId,
            packageName,
            addedAt,
            dailyUsages,
            capturedAt
        )

        assertNull(result)
    }

    @Test
    fun calculate_withDateOutsideHistoricalPeriod_returnsNull() {
        val addedAt = createAddedAt()
        val dailyUsages = createDailyUsages(addedAt).toMutableList()

        //Ragahd-: Use the application addition date, which is outside the baseline period.
        dailyUsages[13] = dailyUsages[13].copy(
            date = addedAt
        )

        val result = useCase.calculate(
            appId,
            packageName,
            addedAt,
            dailyUsages,
            capturedAt
        )

        assertNull(result)
    }

    @Test
    fun calculate_withIncompleteNumberOfRecords_returnsNull() {
        val addedAt = createAddedAt()
        val dailyUsages = createDailyUsages(addedAt).dropLast(1)

        val result = useCase.calculate(
            appId,
            packageName,
            addedAt,
            dailyUsages,
            capturedAt
        )

        assertNull(result)
    }
}
