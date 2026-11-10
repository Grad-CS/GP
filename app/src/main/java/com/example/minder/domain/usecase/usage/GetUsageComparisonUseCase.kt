
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.UsageComparisonStats
import com.example.minder.domain.model.UsageTrend
import java.util.Calendar

class GetUsageComparisonUseCase(
    private val getWeeklyUsageStatsUseCase: GetWeeklyUsageStatsUseCase
) {

    suspend operator fun invoke(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ): UsageComparisonStats {

        //Ragahd-: Get current week's usage for elapsed days
        val currentWeek = getWeeklyUsageStatsUseCase(
            userId = userId,
            date = date
        )

        //Ragahd-: Calculate the same weekday in the previous week
        val previousWeekDate = Calendar.getInstance().apply {
            timeInMillis = date
            add(Calendar.DAY_OF_MONTH, -7)
        }.timeInMillis

        //Ragahd-: Get previous week's usage for the matching period
        val previousWeek = getWeeklyUsageStatsUseCase(
            userId = userId,
            date = previousWeekDate
        )

        //Ragahd-: Calculate usage difference
        val usageChangeSeconds =
            currentWeek.totalUsageSeconds - previousWeek.totalUsageSeconds

        //Ragahd-: Calculate percentage change safely
        val usageChangePercentage =
            if (previousWeek.totalUsageSeconds == 0) {
                0.0
            } else {
                (usageChangeSeconds.toDouble() /
                        previousWeek.totalUsageSeconds) * 100
            }

        //Ragahd-: Determine usage trend
        val trend = when {
            usageChangeSeconds < 0 -> UsageTrend.DECREASED
            usageChangeSeconds > 0 -> UsageTrend.INCREASED
            else -> UsageTrend.UNCHANGED
        }

        return UsageComparisonStats(
            currentWeekUsageSeconds = currentWeek.totalUsageSeconds,
            previousWeekUsageSeconds = previousWeek.totalUsageSeconds,
            usageChangeSeconds = usageChangeSeconds,
            usageChangePercentage = usageChangePercentage,
            trend = trend
        )
    }
}
