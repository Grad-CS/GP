package com.example.minder.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.minder.domain.model.DigitalWellbeingRiskScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(
    name = "onboarding_preferences"
)

private val ONBOARDING_COMPLETED =
    booleanPreferencesKey("onboarding_completed")

private val DIGITAL_WELLBEING_SCORE =
    intPreferencesKey("digital_wellbeing_score")

private val DIGITAL_WELLBEING_RISK_LEVEL =
    stringPreferencesKey("digital_wellbeing_risk_level")

class OnboardingPreferencesDataStore(
    private val context: Context
) {

    val onboardingCompleted: Flow<Boolean> =
        context.onboardingDataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    val digitalWellbeingRiskScore:
            Flow<Pair<Int, String>?> =
        context.onboardingDataStore.data.map { preferences ->

            val score =
                preferences[DIGITAL_WELLBEING_SCORE]

            val level =
                preferences[DIGITAL_WELLBEING_RISK_LEVEL]

            if (score != null && level != null) {
                Pair(score, level)
            } else {
                null
            }
        }

    suspend fun setOnboardingCompleted(
        completed: Boolean
    ) {
        context.onboardingDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[ONBOARDING_COMPLETED] = completed
            }
        }
    }

    suspend fun saveDigitalWellbeingRiskScore(
        score: DigitalWellbeingRiskScore
    ) {
        context.onboardingDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[DIGITAL_WELLBEING_SCORE] =
                    score.totalScore

                this[DIGITAL_WELLBEING_RISK_LEVEL] =
                    score.level.name
            }
        }
    }
}