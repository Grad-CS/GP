
package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.ChallengeGenerator
import com.example.minder.domain.model.ChallengeType
import com.example.minder.domain.model.VisualChallengeGenerator
import com.example.minder.domain.repository.ChallengeRepository


class GenerateChallengeUseCase(
    private val challengeRepository: ChallengeRepository
) {

    //Ragahd-: Generates a new challenge, saves it, and returns its database ID.
    suspend operator fun invoke(
        type: ChallengeType,
        difficulty: String = "EASY",
        puzzleGame: String = "ODD_SHAPE"
    ): Challenge {
        val generated = when (type) {
            ChallengeType.MATH -> {
                ChallengeGenerator.generateMathChallenge(difficulty)
            }

            ChallengeType.PUZZLE -> {
                when (puzzleGame.uppercase()) {
                    //Ragahd-: Passes the requested difficulty to each visual challenge generator.
                    "ODD_SHAPE" ->
                        VisualChallengeGenerator.generateOddShape(difficulty)

                    "SEQUENCE_MEMORY" ->
                        VisualChallengeGenerator.generateSequenceMemory(difficulty)

                    else -> throw IllegalArgumentException(
                        "Unsupported puzzle game: $puzzleGame"
                    )
                }
            }

            ChallengeType.LOGIC -> {
                VisualChallengeGenerator.generatePattern(difficulty)
            }
        }

        //Ragahd-: Persists the generated round before returning its database ID.
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
