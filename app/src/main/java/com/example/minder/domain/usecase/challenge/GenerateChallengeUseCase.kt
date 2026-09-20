package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.repository.ChallengeRepository

class GenerateChallengeUseCase(
    private val challengeRepository: ChallengeRepository
) {

    suspend operator fun invoke(
        difficulty: String,
        type: String? = null
    ): Challenge? {

        val challenges = if (type != null) {
            challengeRepository.getChallengesByType(type)
                .filter { it.difficulty.equals(difficulty, ignoreCase = true) }
        } else {
            challengeRepository.getChallengesByDifficulty(difficulty)
        }

        return challenges.randomOrNull()
    }
}