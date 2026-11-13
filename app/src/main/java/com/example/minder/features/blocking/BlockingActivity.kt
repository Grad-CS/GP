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
import androidx.compose.runtime.mutableStateOf
import com.example.minder.data.repository.ExtensionRequestRepositoryImpl
import com.example.minder.domain.usecase.usage.RequestDailyExtensionUseCase
import java.util.Calendar


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

        //Ragahd-: Initializes the repository that stores daily extension requests.
        val extensionRequestRepository =
            ExtensionRequestRepositoryImpl(
                database.extensionRequestDao()
            )

//Ragahd-: Initializes the use case for requesting a daily extension.
        val requestDailyExtensionUseCase =
            RequestDailyExtensionUseCase(
                extensionRequestRepository = extensionRequestRepository
            )

//Ragahd-: Tracks whether the app has already used its daily extension.
        val extensionAlreadyUsedToday = mutableStateOf<Boolean?>(null)

//Ragahd-: Loads today's extension status for the blocked app.
        lifecycleScope.launch {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val nextDayStart = calendar.timeInMillis

            extensionAlreadyUsedToday.value =
                extensionRequestRepository.getRequestForAppOnDate(
                    appId = appId,
                    dayStart = dayStart,
                    nextDayStart = nextDayStart
                ) != null
        }

        setContent {
            MinderTheme {
                BlockingScreen(
                    appId = appId,
                    interventionId = -1,
                    initialRemainingSeconds = 0L,
                    checkDailyLimitUseCase = checkDailyLimitUseCase,

                    //Ragahd-: Passes the daily extension usage status to the screen.
                    extensionAlreadyUsedToday = extensionAlreadyUsedToday.value,

                    //Ragahd-: Requests the selected extension and closes the blocking screen when granted.
                    onRequestExtension = { selectedMinutes ->
                        lifecycleScope.launch {
                            try {
                                val extensionId = requestDailyExtensionUseCase(
                                    appId = appId,
                                    selectedMinutes = selectedMinutes
                                )

                                if (extensionId != null) {
                                    finish()
                                } else {
                                    extensionAlreadyUsedToday.value = true
                                }
                            } catch (exception: Exception) {
                                Log.e(
                                    "MinderBlocking",
                                    "Failed to request daily extension",
                                    exception
                                )
                            }
                        }
                    },

                    //Ragahd-: Returns the user to Home without granting an extension.
                    onExitApp = {
                        AppAccessibilityService.requestExitToHome()
                        finish()
                    },

                    onTimeFinished = {
                        // Nothing yet
                    }
                )
            }
        }
    }
    // Lama - Notify monitoring when the blocking screen is no longer visible.
    override fun onStop() {
        super.onStop()
        AppAccessibilityService.notifyBlockingClosed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}