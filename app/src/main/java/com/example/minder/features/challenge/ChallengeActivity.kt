
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

        // Lama - Gets the intervention information passed by the monitoring service
        val interventionId =
            intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        val appId =
            intent.getIntExtra(EXTRA_APP_ID, -1)

        val packageName =
            intent.getStringExtra(EXTRA_PACKAGE_NAME)

        //Lama - Identifies challenges started from the daily blocking screen.
        val fromDailyBlock = intent.getBooleanExtra(
            EXTRA_FROM_DAILY_BLOCK,
            false
        )

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

        //Ragahd-: Creates the ViewModel with the existing use cases.
        val challengeViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    if (modelClass.isAssignableFrom(ChallengeViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ChallengeViewModel(
                            interventionId = interventionId,
                            getInterventionChallengeUseCase = getInterventionChallengeUseCase,
                            handleChallengeResultUseCase = handleChallengeResultUseCase
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[ChallengeViewModel::class.java]

        setContent {
            MinderTheme {
                //Ragahd-: Passes the ViewModel to the updated challenge screen.
                ChallengeScreen(
                    appId = appId,
                    interventionId = interventionId,
                    viewModel = challengeViewModel,


                    onSuccess = {
                        //Ragahd-: Marks the challenge as finished.
                        challengeFinished = true

                        Log.d(
                            "MinderChallenge",
                            "Challenge completed successfully | fromDailyBlock=$fromDailyBlock"
                        )

                        //Ragahd-: Notifies monitoring about the challenge source.
                        AppAccessibilityService.notifyChallengeCompleted(fromDailyBlock)

                        //Ragahd-: Moves the task to the background only for periodic challenges.
                        if (!fromDailyBlock) {
                            moveTaskToBack(true)
                        }

                        finish()
                    },


                    onLocked = {
                        //Ragahd-: Marks the locked challenge as finished.
                        challengeFinished = true

                        Log.d(
                            "MinderChallenge",
                            "Challenge locked after maximum attempts"
                        )

                        //Ragahd-: Preserves the source of the completed challenge.
                        AppAccessibilityService.notifyChallengeCompleted(fromDailyBlock)
                        finish()
                    },

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

    //Lama - Detects when an unfinished challenge becomes hidden.
    override fun onStop() {
        super.onStop()

        val interventionId =
            intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        if (
            interventionId != -1 &&
            !isChangingConfigurations &&
            !challengeFinished
        ) {
            AppAccessibilityService.notifyChallengeHidden(
                interventionId
            )
        }
    }

    companion object {

        const val EXTRA_INTERVENTION_ID =
            "extra_intervention_id"

        const val EXTRA_APP_ID =
            "extra_app_id"

        const val EXTRA_PACKAGE_NAME =
            "extra_package_name"

        //Ragahd-: Identifies challenges launched from the daily blocking screen.
        const val EXTRA_FROM_DAILY_BLOCK =
            "extra_from_daily_block"
    }
}
