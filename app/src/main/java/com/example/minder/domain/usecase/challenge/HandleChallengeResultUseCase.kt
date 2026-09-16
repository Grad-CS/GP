package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.repository.InterventionRepository

sealed class ChallengeResult {
    data object Correct : ChallengeResult()
    data object Retry : ChallengeResult()
    data object Locked : ChallengeResult()
}

class HandleChallengeResultUseCase(
    private val submitChallengeUseCase: SubmitChallengeUseCase,
    private val getAttemptCountUseCase: GetAttemptCountUseCase,
    private val interventionRepository: InterventionRepository
) {

    private companion object {
        const val MAX_ATTEMPTS = 3
    }

    suspend operator fun invoke(
        challenge: Challenge,
        interventionId: Int,
        userAnswer: String
    ): ChallengeResult {

        val isCorrect = submitChallengeUseCase(
            challenge = challenge,
            interventionId = interventionId,
            userAnswer = userAnswer
        )

        if (isCorrect) {
            interventionRepository.completeIntervention(
                interventionId = interventionId,
                unlockTime = System.currentTimeMillis(),
                status = "UNLOCKED"
            )

            return ChallengeResult.Correct
        }

        val attemptCount = getAttemptCountUseCase(
            interventionId = interventionId
        )

        return if (attemptCount >= MAX_ATTEMPTS) {

            interventionRepository.completeIntervention(
                interventionId = interventionId,
                unlockTime = null,
                status = "LOCKED"
            )

            ChallengeResult.Locked

        } else {
            ChallengeResult.Retry
        }
    }
}