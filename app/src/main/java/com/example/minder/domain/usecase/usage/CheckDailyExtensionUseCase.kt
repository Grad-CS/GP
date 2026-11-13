package com.example.minder.domain.usecase.usage

import com.example.minder.domain.repository.ExtensionRequestRepository
import java.util.Calendar

class CheckDailyExtensionUseCase(
    private val extensionRequestRepository: ExtensionRequestRepository
) {

    suspend operator fun invoke(
        appId: Int,
        currentTime: Long = System.currentTimeMillis()
    ): Boolean {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayStart = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val nextDayStart = calendar.timeInMillis

        val request = extensionRequestRepository.getRequestForAppOnDate(
            appId = appId,
            dayStart = dayStart,
            nextDayStart = nextDayStart
        ) ?: return false

        val expirationTime =
            request.requestTime + request.grantedMinutes * 60_000L

        return currentTime < expirationTime
    }
}