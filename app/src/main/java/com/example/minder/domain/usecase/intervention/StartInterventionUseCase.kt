
package com.example.minder.domain.usecase.intervention

import com.example.minder.domain.model.Intervention
import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.ChallengeType
import com.example.minder.domain.repository.InterventionRepository
import com.example.minder.domain.usecase.challenge.GenerateChallengeUseCase

data class StartInterventionResult(
    val challenge: Challenge,
    val interventionId: Int
)

class StartInterventionUseCase(
    private val generateChallengeUseCase: GenerateChallengeUseCase,
    private val interventionRepository: InterventionRepository
) {

    //Ragahd-: Generates and saves a challenge before creating its intervention.
    suspend operator fun invoke(
        appId: Int,
        difficulty: String,
        type: ChallengeType,
        puzzleGame: String = "ODD_SHAPE"
    ): StartInterventionResult {

        val challenge = generateChallengeUseCase(
            type = type,
            difficulty = difficulty,
            puzzleGame = puzzleGame
        )

        //Ragahd-: Links the intervention to the generated challenge ID.
        val intervention = Intervention(
            id = 0,
            appId = appId,
            challengeId = challenge.id,
            triggerTime = System.currentTimeMillis(),
            unlockTime = null,
            status = "PENDING",
            savedTime = 0
        )

        val interventionId =
            interventionRepository.addIntervention(intervention).toInt()

        return StartInterventionResult(
            challenge = challenge,
            interventionId = interventionId
        )
    }
}
