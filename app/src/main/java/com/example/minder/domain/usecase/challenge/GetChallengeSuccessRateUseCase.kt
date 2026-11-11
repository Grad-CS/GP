
package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.ChallengeSuccessRate
import com.example.minder.domain.repository.InterventionRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import java.util.Calendar

class GetChallengeSuccessRateUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val interventionRepository: InterventionRepository
) {

    //Ragahd-: Calculates the challenge success rate over the last seven calendar days.
    suspend operator fun invoke(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ): ChallengeSuccessRate {

        //Ragahd-: Defines the start of the seven-day period in local time.
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

        //Ragahd-: Includes only currently enabled restricted apps.
        val enabledApps = restrictedAppRepository
            .getAppsByUserId(userId)
            .filter { it.isEnabled }

        //Ragahd-: Retrieves interventions triggered during the selected period.
        val interventions = enabledApps.flatMap { app ->
            interventionRepository.getInterventionsForPeriod(
                appId = app.id,
                startTime = periodStart,
                endTime = periodEnd
            )
        }

        //Ragahd-: Counts only interventions that have reached a final status.
        val successfulCount = interventions.count {
            it.status == "UNLOCKED"
        }

        val failedCount = interventions.count {
            it.status == "LOCKED"
        }

        val totalCompleted = successfulCount + failedCount

        //Ragahd-: Calculates the percentage using completed interventions only.
        val successRate = if (totalCompleted > 0) {
            (successfulCount.toDouble() / totalCompleted) * 100.0
        } else {
            0.0
        }

        return ChallengeSuccessRate(
            totalCompletedInterventions = totalCompleted,
            successfulInterventions = successfulCount,
            failedInterventions = failedCount,
            successRatePercentage = successRate
        )
    }
}
