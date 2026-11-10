
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.AverageSessionDuration
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UsageSessionRepository
import java.util.Calendar

class GetAverageSessionDurationUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val usageSessionRepository: UsageSessionRepository
) {

    suspend operator fun invoke(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ): AverageSessionDuration {

        //Ragahd-: Define the last seven calendar days including today.
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -6)
        }

        val periodStart = calendar.timeInMillis
        val periodEnd = date

        //Ragahd-: Get only enabled restricted apps.
        val enabledApps = restrictedAppRepository
            .getAppsByUserId(userId)
            .filter { it.isEnabled }

        //Ragahd-: Collect valid sessions from enabled apps.
        val validSessions = enabledApps.flatMap { app ->
            usageSessionRepository.getSessionsForPeriod(
                appId = app.id,
                startTime = periodStart,
                endTime = periodEnd
            ).filter { session ->
                session.startTime >= periodStart &&
                        session.startTime < periodEnd &&
                        session.duration > 0
            }
        }

        //Ragahd-: Calculate total duration using Long to avoid overflow.
        val totalDurationSeconds = validSessions.sumOf {
            it.duration.toLong()
        }

        //Ragahd-: Calculate the average session duration.
        val totalSessions = validSessions.size

        val averageDurationSeconds = if (totalSessions > 0) {
            totalDurationSeconds.toDouble() / totalSessions
        } else {
            0.0
        }

        return AverageSessionDuration(
            averageDurationSeconds = averageDurationSeconds,
            totalSessions = totalSessions
        )
    }
}
