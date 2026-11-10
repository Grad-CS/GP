
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.AppWeeklyUsageStats
import com.example.minder.domain.model.WeeklyUsageStats
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import java.util.Calendar

class GetWeeklyUsageStatsUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val dailyUsageRepository: DailyUsageRepository
) {

    suspend operator fun invoke(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ): WeeklyUsageStats {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val daysSinceSunday = calendar.get(Calendar.DAY_OF_WEEK) -
                Calendar.SUNDAY

        calendar.add(Calendar.DAY_OF_MONTH, -daysSinceSunday)

        val weekStartDate = calendar.timeInMillis

        val endCalendar = calendar.clone() as Calendar
        endCalendar.add(Calendar.DAY_OF_MONTH, 7)

        val weekEndExclusive = endCalendar.timeInMillis

        val weekEndDate = weekEndExclusive - 1L

        val elapsedDays = daysSinceSunday + 1

        //Ragahd-: Get only enabled restricted apps
        val enabledApps = restrictedAppRepository
            .getAppsByUserId(userId)
            .filter { it.isEnabled }

        //Ragahd-: Retrieve weekly usage for each enabled app
        val appStats = enabledApps.map { app ->

            val weeklyUsage = dailyUsageRepository.getUsageForPeriod(
                appId = app.id,
                startDate = weekStartDate,
                endDate = weekEndExclusive
            )

            val totalAppUsage = weeklyUsage.sumOf {
                it.totalUsage
            }

            AppWeeklyUsageStats(
                appId = app.id,
                appName = app.appName,
                packageName = app.packageName,
                totalUsageSeconds = totalAppUsage,
                averageDailyUsageSeconds = totalAppUsage / elapsedDays
            )
        }

        //Ragahd-: Calculate total weekly usage across enabled apps
        val totalUsageSeconds = appStats.sumOf {
            it.totalUsageSeconds
        }

        //Ragahd-: Calculate average daily usage for the week
        val averageDailyUsageSeconds = totalUsageSeconds / elapsedDays

        return WeeklyUsageStats(
            weekStartDate = weekStartDate,
            weekEndDate = weekEndDate,
            totalUsageSeconds = totalUsageSeconds,
            averageDailyUsageSeconds = averageDailyUsageSeconds,
            apps = appStats.sortedByDescending {
                it.totalUsageSeconds
            }
        )
    }
}
