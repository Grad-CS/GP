package com.example.minder.domain.usecase.intervention

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.repository.ChallengeRepository
import com.example.minder.domain.repository.InterventionRepository

class GetInterventionChallengeUseCase(
    private val interventionRepository: InterventionRepository,
    private val challengeRepository: ChallengeRepository
) {
    suspend operator fun invoke(
        interventionId: Int
    ): Challenge? {

        val intervention =
            interventionRepository.getInterventionById(interventionId)
                ?: return null

        return challengeRepository.getChallengeById(
            intervention.challengeId
        )
    }
}