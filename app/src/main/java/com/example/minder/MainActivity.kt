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
import com.example.minder.domain.usecase.usage.SaveUsageSessionsUseCase
import com.example.minder.domain.usecase.usage.UpdateDailyUsageUseCase
import com.example.minder.features.onboarding.OnboardingScreen
import com.example.minder.features.questionnaire.QuestionnaireScreen
import com.example.minder.services.monitoring.AppMonitoringService
import com.example.minder.services.usage.UsagePermissionManager
import com.example.minder.services.usage.UsageStatsService
import com.example.minder.ui.theme.MinderTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var usagePermissionManager: UsagePermissionManager
    private lateinit var database: MinderDatabase
    private lateinit var usageStatsService: UsageStatsService
    private lateinit var saveUsageSessionsUseCase: SaveUsageSessionsUseCase
    private lateinit var updateDailyUsageUseCase: UpdateDailyUsageUseCase
    private lateinit var restrictedAppRepository: RestrictedAppRepositoryImpl

    // Lama - Detects the application currently in the foreground
    private lateinit var appMonitoringService: AppMonitoringService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Lama - Initializes the Usage Access permission manager
        usagePermissionManager =
            UsagePermissionManager(this)

        // Lama - Initializes the Minder Room database
        database =
            MinderDatabase.getDatabase(applicationContext)

        // Lama - Seeds the default challenges when the challenge table is empty
        lifecycleScope.launch {
            ChallengeSeeder.seedIfEmpty(
                database.challengeDao()
            )
        }

        // Lama - Initializes Android usage statistics
        usageStatsService =
            UsageStatsService(this)

        // Lama - Initializes foreground application monitoring
        appMonitoringService =
            AppMonitoringService(this)

        // Lama - Initializes the restricted application repository
        restrictedAppRepository =
            RestrictedAppRepositoryImpl(
                database.restrictedAppDao()
            )

        // Lama - Initializes the usage session repository
        val usageSessionRepository =
            UsageSessionRepositoryImpl(
                database.usageSessionDao()
            )

        // Lama - Initializes the daily usage repository
        val dailyUsageRepository =
            DailyUsageRepositoryImpl(
                database.dailyUsageDao()
            )

        // Lama - Initializes usage-session saving
        saveUsageSessionsUseCase =
            SaveUsageSessionsUseCase(
                restrictedAppRepository = restrictedAppRepository,
                usageSessionRepository = usageSessionRepository
            )

        // Lama - Initializes daily usage updating
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
                                    onNext = {
                                        // Integration continues here
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}