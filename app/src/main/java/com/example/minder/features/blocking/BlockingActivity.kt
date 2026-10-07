package com.example.minder.features.blocking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.repository.ChallengeRepositoryImpl
import com.example.minder.data.repository.DailyUsageRepositoryImpl
import com.example.minder.data.repository.InterventionRepositoryImpl
import com.example.minder.data.repository.RestrictedAppRepositoryImpl
import com.example.minder.domain.usecase.challenge.GenerateChallengeUseCase
import com.example.minder.domain.usecase.intervention.StartInterventionUseCase
import com.example.minder.domain.usecase.usage.CheckDailyLimitUseCase
import com.example.minder.features.challenge.ChallengeActivity
import com.example.minder.ui.theme.MinderTheme
import kotlinx.coroutines.launch

class BlockingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_APP_ID = "app_id"
        const val EXTRA_INTERVENTION_ID = "intervention_id"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appId =
            intent.getIntExtra(EXTRA_APP_ID, -1)

        val packageName =
            intent.getStringExtra(EXTRA_PACKAGE_NAME)

        if (appId == -1) {
            Log.e(
                "MinderBlocking",
                "Invalid appId"
            )

            finish()
            return
        }

        val database =
            MinderDatabase.getDatabase(applicationContext)

        val restrictedAppRepository =
            RestrictedAppRepositoryImpl(
                database.restrictedAppDao()
            )

        val dailyUsageRepository =
            DailyUsageRepositoryImpl(
                database.dailyUsageDao()
            )

        val challengeRepository =
            ChallengeRepositoryImpl(
                database.challengeDao()
            )

        val interventionRepository =
            InterventionRepositoryImpl(
                database.interventionDao()
            )

        val generateChallengeUseCase =
            GenerateChallengeUseCase(
                challengeRepository
            )

        val startInterventionUseCase =
            StartInterventionUseCase(
                generateChallengeUseCase = generateChallengeUseCase,
                interventionRepository = interventionRepository
            )

        val checkDailyLimitUseCase =
            CheckDailyLimitUseCase(
                restrictedAppRepository = restrictedAppRepository,
                dailyUsageRepository = dailyUsageRepository
            )

        setContent {

            MinderTheme {

                BlockingScreen(
                    appId = appId,

                    // No intervention exists yet.
                    interventionId = -1,

                    initialRemainingSeconds = 0L,

                    checkDailyLimitUseCase =
                        checkDailyLimitUseCase,

                    onTimeFinished = {
                        // Nothing yet
                    },

                    onTakeChallenge = { selectedAppId, _ ->

                        Log.d(
                            "MinderBlocking",
                            "Take the Challenge clicked | appId=$selectedAppId"
                        )

                        lifecycleScope.launch {

                            val result =
                                startInterventionUseCase(
                                    appId = selectedAppId,
                                    difficulty = "EASY"
                                )

                            if (result == null) {

                                Log.e(
                                    "MinderBlocking",
                                    "Failed to create intervention"
                                )

                                return@launch
                            }

                            Log.d(
                                "MinderBlocking",
                                "Intervention created | interventionId=${result.interventionId}"
                            )

                            val challengeIntent =
                                Intent(
                                    this@BlockingActivity,
                                    ChallengeActivity::class.java
                                ).apply {

                                    putExtra(
                                        ChallengeActivity.EXTRA_APP_ID,
                                        selectedAppId
                                    )

                                    putExtra(
                                        ChallengeActivity.EXTRA_INTERVENTION_ID,
                                        result.interventionId
                                    )

                                    putExtra(
                                        ChallengeActivity.EXTRA_PACKAGE_NAME,
                                        packageName
                                    )
                                }

                            startActivity(challengeIntent)

                            finish()
                        }
                    }
                )
            }
        }
    }
}