package com.example.minder.data.repository

import com.example.minder.data.local.preferences.OnboardingPreferencesDataStore
import com.example.minder.domain.model.DigitalWellbeingRiskScore
import com.example.minder.domain.model.RiskLevel
import com.example.minder.domain.repository.OnboardingStateRepository
import kotlinx.coroutines.flow.first

class OnboardingStateRepositoryImpl(
    private val dataStore: OnboardingPreferencesDataStore
) : OnboardingStateRepository {

    override suspend fun isOnboardingCompleted(): Boolean {
        return dataStore.onboardingCompleted.first()
    }

    override suspend fun setOnboardingCompleted(
        completed: Boolean
    ) {
        dataStore.setOnboardingCompleted(completed)
    }

    override suspend fun saveDigitalWellbeingRiskScore(
        score: DigitalWellbeingRiskScore
    ) {
        dataStore.saveDigitalWellbeingRiskScore(score)
    }

    override suspend fun getDigitalWellbeingRiskScore():
            DigitalWellbeingRiskScore? {

        return dataStore.digitalWellbeingRiskScore.first()
            ?.let { (totalScore, level) ->
                DigitalWellbeingRiskScore(
                    totalScore = totalScore,
                    level = RiskLevel.valueOf(level)
                )
            }
    }
}