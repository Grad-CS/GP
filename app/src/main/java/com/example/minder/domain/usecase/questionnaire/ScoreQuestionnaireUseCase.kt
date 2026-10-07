package com.example.minder.domain.usecase.questionnaire

import com.example.minder.domain.model.DigitalWellbeingRiskScore
import com.example.minder.domain.model.RiskLevel

class ScoreQuestionnaireUseCase {

    operator fun invoke(answers: List<String>): DigitalWellbeingRiskScore {

        require(answers.size == 3) {
            "Questionnaire must contain exactly 3 answers."
        }

        val score = answers.sumOf { answer ->
            answerScore(answer)
        }

        val level = when (score) {
            in 0..3 -> RiskLevel.LOW
            in 4..6 -> RiskLevel.MODERATE
            in 7..9 -> RiskLevel.HIGH
            else -> RiskLevel.VERY_HIGH
        }

        return DigitalWellbeingRiskScore(
            totalScore = score,
            level = level
        )
    }

    private fun answerScore(answer: String): Int {
        return when (answer.trim().lowercase()) {
            "very low",
            "never" -> 0

            "low",
            "rarely" -> 1

            "moderate",
            "sometimes" -> 2

            "high",
            "often" -> 3

            "very high",
            "very often",
            "always" -> 4

            else -> {
                throw IllegalArgumentException(
                    "Unknown questionnaire answer: $answer"
                )
            }
        }
    }
}