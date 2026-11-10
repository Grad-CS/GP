
package com.example.minder.domain.model

//Ragahd-: Holds the challenge success rate and completed intervention counts.
data class ChallengeSuccessRate(
    val totalCompletedInterventions: Int,
    val successfulInterventions: Int,
    val failedInterventions: Int,
    val successRatePercentage: Double
)
