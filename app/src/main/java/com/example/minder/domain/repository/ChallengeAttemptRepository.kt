package com.example.minder.domain.repository

import com.example.minder.domain.model.ChallengeAttempt

interface ChallengeAttemptRepository {

    suspend fun addAttempt(
        attempt: ChallengeAttempt
    ): Long

    suspend fun getAttemptsByInterventionId(
        interventionId: Int
    ): List<ChallengeAttempt>

    suspend fun getAttemptsByChallengeId(
        challengeId: Int
    ): List<ChallengeAttempt>

    suspend fun getSuccessfulAttempts(
        interventionId: Int
    ): List<ChallengeAttempt>

    suspend fun getAttemptCount(
        interventionId: Int
    ): Int
}