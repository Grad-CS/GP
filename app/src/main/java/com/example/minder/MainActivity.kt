package com.example.minder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.repository.DailyUsageRepositoryImpl
import com.example.minder.data.repository.RestrictedAppRepositoryImpl
import com.example.minder.data.repository.UsageSessionRepositoryImpl
import com.example.minder.domain.usecase.usage.SaveUsageSessionsUseCase
import com.example.minder.domain.usecase.usage.UpdateDailyUsageUseCase
import com.example.minder.services.usage.UsagePermissionManager
import com.example.minder.services.usage.UsageStatsService
import com.example.minder.features.onboarding.OnboardingScreen
import com.example.minder.features.questionnaire.QuestionnaireScreen
import com.example.minder.ui.theme.MinderTheme
import com.example.minder.data.repository.ChallengeRepositoryImpl
import com.example.minder.data.repository.InterventionRepositoryImpl
import com.example.minder.domain.usecase.challenge.GenerateChallengeUseCase
import com.example.minder.domain.usecase.intervention.StartInterventionUseCase

class MainActivity : ComponentActivity() {

    private lateinit var usagePermissionManager: UsagePermissionManager
    private lateinit var database: MinderDatabase
    private lateinit var usageStatsService: UsageStatsService
    private lateinit var saveUsageSessionsUseCase: SaveUsageSessionsUseCase
    private lateinit var updateDailyUsageUseCase: UpdateDailyUsageUseCase
    private lateinit var restrictedAppRepository: RestrictedAppRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        usagePermissionManager = UsagePermissionManager(this)

        database = MinderDatabase.getDatabase(applicationContext)

        usageStatsService = UsageStatsService(this)

        restrictedAppRepository =
            RestrictedAppRepositoryImpl(
                database.restrictedAppDao()
            )

        val usageSessionRepository =
            UsageSessionRepositoryImpl(
                database.usageSessionDao()
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
                challengeRepository = challengeRepository
            )

        val startInterventionUseCase =
            StartInterventionUseCase(
                generateChallengeUseCase = generateChallengeUseCase,
                interventionRepository = interventionRepository
            )

        saveUsageSessionsUseCase =
            SaveUsageSessionsUseCase(
                restrictedAppRepository = restrictedAppRepository,
                usageSessionRepository = usageSessionRepository
            )

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