package com.example.minder.data.local.models

//Ragahd-: Holds challenge attempt data together with its challenge type.
data class ChallengeAttemptWithTypeEntity(
    val interventionId: Int,
    val challengeId: Int,
    val isCorrect: Boolean,
    val attemptTime: Long,
    val challengeType: String
)