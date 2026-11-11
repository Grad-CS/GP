
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.SavedTimeStats
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UsageBaselineRepository
import java.util.Calendar

class GetSavedTimeStatsUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val dailyUsageRepository: DailyUsageRepository,
    private val usageBaselineRepository: UsageBaselineRepository
) {

    //Ragahd-: Calculate today's and total saved time for enabled apps.
    suspend operator fun invoke(userId: Int): SavedTimeStats {

        //Ragahd-: Get the start of the current local day.
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayStart = todayCalendar.timeInMillis
        val todayDayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)

        //Ragahd-: Retrieve only enabled restricted apps.
        val enabledApps = restrictedAppRepository
            .getAppsByUserId(userId)
            .filter { it.isEnabled }

        var todaySavedTime = 0L
        var totalSavedTime = 0L

        for (app in enabledApps) {

            //Ragahd-: Retrieve the fixed weekday baselines for this app.
            val baselines = usageBaselineRepository
                .getBaselineByAppId(app.id)
                .associateBy { it.dayOfWeek }

            //Ragahd-: Skip apps without a valid weekday baseline.
            if (baselines.isEmpty()) continue

            //Ragahd-: Calculate today's saved time when today's usage exists.
            val todayBaseline = baselines[todayDayOfWeek]
            val todayUsage = dailyUsageRepository.getUsageForDate(
                appId = app.id,
                date = todayStart
            )

            if (todayBaseline != null && todayUsage != null) {
                todaySavedTime += maxOf(
                    0,
                    todayBaseline.averageUsageSeconds - todayUsage.totalUsage
                ).toLong()
            }

            //Ragahd-: Determine the first complete day after app addition.
            val firstDayCalendar = Calendar.getInstance().apply {
                timeInMillis = app.addedAt
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val addedDayStart = firstDayCalendar.timeInMillis

            //Ragahd-: Exclude the addition day unless the app was added at midnight.
            if (app.addedAt != addedDayStart) {
                firstDayCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val firstCompleteDay = firstDayCalendar.timeInMillis

            //Ragahd-: Skip historical calculation when no complete day exists.
            if (firstCompleteDay >= todayStart) continue

            //Ragahd-: Retrieve available daily usage records before today.
            val historicalUsage = dailyUsageRepository.getUsageForPeriod(
                appId = app.id,
                startDate = firstCompleteDay,
                endDate = todayStart
            ).associateBy { it.date }

            //Ragahd-: Calculate saved time for each completed day with verified usage.
            val dayCalendar = Calendar.getInstance().apply {
                timeInMillis = firstCompleteDay
            }

            while (dayCalendar.timeInMillis < todayStart) {

                val dayStart = dayCalendar.timeInMillis
                val dayOfWeek = dayCalendar.get(Calendar.DAY_OF_WEEK)

                val baseline = baselines[dayOfWeek]
                val actualUsage = historicalUsage[dayStart]

                //Ragahd-: Count only days with both baseline and actual usage records.
                if (baseline != null && actualUsage != null) {
                    totalSavedTime += maxOf(
                        0,
                        baseline.averageUsageSeconds - actualUsage.totalUsage
                    ).toLong()
                }

                dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        //Ragahd-: Return saved time in seconds without integer overflow.
        return SavedTimeStats(
            todaySavedTimeSeconds = todaySavedTime.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            totalSavedTimeSeconds = totalSavedTime.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        )
    }
}
