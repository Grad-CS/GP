
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.ExtensionRequest
import com.example.minder.domain.repository.ExtensionRequestRepository
import java.util.Calendar

class RequestDailyExtensionUseCase(
    private val extensionRequestRepository: ExtensionRequestRepository
) {

    suspend operator fun invoke(
        appId: Int,
        selectedMinutes: Int,
        requestTime: Long = System.currentTimeMillis()
    ): Long? {

        require(selectedMinutes in listOf(10, 15, 20)) {
            "Extension must be 10, 15, or 20 minutes"
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = requestTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayStart = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)

        val nextDayStart = calendar.timeInMillis

        val request = ExtensionRequest(
            appId = appId,
            requestedMinutes = selectedMinutes,
            grantedMinutes = selectedMinutes,
            requestTime = requestTime,
            status = "ACTIVE"
        )

        return extensionRequestRepository.addDailyExtensionIfAbsent(
            request = request,
            dayStart = dayStart,
            nextDayStart = nextDayStart
        )
    }
}
