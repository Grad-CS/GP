
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.HistoricalDailyUsage
import com.example.minder.domain.model.UsageBaseline
import java.util.Calendar

class CalculateHistoricalBaselineUseCase {

    //Ragahd-: Get the 14-day historical period before the app was first added.
    fun getHistoricalPeriod(addedAt: Long): Pair<Long, Long> {
        val periodEndCalendar = Calendar.getInstance().apply {
            timeInMillis = addedAt
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val periodEndExclusive = periodEndCalendar.timeInMillis

        val periodStartCalendar = periodEndCalendar.clone() as Calendar
        periodStartCalendar.add(Calendar.DAY_OF_MONTH, -14)

        return Pair(
            periodStartCalendar.timeInMillis,
            periodEndExclusive
        )
    }

    fun calculate(
        appId: Int,
        packageName: String,
        addedAt: Long,
        dailyUsages: List<HistoricalDailyUsage>,
        capturedAt: Long
    ): List<UsageBaseline>? {

        //Ragahd-: Reuse the shared historical period calculation.
        val (periodStart, periodEndExclusive) =
            getHistoricalPeriod(addedAt)

        //Ragahd-: Generate the expected dates for all 14 complete days.
        val expectedDates = mutableListOf<Long>()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = periodStart
        }

        repeat(14) {
            expectedDates.add(calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        //Ragahd-: Reject missing, duplicated, unexpected, or invalid records.
        if (
            dailyUsages.size != 14 ||
            dailyUsages.map { it.date }.toSet() != expectedDates.toSet() ||
            dailyUsages.map { it.date }.distinct().size != 14 ||
            dailyUsages.any {
                it.packageName != packageName ||
                        it.usageSeconds == null ||
                        it.usageSeconds < 0
            }
        ) {
            return null
        }

        //Ragahd-: Group valid usage samples by local weekday.
        val usageByWeekday = mutableMapOf<Int, MutableList<Int>>()

        dailyUsages.forEach { dailyUsage ->
            val dayOfWeek = Calendar.getInstance().apply {
                timeInMillis = dailyUsage.date
            }.get(Calendar.DAY_OF_WEEK)

            usageByWeekday.getOrPut(dayOfWeek) {
                mutableListOf()
            }.add(dailyUsage.usageSeconds!!)
        }

        //Ragahd-: Require exactly two valid samples for every weekday.
        if (
            (Calendar.SUNDAY..Calendar.SATURDAY).any { day ->
                usageByWeekday[day]?.size != 2
            }
        ) {
            return null
        }

        //Ragahd-: Calculate and return one fixed baseline for each weekday.
        return (Calendar.SUNDAY..Calendar.SATURDAY).map { dayOfWeek ->
            val samples = usageByWeekday.getValue(dayOfWeek)

            UsageBaseline(
                baselineId = 0,
                appId = appId,
                dayOfWeek = dayOfWeek,
                averageUsageSeconds = (
                        samples.sumOf { it.toLong() } / 2L
                        ).toInt(),
                sampleCount = 2,
                periodStart = periodStart,
                periodEnd = periodEndExclusive,
                capturedAt = capturedAt
            )
        }
    }
}
