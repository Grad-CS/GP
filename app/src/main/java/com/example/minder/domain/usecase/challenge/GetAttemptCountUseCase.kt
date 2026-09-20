package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.repository.ChallengeAttemptRepository

class GetAttemptCountUseCase(
    private val challengeAttemptRepository: ChallengeAttemptRepository
) {

    suspend operator fun invoke(
        interventionId: Int
    ): Int {
        return challengeAttemptRepository.getAttemptCount(
            interventionId
        )
    }
}