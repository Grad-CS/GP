package com.example.minder.domain.model

//Ragahd-: Holds challenge attempt information together with its challenge type.
data class ChallengeAttemptWithType(
    val interventionId: Int,
    val challengeId: Int,
    val isCorrect: Boolean,
    val attemptTime: Long,
    val challengeType: ChallengeType
)