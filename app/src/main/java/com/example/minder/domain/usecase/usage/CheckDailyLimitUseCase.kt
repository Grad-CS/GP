package com.example.minder.domain.usecase.usage

import com.example.minder.domain.repository.DailyUsageRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import java.util.Calendar

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

        //Ragahd-: Calculate the local day boundaries
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayStart = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val nextDayStart = calendar.timeInMillis

        //Ragahd-: Retrieve usage within the local day range
        val usage = dailyUsageRepository.getUsageForDate(
            appId = appId,
            dayStart = dayStart,
            nextDayStart = nextDayStart
        )

        val totalUsageSeconds = usage?.totalUsage ?: 0
        val dailyLimitSeconds = app.dailyLimit * 60

        return totalUsageSeconds >= dailyLimitSeconds
    }
}