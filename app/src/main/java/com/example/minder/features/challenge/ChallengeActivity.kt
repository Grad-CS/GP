package com.example.minder.features.challenge

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.repository.ChallengeAttemptRepositoryImpl
import com.example.minder.data.repository.ChallengeRepositoryImpl
import com.example.minder.data.repository.InterventionRepositoryImpl
import com.example.minder.domain.usecase.challenge.GetAttemptCountUseCase
import com.example.minder.domain.usecase.challenge.HandleChallengeResultUseCase
import com.example.minder.domain.usecase.challenge.SubmitChallengeUseCase
import com.example.minder.domain.usecase.challenge.ValidateChallengeUseCase
import com.example.minder.domain.usecase.intervention.GetInterventionChallengeUseCase
import com.example.minder.services.monitoring.AppAccessibilityService
import com.example.minder.ui.theme.MinderTheme

class ChallengeActivity : ComponentActivity() {
    private var challengeFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)
        val appId = intent.getIntExtra(EXTRA_APP_ID, -1)
        AppAccessibilityService.notifyChallengeCompleted()
        moveTaskToBack(true)
        finish()

        Log.d("MinderChallenge", "Opened | interventionId=$interventionId | appId=$appId")
        if (interventionId == -1 || appId == -1) {
            Log.e("MinderChallenge", "Invalid challenge data")
            finish()
            return
        }

        val database = MinderDatabase.getDatabase(applicationContext)
        val challengeRepository = ChallengeRepositoryImpl(database.challengeDao())
        val interventionRepository = InterventionRepositoryImpl(database.interventionDao())
        val attemptRepository = ChallengeAttemptRepositoryImpl(database.challengeAttemptDao())

        val getChallenge = GetInterventionChallengeUseCase(
            interventionRepository = interventionRepository,
            challengeRepository = challengeRepository
        )
        val handleResult = HandleChallengeResultUseCase(
            submitChallengeUseCase = SubmitChallengeUseCase(
                validateChallengeUseCase = ValidateChallengeUseCase(),
                challengeAttemptRepository = attemptRepository
            ),
            getAttemptCountUseCase = GetAttemptCountUseCase(
                challengeAttemptRepository = attemptRepository
            ),
            interventionRepository = interventionRepository
        )

        val challengeViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ChallengeViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ChallengeViewModel(
                            interventionId = interventionId,
                            getInterventionChallengeUseCase = getChallenge,
                            handleChallengeResultUseCase = handleResult
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[ChallengeViewModel::class.java]

        setContent {
            MinderTheme {
                ChallengeScreen(
                    appId = appId,
                    interventionId = interventionId,
                    viewModel = challengeViewModel,
                    onSuccess = {
                        challengeFinished = true
                        Log.d("MinderChallenge", "Succeeded | fromDailyBlock=$fromDailyBlock")
                        AppAccessibilityService.notifyChallengeCompleted(fromDailyBlock)
                        if (!fromDailyBlock) moveTaskToBack(true)
                        finish()
                    },

                    onLocked = {
                        // Lama - Challenge locked after 3 wrong answers.
                        challengeFinished = true

                        Log.d(
                            "MinderChallenge",
                            "Challenge locked after maximum attempts"
                        )

                        // Lama - Notify monitoring to block restricted apps.
                        AppAccessibilityService.notifyChallengeLocked(
                            interventionId
                        )

                        finish()
                    },

                    onClose = {
                        Log.d("MinderChallenge", "Close blocked while challenge is active")
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)
        if (interventionId != -1 && !isChangingConfigurations && !challengeFinished) {
            AppAccessibilityService.notifyChallengeHidden(interventionId)
        }
    }

    companion object {
        const val EXTRA_INTERVENTION_ID = "extra_intervention_id"
        const val EXTRA_APP_ID = "extra_app_id"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_FROM_DAILY_BLOCK = "extra_from_daily_block"
    }
}
