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
import com.example.minder.services.monitoring.AppAccessibilityService
//Ragahd-: Imports the challenge types used for intervention creation.
import com.example.minder.domain.model.ChallengeType
//Ragahd-: Provides access to the current user.
import com.example.minder.data.repository.UserRepositoryImpl
//Ragahd-: Provides access to the user's saved settings.
import com.example.minder.data.repository.UserSettingsRepositoryImpl


class BlockingActivity : ComponentActivity() {

    //Ragahd-: Prevents duplicate challenge creation from repeated taps.
    private var isStartingChallenge = false

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

//Ragahd-: Initializes repositories for retrieving the current user and saved settings.
        val userRepository = UserRepositoryImpl(database.userDao())

//Ragahd-: Initializes the repository that stores user preferences.
        val userSettingsRepository =
            UserSettingsRepositoryImpl(database.userSettingsDao())

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
                        if (!isStartingChallenge) {
                            isStartingChallenge = true

                            lifecycleScope.launch {
                                try {
                                    //Ragahd-: Selects a challenge type for the new intervention.
                                    val challengeType = ChallengeType.values().random()

                                    //Ragahd-: Retrieves the current user from the database.
                                    val currentUser = userRepository.getUser()

                                    //Ragahd-: Retrieves the current user's saved settings.
                                    val settings = currentUser?.let { user ->
                                        userSettingsRepository.getSettingsByUserId(user.userId)
                                    }

                                    //Ragahd-: Uses the saved difficulty or EASY when settings are unavailable or invalid.
                                    val difficulty = settings?.challengeDifficulty
                                        ?.uppercase()
                                        ?.takeIf { it in setOf("EASY", "MEDIUM", "HARD") }
                                        ?: "EASY"

                                   //Ragahd-: Creates the intervention using the saved difficulty.
                                    val result = startInterventionUseCase(
                                        appId = selectedAppId,
                                        difficulty = difficulty,
                                        type = challengeType
                                    )

                                    //Ragahd-: Logs the intervention and linked challenge IDs.
                                    Log.d(
                                        "MinderBlocking",
                                        "Intervention created | interventionId=${result.interventionId} | " +
                                                "challengeId=${result.challenge.id} | type=$challengeType"
                                    )

                                    //Ragahd-: Opens the challenge without finishing the blocking screen.
                                    val challengeIntent = Intent(
                                        this@BlockingActivity,
                                        ChallengeActivity::class.java
                                    ).apply {
                                        putExtra(ChallengeActivity.EXTRA_APP_ID, selectedAppId)
                                        putExtra(
                                            ChallengeActivity.EXTRA_INTERVENTION_ID,
                                            result.interventionId
                                        )
                                        putExtra(ChallengeActivity.EXTRA_PACKAGE_NAME, packageName)
                                        putExtra(ChallengeActivity.EXTRA_FROM_DAILY_BLOCK, true)
                                    }

                                    challengeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(challengeIntent)

                                    //Ragahd-: Keeps BlockingActivity underneath the challenge.
                                } catch (exception: Exception) {
                                    //Ragahd-: Allows another attempt if challenge creation fails.
                                    isStartingChallenge = false
                                    Log.e(
                                        "MinderBlocking",
                                        "Failed to create daily-block challenge",
                                        exception
                                    )
                                }
                            }
                        }
                    }

                )
            }
        }
    }
    override fun onDestroy() {
        AppAccessibilityService.notifyBlockingClosed()
        super.onDestroy()
    }
}