package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.AttemptsAndChallengeTypesStatistics
import com.example.minder.domain.model.ChallengeType
import com.example.minder.domain.model.ChallengeTypeStatistics
import com.example.minder.domain.repository.ChallengeAttemptRepository
import java.util.Calendar

class GetAttemptsAndChallengeTypesStatisticsUseCase(
    private val challengeAttemptRepository: ChallengeAttemptRepository
) {

    //Ragahd-: Calculates challenge attempt statistics over the last seven calendar days.
    suspend operator fun invoke(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ): AttemptsAndChallengeTypesStatistics {

        //Ragahd-: Defines the start of the seven-day period in local time.
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -6)
        }

        val periodStart = calendar.timeInMillis
        val periodEnd = date

        //Ragahd-: Retrieves attempts made by the user within the selected period.
        val attempts = challengeAttemptRepository.getAttemptsWithTypeForPeriod(
            userId = userId,
            startTime = periodStart,
            endTime = periodEnd
        )

        //Ragahd-: Calculates overall attempt counts.
        val totalAttempts = attempts.size
        val correctAttempts = attempts.count { it.isCorrect }
        val incorrectAttempts = attempts.count { !it.isCorrect }

        //Ragahd-: Calculates the average attempts per intervention with recorded attempts.
        val uniqueInterventions = attempts
            .map { it.interventionId }
            .distinct()
            .size

        val averageAttemptsPerIntervention =
            if (uniqueInterventions > 0) {
                totalAttempts.toDouble() / uniqueInterventions
            } else {
                0.0
            }

        //Ragahd-: Calculates attempt statistics for every supported challenge type.
        val attemptsByChallengeType = ChallengeType.values().associateWith { type ->

            val typeAttempts = attempts.filter {
                it.challengeType == type
            }

            val typeCorrectAttempts = typeAttempts.count {
                it.isCorrect
            }

            val typeIncorrectAttempts = typeAttempts.count {
                !it.isCorrect
            }

            val successRatePercentage =
                if (typeAttempts.isNotEmpty()) {
                    (typeCorrectAttempts.toDouble() / typeAttempts.size) * 100.0
                } else {
                    0.0
                }

            ChallengeTypeStatistics(
                totalAttempts = typeAttempts.size,
                correctAttempts = typeCorrectAttempts,
                incorrectAttempts = typeIncorrectAttempts,
                successRatePercentage = successRatePercentage
            )
        }

        return AttemptsAndChallengeTypesStatistics(
            totalAttempts = totalAttempts,
            correctAttempts = correctAttempts,
            incorrectAttempts = incorrectAttempts,
            averageAttemptsPerIntervention = averageAttemptsPerIntervention,
            attemptsByChallengeType = attemptsByChallengeType
        )
    }
}