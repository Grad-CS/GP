package com.example.minder.domain.repository

import com.example.minder.domain.model.ChallengeAttempt
import com.example.minder.domain.model.ChallengeAttemptWithType

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

    //Ragahd-: Retrieves challenge attempts with their types within a specific period.
    suspend fun getAttemptsWithTypeForPeriod(
        userId: Int,
        startTime: Long,
        endTime: Long
    ): List<ChallengeAttemptWithType>
}