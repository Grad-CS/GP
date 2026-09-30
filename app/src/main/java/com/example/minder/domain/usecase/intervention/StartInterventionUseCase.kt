package com.example.minder.domain.usecase.intervention

import com.example.minder.domain.model.Intervention
import com.example.minder.domain.model.Challenge
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

    suspend operator fun invoke(
        appId: Int,
        difficulty: String,
        type: String? = null
    ): StartInterventionResult? {

        val challenge = generateChallengeUseCase(
            difficulty = difficulty,
            type = type
        ) ?: return null

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
            interventionRepository.addIntervention(intervention)
                .toInt()

        return StartInterventionResult(
            challenge = challenge,
            interventionId = interventionId
        )
    }
}