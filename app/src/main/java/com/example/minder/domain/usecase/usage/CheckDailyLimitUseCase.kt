package com.example.minder.domain.usecase.usage

import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository

class CheckDailyLimitUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val dailyUsageRepository: DailyUsageRepository
) {

    suspend operator fun invoke(
        appId: Int,
        date: Long
    ): Boolean {

        val app = restrictedAppRepository.getAppById(appId)
            ?: return false

        val usage = dailyUsageRepository.getUsageForDate(
            appId = appId,
            date = date
        )

        val totalUsageSeconds = usage?.totalUsage ?: 0
        val dailyLimitSeconds = app.dailyLimit * 60

        return totalUsageSeconds >= dailyLimitSeconds
    }
}