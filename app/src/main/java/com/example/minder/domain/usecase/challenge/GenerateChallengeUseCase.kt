package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.ChallengeGenerator
import com.example.minder.domain.model.ChallengeType
import com.example.minder.domain.model.VisualChallengeGenerator
import com.example.minder.domain.repository.ChallengeRepository

class GenerateChallengeUseCase(
    private val challengeRepository: ChallengeRepository
) {
    // Ragahd - Generates and persists a new challenge for the intervention.
    suspend operator fun invoke(
        type: ChallengeType,
        difficulty: String = "EASY",
        puzzleGame: String = "ODD_SHAPE"
    ): Challenge {
        val generated = when (type) {
            ChallengeType.MATH -> ChallengeGenerator.generateMathChallenge(difficulty)
            ChallengeType.PUZZLE -> when (puzzleGame.uppercase()) {
                "ODD_SHAPE" -> VisualChallengeGenerator.generateOddShape(difficulty)
                "SEQUENCE_MEMORY" -> VisualChallengeGenerator.generateSequenceMemory(difficulty)
                else -> throw IllegalArgumentException("Unsupported puzzle game: $puzzleGame")
            }
            ChallengeType.LOGIC -> VisualChallengeGenerator.generatePattern(difficulty)
        }

        val challengeId = challengeRepository.saveGeneratedChallenge(generated)
        return Challenge(
            id = challengeId,
            question = generated.question,
            correctAnswer = generated.correctAnswer,
            difficulty = generated.difficulty,
            type = generated.challengeType.name,
            gameData = generated.gameData
        )
    }
}
