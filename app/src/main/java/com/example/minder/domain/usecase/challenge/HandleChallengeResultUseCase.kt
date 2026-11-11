
package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.repository.InterventionRepository
import java.util.Calendar

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

        // Lama - Retrieve the current intervention.
        val intervention =
            interventionRepository.getInterventionById(interventionId)
                ?: error("Intervention not found: $interventionId")

        // Lama - Prevent attempts on finished interventions.
        when (intervention.status) {
            "LOCKED" -> return ChallengeResult.Locked
            "UNLOCKED" -> return ChallengeResult.Correct
            "PENDING" -> Unit
            else -> error(
                "Unexpected intervention status: ${intervention.status}"
            )
        }

        // Lama - Validate and save the user's answer.
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

        // Lama - Count incorrect attempts.
        val attemptCount = getAttemptCountUseCase(
            interventionId = interventionId
        )

        if (attemptCount >= MAX_ATTEMPTS) {

            // Lama - Lock restricted apps until next local midnight.
            val lockExpiresAt = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            interventionRepository.completeIntervention(
                interventionId = interventionId,
                unlockTime = lockExpiresAt,
                status = "LOCKED"
            )

            return ChallengeResult.Locked
        }

        return ChallengeResult.Retry
    }
}
