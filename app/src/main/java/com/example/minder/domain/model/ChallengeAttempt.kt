package com.example.minder.domain.model

data class ChallengeAttempt(
    val id: Int,
    val interventionId: Int,
    val challengeId: Int,
    val userAnswer: String,
    val isCorrect: Boolean,
    val attemptTime: Long
)