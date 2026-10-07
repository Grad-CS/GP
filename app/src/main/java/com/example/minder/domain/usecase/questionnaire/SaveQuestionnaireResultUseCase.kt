package com.example.minder.domain.usecase.questionnaire

import com.example.minder.domain.model.DigitalWellbeingRiskScore
import com.example.minder.domain.repository.OnboardingStateRepository

class SaveQuestionnaireResultUseCase(
    private val repository: OnboardingStateRepository
) {

    suspend operator fun invoke(
        score: DigitalWellbeingRiskScore
    ) {
        repository.saveDigitalWellbeingRiskScore(score)
    }
}