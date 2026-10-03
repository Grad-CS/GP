package com.example.minder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.minder.data.local.database.ChallengeSeeder
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.repository.DailyUsageRepositoryImpl
import com.example.minder.data.repository.RestrictedAppRepositoryImpl
import com.example.minder.data.repository.UsageSessionRepositoryImpl
import com.example.minder.data.repository.UserRepositoryImpl
import com.example.minder.domain.usecase.usage.SaveUsageSessionsUseCase
import com.example.minder.domain.usecase.usage.UpdateDailyUsageUseCase
import com.example.minder.features.onboarding.OnboardingScreen
import com.example.minder.features.questionnaire.QuestionnaireScreen
import com.example.minder.services.monitoring.AppMonitoringService
import com.example.minder.services.usage.UsagePermissionManager
import com.example.minder.services.usage.UsageStatsService
import com.example.minder.ui.theme.MinderTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import android.util.Log

class MainActivity : ComponentActivity() {

    private lateinit var usagePermissionManager: UsagePermissionManager
    private lateinit var database: MinderDatabase
    private lateinit var usageStatsService: UsageStatsService
    private lateinit var saveUsageSessionsUseCase: SaveUsageSessionsUseCase
    private lateinit var updateDailyUsageUseCase: UpdateDailyUsageUseCase
    private lateinit var restrictedAppRepository: RestrictedAppRepositoryImpl
    private lateinit var userRepository: UserRepositoryImpl

    // Detects the application currently in the foreground
    private lateinit var appMonitoringService: AppMonitoringService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Initializes the Usage Access permission manager
        usagePermissionManager =
            UsagePermissionManager(this)

        // Initializes the Minder Room database
        database =
            MinderDatabase.getDatabase(applicationContext)

        // Seeds the default challenges when the challenge table is empty
        lifecycleScope.launch {
            ChallengeSeeder.seedIfEmpty(
                database.challengeDao()
            )
        }

        // Initializes Android usage statistics
        usageStatsService =
            UsageStatsService(this)

        // Initializes foreground application monitoring
        appMonitoringService =
            AppMonitoringService(this)

        // Initializes the user repository
        userRepository =
            UserRepositoryImpl(
                database.userDao()
            )

        // Initializes the restricted application repository
        restrictedAppRepository =
            RestrictedAppRepositoryImpl(
                database.restrictedAppDao()
            )

        // Initializes the usage session repository
        val usageSessionRepository =
            UsageSessionRepositoryImpl(
                database.usageSessionDao()
            )

        // Initializes the daily usage repository
        val dailyUsageRepository =
            DailyUsageRepositoryImpl(
                database.dailyUsageDao()
            )

        // Initializes usage-session saving
        saveUsageSessionsUseCase =
            SaveUsageSessionsUseCase(
                restrictedAppRepository = restrictedAppRepository,
                usageSessionRepository = usageSessionRepository
            )

        // Initializes daily usage updating
        updateDailyUsageUseCase =
            UpdateDailyUsageUseCase(
                usageSessionRepository = usageSessionRepository,
                dailyUsageRepository = dailyUsageRepository
            )

        setContent {
            MinderTheme {

                OnboardingScreen(
                    onGetStarted = {

                        setContent {
                            MinderTheme {

                                QuestionnaireScreen(
                                    onNext = { answers ->

                                        Log.d(
                                            "MinderQuestionnaire",
                                            "Questionnaire answers = $answers"
                                        )
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // onResume may run before initialization has completed in onCreate
        if (!::usagePermissionManager.isInitialized) {
            return
        }

        syncUsageData()

    }

    private fun syncUsageData() {

        Log.d("MinderUsage", "syncUsageData STARTED")

        val hasUsageAccess =
            usagePermissionManager.hasUsageAccess()

        Log.d(
            "MinderUsage",
            "Usage Access = $hasUsageAccess"
        )

        if (!hasUsageAccess) {
            Log.d(
                "MinderUsage",
                "STOPPED: Usage Access not granted"
            )
            return
        }

        lifecycleScope.launch {

            Log.d(
                "MinderUsage",
                "Coroutine STARTED"
            )

            val user =
                userRepository.getUser()

            Log.d(
                "MinderUsage",
                "User from DB = $user"
            )

            if (user == null) {
                Log.d(
                    "MinderUsage",
                    "STOPPED: User not found"
                )
                return@launch
            }

            val sessions =
                usageStatsService.getTodayUsageSessions()

            Log.d(
                "MinderUsage",
                "Sessions found = ${sessions.size}"
            )

            saveUsageSessionsUseCase(
                userId = user.userId,
                sessions = sessions
            )

            Log.d(
                "MinderUsage",
                "Sessions save FINISHED"
            )

            val calendar =
                Calendar.getInstance()

            calendar.set(
                Calendar.HOUR_OF_DAY,
                0
            )
            calendar.set(
                Calendar.MINUTE,
                0
            )
            calendar.set(
                Calendar.SECOND,
                0
            )
            calendar.set(
                Calendar.MILLISECOND,
                0
            )

            val dayStart =
                calendar.timeInMillis

            calendar.add(
                Calendar.DAY_OF_MONTH,
                1
            )

            val dayEnd =
                calendar.timeInMillis

            val restrictedApps =
                restrictedAppRepository.getAppsByUserId(
                    user.userId
                )

            Log.d(
                "MinderUsage",
                "Restricted apps = ${restrictedApps.size}"
            )

            restrictedApps
                .filter { it.isEnabled }
                .forEach { app ->

                    Log.d(
                        "MinderUsage",
                        "Updating appId=${app.id}, package=${app.packageName}"
                    )

                    try {

                        updateDailyUsageUseCase(
                            appId = app.id,
                            date = dayStart,
                            startTime = dayStart,
                            endTime = dayEnd
                        )

                        Log.d(
                            "MinderUsage",
                            "Daily usage SUCCESS: appId=${app.id}"
                        )

                    } catch (e: Exception) {

                        Log.e(
                            "MinderUsage",
                            "Daily usage FAILED: appId=${app.id}",
                            e
                        )
                    }
                }

            Log.d(
                "MinderUsage",
                "syncUsageData FINISHED"
            )
        }
    }
}