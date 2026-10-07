package com.example.minder.domain.repository

import com.example.minder.domain.model.DigitalWellbeingRiskScore

interface OnboardingStateRepository {

    suspend fun isOnboardingCompleted(): Boolean

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun saveDigitalWellbeingRiskScore(
        score: DigitalWellbeingRiskScore
    )

    suspend fun getDigitalWellbeingRiskScore():
            DigitalWellbeingRiskScore?
}