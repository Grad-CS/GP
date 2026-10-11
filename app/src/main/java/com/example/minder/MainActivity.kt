package com.example.minder

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.minder.core.navigation.AppScreen
import com.example.minder.core.navigation.AppStateHandler
import com.example.minder.data.local.database.ChallengeSeeder
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.preferences.OnboardingPreferencesDataStore
import com.example.minder.data.repository.DailyUsageRepositoryImpl
import com.example.minder.data.repository.OnboardingStateRepositoryImpl
import com.example.minder.data.repository.RestrictedAppRepositoryImpl
import com.example.minder.data.repository.UsageSessionRepositoryImpl
import com.example.minder.data.repository.UserRepositoryImpl
import com.example.minder.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.example.minder.domain.usecase.onboarding.GetOnboardingCompletedUseCase
import com.example.minder.domain.usecase.questionnaire.SaveQuestionnaireResultUseCase
import com.example.minder.domain.usecase.questionnaire.ScoreQuestionnaireUseCase
import com.example.minder.domain.usecase.usage.SaveUsageSessionsUseCase
import com.example.minder.domain.usecase.usage.UpdateDailyUsageUseCase
import com.example.minder.domain.usecase.user.CreateUserUseCase
import com.example.minder.domain.usecase.user.InitializeLocalUserUseCase
import com.example.minder.features.app_selection.AppSelectionScreen
import com.example.minder.features.app_selection.SelectableApp
import com.example.minder.features.home.HomeScreen
import com.example.minder.features.onboarding.SplashScreen
import com.example.minder.features.onboarding.WelcomeScreen
import com.example.minder.features.permissions.PermissionsScreen
import com.example.minder.features.questionnaire.QuestionnaireScreen
import com.example.minder.features.setup.ChallengeSetupScreen
import com.example.minder.services.monitoring.AppMonitoringService
import com.example.minder.services.usage.UsagePermissionManager
import com.example.minder.services.usage.UsageStatsService
import com.example.minder.ui.theme.MinderTheme
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var usagePermissionManager: UsagePermissionManager
    private lateinit var database: MinderDatabase
    private lateinit var usageStatsService: UsageStatsService
    private lateinit var saveUsageSessionsUseCase: SaveUsageSessionsUseCase
    private lateinit var updateDailyUsageUseCase: UpdateDailyUsageUseCase
    private lateinit var restrictedAppRepository: RestrictedAppRepositoryImpl
    private lateinit var userRepository: UserRepositoryImpl

    private lateinit var initializeLocalUserUseCase: InitializeLocalUserUseCase
    private lateinit var getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase
    private lateinit var completeOnboardingUseCase: CompleteOnboardingUseCase

    private var localUserInitialized = false

    private lateinit var appMonitoringService: AppMonitoringService
    private lateinit var scoreQuestionnaireUseCase: ScoreQuestionnaireUseCase
    private lateinit var saveQuestionnaireResultUseCase: SaveQuestionnaireResultUseCase

    val appStateHandler = AppStateHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinderTheme {
                val currentScreen by appStateHandler.currentScreen.collectAsState()

                // الاحتفاظ بالتطبيقات التي حددها المستخدم مع حدودها الزمنية
                var selectedAppsList by remember { mutableStateOf<List<SelectableApp>>(emptyList()) }

                when (currentScreen) {
                    is AppScreen.Splash -> {
                        SplashScreen(
                            onSplashTimeout = {
                                appStateHandler.navigateTo(AppScreen.Onboarding)
                            }
                        )
                    }

                    is AppScreen.Permissions -> {
                        PermissionsScreen(
                            onBackClick = {
                                appStateHandler.navigateTo(AppScreen.Onboarding)
                            },
                            onNextClick = {
                                // بعد الأذونات، ننتقل إلى شاشة الأسئلة
                                appStateHandler.navigateTo(AppScreen.Questionnaire)
                            }
                        )
                    }

                    is AppScreen.Questionnaire -> {
                        QuestionnaireScreen(
                            onNext = { answers ->
                                Log.d("MinderQuestionnaire", "Questionnaire answers = $answers")
                                lifecycleScope.launch {
                                    val score = scoreQuestionnaireUseCase(answers = answers)
                                    saveQuestionnaireResultUseCase(score = score)

                                    // الانتقال لشاشة اختيار التطبيقات
                                    appStateHandler.navigateTo(AppScreen.AppSelection)
                                }
                            }
                        )
                    }

                    is AppScreen.AppSelection -> {
                        AppSelectionScreen(
                            onBackClick = {
                                appStateHandler.navigateTo(AppScreen.Questionnaire)
                            },
                            onDoneClick = { selectedApps ->
                                Log.d("MinderAppSelection", "Selected apps count = ${selectedApps.size}")
                                selectedAppsList = selectedApps
                                appStateHandler.navigateTo(AppScreen.ChallengeSetup)
                            }
                        )
                    }

                    is AppScreen.ChallengeSetup -> {
                        // حساب الحد الأدنى للحدود اليومية للتطبيقات المختارة لتمريره كقيمة للتحقق
                        val calculatedLimit = selectedAppsList.minOfOrNull { it.dailyLimitMinutes ?: Int.MAX_VALUE } ?: 60

                        ChallengeSetupScreen(
                            dailyLimitMinutes = calculatedLimit, // تمرير القيمة هنا لحل الخطأ
                            onBackClick = {
                                appStateHandler.navigateTo(screen = AppScreen.AppSelection)
                            },
                            onNextClick = { intervalMinutes ->
                                Log.d("MinderSetup", "Challenge interval = $intervalMinutes mins")
                                lifecycleScope.launch {
                                    completeOnboardingUseCase()
                                    appStateHandler.navigateTo(screen = AppScreen.Home)
                                }
                            }
                        )
                    }

                    is AppScreen.Home -> {
                        HomeScreen(
                            selectedApps = selectedAppsList,
                            onSettingsClick = {
                                // التوجه لصفحة الإعدادات
                            },
                            onTabSelected = { tabIndex ->
                                Log.d("MinderHome", "Tab index clicked: $tabIndex")
                            }
                        )
                    }

                    else -> {
                        WelcomeScreen(
                            onGetStartedClick = {
                                // عند الضغط على البدء، نتوجه لشاشة الأذونات أولاً
                                appStateHandler.navigateTo(AppScreen.Permissions)
                            }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            try {
                usagePermissionManager = UsagePermissionManager(this@MainActivity)
                database = MinderDatabase.getDatabase(applicationContext)

                ChallengeSeeder.seedIfEmpty(database.challengeDao())

                usageStatsService = UsageStatsService(this@MainActivity)
                appMonitoringService = AppMonitoringService(this@MainActivity)
                userRepository = UserRepositoryImpl(database.userDao())

                val createUserUseCase = CreateUserUseCase(userRepository = userRepository)
                initializeLocalUserUseCase = InitializeLocalUserUseCase(
                    userRepository = userRepository,
                    createUserUseCase = createUserUseCase
                )

                val onboardingDataStore = OnboardingPreferencesDataStore(applicationContext)
                val onboardingStateRepository = OnboardingStateRepositoryImpl(onboardingDataStore)

                getOnboardingCompletedUseCase = GetOnboardingCompletedUseCase(repository = onboardingStateRepository)
                completeOnboardingUseCase = CompleteOnboardingUseCase(repository = onboardingStateRepository)
                scoreQuestionnaireUseCase = ScoreQuestionnaireUseCase()
                saveQuestionnaireResultUseCase = SaveQuestionnaireResultUseCase(repository = onboardingStateRepository)

                restrictedAppRepository = RestrictedAppRepositoryImpl(database.restrictedAppDao())
                val usageSessionRepository = UsageSessionRepositoryImpl(database.usageSessionDao())
                val dailyUsageRepository = DailyUsageRepositoryImpl(database.dailyUsageDao())

                saveUsageSessionsUseCase = SaveUsageSessionsUseCase(
                    restrictedAppRepository = restrictedAppRepository,
                    usageSessionRepository = usageSessionRepository
                )

                updateDailyUsageUseCase = UpdateDailyUsageUseCase(
                    usageSessionRepository = usageSessionRepository,
                    dailyUsageRepository = dailyUsageRepository
                )

                initializeLocalUserUseCase()
                localUserInitialized = true
                syncUsageData()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing services", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::usagePermissionManager.isInitialized) return
        if (!localUserInitialized) return
        syncUsageData()
    }

    private fun syncUsageData() {
        val hasUsageAccess = usagePermissionManager.hasUsageAccess()
        if (!hasUsageAccess) return

        lifecycleScope.launch {
            val user = userRepository.getUser() ?: return@launch
            val sessions = usageStatsService.getTodayUsageSessions()
            saveUsageSessionsUseCase(userId = user.userId, sessions = sessions)

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val dayEnd = calendar.timeInMillis

            val restrictedApps = restrictedAppRepository.getAppsByUserId(user.userId)
            restrictedApps.filter { it.isEnabled }.forEach { app ->
                try {
                    updateDailyUsageUseCase(
                        appId = app.id,
                        date = dayStart,
                        startTime = dayStart,
                        endTime = dayEnd
                    )
                } catch (e: Exception) {
                    Log.e("MinderUsage", "Daily usage FAILED: appId=${app.id}", e)
                }
            }
        }
    }
}