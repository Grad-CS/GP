package com.example.minder.features.challenge

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lama - Gets the intervention information passed by the monitoring service
        val interventionId =
            intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        val appId =
            intent.getIntExtra(EXTRA_APP_ID, -1)

        Log.d(
            "MinderChallenge",
            "ChallengeActivity opened | interventionId=$interventionId | appId=$appId"
        )

        // Lama - Prevents opening the challenge without valid intervention data
        if (interventionId == -1 || appId == -1) {

            Log.e(
                "MinderChallenge",
                "Invalid challenge data"
            )

            finish()
            return
        }

        // Lama - Gets access to the existing Minder database
        val database =
            MinderDatabase.getDatabase(applicationContext)

        // Lama - Uses the challenge repository
        val challengeRepository =
            ChallengeRepositoryImpl(
                database.challengeDao()
            )

        // Lama - Uses the intervention repository
        val interventionRepository =
            InterventionRepositoryImpl(
                database.interventionDao()
            )

        // Lama - Uses the challenge-attempt repository
        val challengeAttemptRepository =
            ChallengeAttemptRepositoryImpl(
                database.challengeAttemptDao()
            )

        // Lama - Gets the challenge linked to the current intervention
        val getInterventionChallengeUseCase =
            GetInterventionChallengeUseCase(
                interventionRepository = interventionRepository,
                challengeRepository = challengeRepository
            )

        // Lama - Validates the user's challenge answer
        val validateChallengeUseCase =
            ValidateChallengeUseCase()

        // Lama - Saves every challenge attempt
        val submitChallengeUseCase =
            SubmitChallengeUseCase(
                validateChallengeUseCase = validateChallengeUseCase,
                challengeAttemptRepository = challengeAttemptRepository
            )

        // Lama - Counts the challenge attempts for the intervention
        val getAttemptCountUseCase =
            GetAttemptCountUseCase(
                challengeAttemptRepository = challengeAttemptRepository
            )

        // Lama - Handles correct, retry, and locked challenge results
        val handleChallengeResultUseCase =
            HandleChallengeResultUseCase(
                submitChallengeUseCase = submitChallengeUseCase,
                getAttemptCountUseCase = getAttemptCountUseCase,
                interventionRepository = interventionRepository
            )

        setContent {

            MinderTheme {

                ChallengeScreen(
                    appId = appId,
                    interventionId = interventionId,
                    getInterventionChallengeUseCase =
                        getInterventionChallengeUseCase,
                    handleChallengeResultUseCase =
                        handleChallengeResultUseCase,

                    // Lama - Closes the challenge after a correct answer
                    onSuccess = {

                        Log.d(
                            "MinderChallenge",
                            "Challenge completed successfully"
                        )

                        // Lama - Notifies monitoring that the challenge is finished
                        // so a new shared usage interval can begin
                        AppAccessibilityService.notifyChallengeCompleted()

                        finish()
                    },

                    // Lama - Closes the challenge after maximum attempts
                    onLocked = {

                        Log.d(
                            "MinderChallenge",
                            "Challenge locked after maximum attempts"
                        )

                        // Ragahd - Notifies monitoring that the locked challenge has ended
                        AppAccessibilityService.notifyChallengeCompleted()

                        finish()
                    },

                    // Lama - Prevents bypassing an active challenge
                    onClose = {

                        Log.d(
                            "MinderChallenge",
                            "Close blocked while challenge is active"
                        )
                    }
                )
            }
        }
    }

    companion object {

        const val EXTRA_INTERVENTION_ID =
            "extra_intervention_id"

        const val EXTRA_APP_ID =
            "extra_app_id"
    }
}