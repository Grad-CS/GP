package com.example.minder.data.repository

import com.example.minder.data.local.dao.ChallengeAttemptDao
import com.example.minder.data.local.entities.ChallengeAttemptEntity
import com.example.minder.domain.model.ChallengeAttempt
import com.example.minder.domain.repository.ChallengeAttemptRepository

class ChallengeAttemptRepositoryImpl(
    private val challengeAttemptDao: ChallengeAttemptDao
) : ChallengeAttemptRepository {

    override suspend fun addAttempt(
        attempt: ChallengeAttempt
    ): Long {
        return challengeAttemptDao.insertAttempt(
            attempt.toEntity()
        )
    }

    override suspend fun getAttemptsByInterventionId(
        interventionId: Int
    ): List<ChallengeAttempt> {
        return challengeAttemptDao
            .getAttemptsByInterventionId(interventionId)
            .map { it.toDomain() }
    }

    override suspend fun getAttemptsByChallengeId(
        challengeId: Int
    ): List<ChallengeAttempt> {
        return challengeAttemptDao
            .getAttemptsByChallengeId(challengeId)
            .map { it.toDomain() }
    }

    override suspend fun getSuccessfulAttempts(
        interventionId: Int
    ): List<ChallengeAttempt> {
        return challengeAttemptDao
            .getSuccessfulAttempts(interventionId)
            .map { it.toDomain() }
    }

    override suspend fun getAttemptCount(
        interventionId: Int
    ): Int {
        return challengeAttemptDao.getAttemptCount(interventionId)
    }
}

private fun ChallengeAttemptEntity.toDomain(): ChallengeAttempt {
    return ChallengeAttempt(
        id = attemptId,
        interventionId = interventionId,
        challengeId = challengeId,
        userAnswer = userAnswer,
        isCorrect = isCorrect,
        attemptTime = attemptTime
    )
}

private fun ChallengeAttempt.toEntity(): ChallengeAttemptEntity {
    return ChallengeAttemptEntity(
        attemptId = id,
        interventionId = interventionId,
        challengeId = challengeId,
        userAnswer = userAnswer,
        isCorrect = isCorrect,
        attemptTime = attemptTime
    )
}