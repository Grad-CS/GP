
package com.example.minder.domain.usecase.intervention

import com.example.minder.domain.model.InterventionStatistics
import com.example.minder.domain.repository.InterventionRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import java.util.Calendar

class GetInterventionStatisticsUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val interventionRepository: InterventionRepository
) {

    //Ragahd-: Retrieves intervention statistics for the last seven calendar days.
    suspend operator fun invoke(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ): InterventionStatistics {

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

        //Ragahd-: Collects interventions triggered within the selected period.
        val interventions = enabledApps.flatMap { app ->
            interventionRepository.getInterventionsForPeriod(
                appId = app.id,
                startTime = periodStart,
                endTime = periodEnd
            )
        }

        //Ragahd-: Counts interventions according to their final status.
        val unlockedCount = interventions.count {
            it.status == "UNLOCKED"
        }

        val lockedCount = interventions.count {
            it.status == "LOCKED"
        }

        val pendingCount = interventions.count {
            it.status == "PENDING"
        }

        //Ragahd-: Calculates resolution durations for valid unlocked interventions.
        val resolutionDurations = interventions.mapNotNull { intervention ->
            if (intervention.status != "UNLOCKED") {
                null
            } else {
                intervention.unlockTime
                    ?.takeIf { it > intervention.triggerTime }
                    ?.minus(intervention.triggerTime)
            }
        }

        val averageResolutionTimeSeconds =
            if (resolutionDurations.isNotEmpty()) {
                resolutionDurations.average() / 1000.0
            } else {
                0.0
            }

        return InterventionStatistics(
            totalInterventions = interventions.size,
            unlockedInterventions = unlockedCount,
            lockedInterventions = lockedCount,
            pendingInterventions = pendingCount,
            averageResolutionTimeSeconds = averageResolutionTimeSeconds
        )
    }
}
