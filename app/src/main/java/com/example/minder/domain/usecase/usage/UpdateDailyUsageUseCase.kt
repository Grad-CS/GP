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

        // Check if a daily usage record already exists
        val existingUsage = dailyUsageRepository.getUsageForDate(
            appId = appId,
            date = date
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