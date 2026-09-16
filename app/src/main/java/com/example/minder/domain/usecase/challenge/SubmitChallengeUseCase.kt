package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.ChallengeAttempt
import com.example.minder.domain.repository.ChallengeAttemptRepository

class SubmitChallengeUseCase(
    private val validateChallengeUseCase: ValidateChallengeUseCase,
    private val challengeAttemptRepository: ChallengeAttemptRepository
) {

    suspend operator fun invoke(
        challenge: Challenge,
        interventionId: Int,
        userAnswer: String
    ): Boolean {

        val isCorrect = validateChallengeUseCase(
            challenge = challenge,
            userAnswer = userAnswer
        )

        val attempt = ChallengeAttempt(
            id = 0,
            interventionId = interventionId,
            challengeId = challenge.id,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            attemptTime = System.currentTimeMillis()
        )

        challengeAttemptRepository.addAttempt(attempt)

        return isCorrect
    }
}