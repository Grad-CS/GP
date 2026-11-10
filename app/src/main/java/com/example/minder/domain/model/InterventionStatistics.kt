
package com.example.minder.domain.model

//Ragahd-: Holds intervention statistics for the selected period.
data class InterventionStatistics(
    val totalInterventions: Int,
    val unlockedInterventions: Int,
    val lockedInterventions: Int,
    val pendingInterventions: Int,
    val averageResolutionTimeSeconds: Double
)
