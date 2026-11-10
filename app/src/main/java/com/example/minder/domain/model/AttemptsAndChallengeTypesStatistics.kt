package com.example.minder.domain.model

//Ragahd-: Holds overall challenge attempt statistics and statistics grouped by challenge type.
data class AttemptsAndChallengeTypesStatistics(
    val totalAttempts: Int,
    val correctAttempts: Int,
    val incorrectAttempts: Int,
    val averageAttemptsPerIntervention: Double,
    val attemptsByChallengeType: Map<ChallengeType, ChallengeTypeStatistics>
)

//Ragahd-: Holds attempt counts and success rate for a single challenge type.
data class ChallengeTypeStatistics(
    val totalAttempts: Int,
    val correctAttempts: Int,
    val incorrectAttempts: Int,
    val successRatePercentage: Double
)