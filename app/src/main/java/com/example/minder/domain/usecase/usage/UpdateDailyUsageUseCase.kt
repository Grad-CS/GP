package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.UsageSummary
import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.UsageSessionRepository

class UpdateDailyUsageUseCase(
    private val usageSessionRepository: UsageSessionRepository,
    private val dailyUsageRepository: DailyUsageRepository
) {

    suspend operator fun invoke(
        appId: Int,
        date: Long,
        startTime: Long,
        endTime: Long
    ) {

        // Get all usage sessions for the selected day
        val sessions = usageSessionRepository.getSessionsForPeriod(
            appId = appId,
            startTime = startTime,
            endTime = endTime
        )

        // Total usage is stored in seconds
        val totalUsageSeconds = sessions.sumOf { it.duration }

        //Ragahd-: Find an existing usage record within the selected day
        val existingUsage = dailyUsageRepository.getUsageForDate(
            appId = appId,
            dayStart = startTime,
            nextDayStart = endTime
        )

        if (existingUsage == null) {

            // No record exists → create a new one
            val dailyUsage = UsageSummary(
                id = 0,
                appId = appId,
                date = date,
                totalUsage = totalUsageSeconds,
                savedTime = 0,
                challengeCount = 0
            )

            dailyUsageRepository.addDailyUsage(dailyUsage)

        } else {

            // Record exists → update only the usage
            val updatedUsage = existingUsage.copy(
                totalUsage = totalUsageSeconds
            )

            dailyUsageRepository.updateDailyUsage(updatedUsage)
        }
    }
}