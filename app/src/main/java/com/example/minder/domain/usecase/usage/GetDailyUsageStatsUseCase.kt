package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.AppDailyUsageStats
import com.example.minder.domain.model.DailyUsageStats
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository

class GetDailyUsageStatsUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val dailyUsageRepository: DailyUsageRepository
) {

    suspend operator fun invoke(
        userId: Int,
        date: Long
    ): DailyUsageStats {

        //Ragahd-: Get only enabled restricted apps
        val enabledApps = restrictedAppRepository
            .getAppsByUserId(userId)
            .filter { it.isEnabled }

        //Ragahd-: Retrieve daily usage for each enabled app
        val appStats = enabledApps.map { app ->

            val dailyUsage = dailyUsageRepository.getUsageForDate(
                appId = app.id,
                date = date
            )

            AppDailyUsageStats(
                appId = app.id,
                appName = app.appName,
                packageName = app.packageName,
                totalUsageSeconds = dailyUsage?.totalUsage ?: 0
            )
        }

        //Ragahd-: Calculate total usage across all enabled apps
        val totalUsageSeconds = appStats.sumOf {
            it.totalUsageSeconds
        }

        return DailyUsageStats(
            date = date,
            totalUsageSeconds = totalUsageSeconds,
            apps = appStats.sortedByDescending {
                it.totalUsageSeconds
            }
        )
    }
}