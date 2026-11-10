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
import com.example.minder.data.repository.UserSettingsRepositoryImpl
import com.example.minder.domain.model.UserSettings
import com.example.minder.domain.usecase.restrictedapp.SaveRestrictedAppsUseCase
import com.example.minder.domain.usecase.restrictedapp.SelectedApp
import com.example.minder.domain.usecase.settings.ValidateChallengeIntervalUseCase
//Ragahd-: For notifications to show to user
import android.widget.Toast
//Ragahd-: Import LaunchedEffect to restore saved apps when Home opens
import androidx.compose.runtime.LaunchedEffect
import com.example.minder.services.usage.HistoricalUsageDiagnostic

//Ragahd-: Historical usage data source and repositories.
import com.example.minder.data.datasource.HistoricalUsageDataSourceImpl
import com.example.minder.data.repository.HistoricalUsageRepositoryImpl
import com.example.minder.data.repository.UsageBaselineRepositoryImpl

//Ragahd-: Historical baseline use cases.
import com.example.minder.domain.usecase.usage.CalculateHistoricalBaselineUseCase
import com.example.minder.domain.usecase.usage.SaveHistoricalBaselinesUseCase
//LAMA-: Provides access to the active accessibility monitoring service.
import com.example.minder.services.monitoring.AppAccessibilityService
//Ragahd-: Daily, weekly, comparison, and saved time statistics use cases.
import com.example.minder.domain.usecase.usage.GetDailyUsageStatsUseCase
import com.example.minder.domain.usecase.usage.GetWeeklyUsageStatsUseCase
import com.example.minder.domain.usecase.usage.GetUsageComparisonUseCase
import com.example.minder.domain.usecase.usage.GetSavedTimeStatsUseCase
import com.example.minder.domain.usecase.challenge.GetChallengeSuccessRateUseCase
import com.example.minder.domain.usecase.intervention.GetInterventionStatisticsUseCase
import com.example.minder.domain.usecase.usage.GetAverageSessionDurationUseCase
import com.example.minder.domain.usecase.challenge.GetAttemptsAndChallengeTypesStatisticsUseCase
import com.example.minder.domain.usecase.usage.GetPeakUsageTimesUseCase
import com.example.minder.data.repository.InterventionRepositoryImpl
import com.example.minder.data.repository.ChallengeAttemptRepositoryImpl

//Ragahd-: ViewModel lifecycle management.
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//Ragahd-: Statistics and Progress ViewModels.
import com.example.minder.features.statistics.StatisticsViewModel
import com.example.minder.features.progress.ProgressViewModel

class MainActivity : ComponentActivity() {

    private lateinit var usagePermissionManager: UsagePermissionManager
    private lateinit var database: MinderDatabase
    private lateinit var usageStatsService: UsageStatsService
    private lateinit var saveUsageSessionsUseCase: SaveUsageSessionsUseCase
    private lateinit var updateDailyUsageUseCase: UpdateDailyUsageUseCase
    private lateinit var restrictedAppRepository: RestrictedAppRepositoryImpl
    private lateinit var userSettingsRepository: UserSettingsRepositoryImpl
    private lateinit var saveRestrictedAppsUseCase: SaveRestrictedAppsUseCase

    //Ragahd-: Handles saving fixed historical usage baselines.
    private lateinit var saveHistoricalBaselinesUseCase: SaveHistoricalBaselinesUseCase
    private lateinit var validateChallengeIntervalUseCase: ValidateChallengeIntervalUseCase
    private lateinit var userRepository: UserRepositoryImpl

    private lateinit var initializeLocalUserUseCase: InitializeLocalUserUseCase
    private lateinit var getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase
    private lateinit var completeOnboardingUseCase: CompleteOnboardingUseCase

    private var localUserInitialized = false
    //Ragahd-: Track whether startup initialization and splash have finished
    private var servicesInitialized = false
    private var splashFinished = false

    private lateinit var appMonitoringService: AppMonitoringService
    private lateinit var scoreQuestionnaireUseCase: ScoreQuestionnaireUseCase
    private lateinit var saveQuestionnaireResultUseCase: SaveQuestionnaireResultUseCase

    val appStateHandler = AppStateHandler()
    //Ragahd-: Provides daily usage statistics.
    private lateinit var getDailyUsageStatsUseCase: GetDailyUsageStatsUseCase

    //Ragahd-: Provides weekly usage statistics.
    private lateinit var getWeeklyUsageStatsUseCase: GetWeeklyUsageStatsUseCase

    //Ragahd-: Compares current and previous week usage.
    private lateinit var getUsageComparisonUseCase: GetUsageComparisonUseCase

    //Ragahd-: Calculates saved time statistics.
    private lateinit var getSavedTimeStatsUseCase: GetSavedTimeStatsUseCase

    //Ragahd-: Average session duration statistics
    private lateinit var getAverageSessionDurationUseCase: GetAverageSessionDurationUseCase

    //Ragahd-: Peak usage times statistics
    private lateinit var getPeakUsageTimesUseCase: GetPeakUsageTimesUseCase

    //Ragahd-: Intervention statistics
    private lateinit var getInterventionStatisticsUseCase: GetInterventionStatisticsUseCase

    //Ragahd-: Challenge success rate statistics
    private lateinit var getChallengeSuccessRateUseCase: GetChallengeSuccessRateUseCase

    //Ragahd-: Attempts and challenge types statistics
    private lateinit var getAttemptsAndChallengeTypesStatisticsUseCase: GetAttemptsAndChallengeTypesStatisticsUseCase

    //Ragahd-: Provides statistics data to the Statistics feature.
    private lateinit var statisticsViewModel: StatisticsViewModel

    //Ragahd-: Provides progress data to the Progress feature.
    private lateinit var progressViewModel: ProgressViewModel

    //Ragahd-: Navigate according to the saved onboarding state
    private fun navigateAfterSplash() {
        lifecycleScope.launch {
            try {
                val onboardingCompleted = getOnboardingCompletedUseCase()

                appStateHandler.navigateTo(
                    if (onboardingCompleted) {
                        AppScreen.Home
                    } else {
                        AppScreen.Onboarding
                    }
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to read onboarding state", e)
                appStateHandler.navigateTo(AppScreen.Onboarding)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinderTheme {
                val currentScreen by appStateHandler.currentScreen.collectAsState()

                // الاحتفاظ بالتطبيقات التي حددها المستخدم مع حدودها الزمنية
                var selectedAppsList by remember { mutableStateOf<List<SelectableApp>>(emptyList()) }


                var isEditingAppsFromHome by remember { mutableStateOf(false) }


                //Ragahd-: Restore selected apps from Room when Home opens
                LaunchedEffect(currentScreen) {
                    if (currentScreen is AppScreen.Home && ::restrictedAppRepository.isInitialized) {
                        try {
                            val user = userRepository.getUser()

                            if (user != null) {
                                val savedApps = restrictedAppRepository
                                    .getAppsByUserId(user.userId)
                                    .filter { it.isEnabled }

                                selectedAppsList = savedApps.map { app ->
                                    SelectableApp(
                                        id = app.packageName,
                                        name = app.appName,
                                        packageName = app.packageName,
                                        isSelected = true,
                                        dailyLimitMinutes = app.dailyLimit
                                    )
                                }

                                Log.d(
                                    "MainActivity",
                                    "Restored ${selectedAppsList.size} selected apps from Room"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Failed to restore selected apps", e)
                        }
                    }
                }

                when (currentScreen) {
                    //Ragahd-: Wait for initialization before reading onboarding state
                    is AppScreen.Splash -> {
                        SplashScreen(
                            onSplashTimeout = {
                                splashFinished = true

                                if (servicesInitialized) {
                                    navigateAfterSplash()
                                }
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

                            //Ragahd-: Restore saved apps only when editing from Home
                            initialSelectedApps = if (isEditingAppsFromHome) {
                                selectedAppsList
                            } else {
                                emptyList()
                            },
                             onBackClick = {
                               appStateHandler.navigateTo(AppScreen.Questionnaire)
                             },




                            onDoneClick = { selectedApps ->
                                lifecycleScope.launch {
                                    //Ragahd-: Make sure user have selected at least one app
                                    if (selectedApps.isEmpty()) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Please select at least one app.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }

                                    //Ragahd-: Exclude the apps does not have a proper daily limit
                                    val appsToSave = selectedApps.filter {
                                        (it.dailyLimitMinutes ?: 0) > 0
                                    }

                                    if (appsToSave.isEmpty()) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Please set a valid daily limit for your apps.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }

                                    //Ragahd-: bring user and store apps with his account
                                    val user = userRepository.getUser() ?: run {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Unable to load user data.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }

                                    //Ragahd-: Reconvert selected apps to be stored
                                    val appsToSaveAsDomain = appsToSave.map { app ->
                                        SelectedApp(
                                            appName = app.name,
                                            packageName = app.packageName,
                                            dailyLimit = app.dailyLimitMinutes!!
                                        )
                                    }

                                    //Ragahd-: save selected apps in Room Database
                                    saveRestrictedAppsUseCase(
                                        userId = user.userId,
                                        selectedApps = appsToSaveAsDomain
                                    )

                                    //Ragahd-: Generate historical baselines when Usage Access is available.
                                    if (usagePermissionManager.hasUsageAccess()) {
                                        try {
                                            val savedBaselineAppIds = saveHistoricalBaselinesUseCase(
                                                userId = user.userId
                                            )

                                            Log.d(
                                                "HistoricalBaseline",
                                                "Baselines saved for app IDs: $savedBaselineAppIds"
                                            )
                                        } catch (e: Exception) {
                                            //Ragahd-: Keep app selection working even if baseline generation fails.
                                            Log.e(
                                                "HistoricalBaseline",
                                                "Failed to generate historical baselines",
                                                e
                                            )
                                        }
                                    }

                                    //Ragahd-: Update the selected apps displayed on Home
                                    selectedAppsList = appsToSave

                                    //Ragahd-: Continue onboarding to challenge setup
                                    appStateHandler.navigateTo(AppScreen.ChallengeSetup)

                                }
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
                                lifecycleScope.launch {
                                    try {
                                        //Ragahd-: get local user before saving challenge sittings
                                        val user = userRepository.getUser()
                                        if (user == null) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Unable to load user data.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@launch
                                        }

                                        //Ragahd-: Making sure that the user has saved restricted apps
                                        val savedApps = restrictedAppRepository.getAppsByUserId(user.userId)
                                            .filter { it.isEnabled }

                                        if (savedApps.isEmpty()) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Please select at least one app.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            appStateHandler.navigateTo(AppScreen.AppSelection)
                                            return@launch
                                        }


                                       //Ragahd-: Validate every selected app before saving challenge settings
                                        val allAppsValid = savedApps.all { app ->
                                            intervalMinutes < app.dailyLimit
                                        }

                                        if (!allAppsValid) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Challenge interval must be less than every selected app's daily limit.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@launch
                                        }

                                      //Ragahd-: Create challenge settings or update existing settings after validation
                                        val existingSettings =
                                            userSettingsRepository.getSettingsByUserId(user.userId)

                                        if (existingSettings == null) {
                                            userSettingsRepository.saveSettings(
                                                UserSettings(
                                                    settingsId = 0,
                                                    userId = user.userId,
                                                    challengeDifficulty = "MEDIUM",
                                                    challengeInterval = intervalMinutes,
                                                    blockingEnabled = false,
                                                    usageAccessGranted = usagePermissionManager.hasUsageAccess()
                                                )
                                            )
                                        } else {
                                            userSettingsRepository.updateSettings(
                                                existingSettings.copy(
                                                    challengeInterval = intervalMinutes,
                                                    usageAccessGranted = usagePermissionManager.hasUsageAccess()
                                                )
                                            )
                                        }
                                        //Lama - Update the active moritoring service
                                        //after saving the user's selected challenge interval
                                        AppAccessibilityService.notifyChallengeIntervalChanged(
                                            intervalMinutes
                                        )
                                        Log.d(
                                            "MinderLimit",
                                            "Saved challenge interval: $intervalMinutes minutes"
                                        )

                                        if (!allAppsValid) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Challenge interval must be less than every selected app's daily limit.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@launch
                                        }

                                        //Ragahd-: finish setting user after making sure of every thing and saving data
                                        completeOnboardingUseCase()
                                        appStateHandler.navigateTo(AppScreen.Home)

                                    } catch (e: Exception) {
                                        //Ragahd-: show notification when there is an error
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Unable to save challenge settings. Please try again.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        Log.e("MainActivity", "Failed to save challenge settings", e)
                                    }
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

                //Ragahd-: Check whether historical usage access is available.
                val hasUsageAccess = usagePermissionManager.hasUsageAccess()

                Log.d(
                    "HistoricalDiagnostic",
                    "Usage Access granted: $hasUsageAccess"
                )

                if (hasUsageAccess) {
                    //Ragahd-: Run historical usage diagnostics on the IO dispatcher.
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        HistoricalUsageDiagnostic(this@MainActivity)
                            .inspectHistoricalUsage(14)
                    }
                } else {
                    Log.e(
                        "HistoricalDiagnostic",
                        "Historical diagnostic skipped because Usage Access is not granted."
                    )
                }

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
                userSettingsRepository = UserSettingsRepositoryImpl(database.userSettingsDao())

                //Ragahd-: Create historical usage data source.
                val historicalUsageDataSource =
                    HistoricalUsageDataSourceImpl(applicationContext)

//Ragahd-: Create historical usage repository.
                val historicalUsageRepository =
                    HistoricalUsageRepositoryImpl(historicalUsageDataSource)

//Ragahd-: Create usage baseline repository.
                val usageBaselineRepository =
                    UsageBaselineRepositoryImpl(database.usageBaselineDao())

//Ragahd-: Create historical baseline calculator.
                val calculateHistoricalBaselineUseCase =
                    CalculateHistoricalBaselineUseCase()

//Ragahd-: Create historical baseline saving use case.
                saveHistoricalBaselinesUseCase = SaveHistoricalBaselinesUseCase(
                    restrictedAppRepository = restrictedAppRepository,
                    historicalUsageRepository = historicalUsageRepository,
                    calculateHistoricalBaselineUseCase = calculateHistoricalBaselineUseCase,
                    usageBaselineRepository = usageBaselineRepository
                )

                saveRestrictedAppsUseCase = SaveRestrictedAppsUseCase(
                    restrictedAppRepository = restrictedAppRepository
                )

                validateChallengeIntervalUseCase = ValidateChallengeIntervalUseCase(
                    userSettingsRepository = userSettingsRepository,
                    restrictedAppRepository = restrictedAppRepository
                )
                val usageSessionRepository = UsageSessionRepositoryImpl(database.usageSessionDao())
                val dailyUsageRepository = DailyUsageRepositoryImpl(database.dailyUsageDao())
                //Ragahd-: Initialize intervention repository
                val interventionRepository = InterventionRepositoryImpl(
                    database.interventionDao()
                )

                //Ragahd-: Initialize challenge attempt repository
                val challengeAttemptRepository = ChallengeAttemptRepositoryImpl(
                    database.challengeAttemptDao()
                )

                //Ragahd-: Initialize daily usage statistics.
                getDailyUsageStatsUseCase = GetDailyUsageStatsUseCase(
                    restrictedAppRepository = restrictedAppRepository,
                    dailyUsageRepository = dailyUsageRepository
                )

                //Ragahd-: Initialize saved time statistics.
                getSavedTimeStatsUseCase = GetSavedTimeStatsUseCase(
                    restrictedAppRepository = restrictedAppRepository,
                    dailyUsageRepository = dailyUsageRepository,
                    usageBaselineRepository = usageBaselineRepository
                )

                //Ragahd-: Initialize weekly usage statistics.
                getWeeklyUsageStatsUseCase = GetWeeklyUsageStatsUseCase(
                    restrictedAppRepository = restrictedAppRepository,
                    dailyUsageRepository = dailyUsageRepository
                )

                //Ragahd-: Initialize weekly usage comparison.
                getUsageComparisonUseCase = GetUsageComparisonUseCase(
                    getWeeklyUsageStatsUseCase = getWeeklyUsageStatsUseCase
                )

                //Ragahd-: Initialize average session duration statistics
                getAverageSessionDurationUseCase = GetAverageSessionDurationUseCase(
                    restrictedAppRepository,
                    usageSessionRepository
                )

//Ragahd-: Initialize peak usage times statistics
                getPeakUsageTimesUseCase = GetPeakUsageTimesUseCase(
                    restrictedAppRepository,
                    usageSessionRepository
                )

//Ragahd-: Initialize intervention statistics
                getInterventionStatisticsUseCase = GetInterventionStatisticsUseCase(
                    restrictedAppRepository,
                    interventionRepository
                )

//Ragahd-: Initialize challenge success rate statistics
                getChallengeSuccessRateUseCase = GetChallengeSuccessRateUseCase(
                    restrictedAppRepository,
                    interventionRepository
                )

//Ragahd-: Initialize attempts and challenge types statistics
                getAttemptsAndChallengeTypesStatisticsUseCase =
                    GetAttemptsAndChallengeTypesStatisticsUseCase(
                        challengeAttemptRepository
                    )
                //Ragahd-: Create a factory to provide the required use cases to both ViewModels.
                val statisticsViewModelFactory = object : ViewModelProvider.Factory {

                    override fun <T : ViewModel> create(modelClass: Class<T>): T {

                        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return StatisticsViewModel(
                                getDailyUsageStatsUseCase = getDailyUsageStatsUseCase,
                                getWeeklyUsageStatsUseCase = getWeeklyUsageStatsUseCase,
                                getUsageComparisonUseCase = getUsageComparisonUseCase,
                                getSavedTimeStatsUseCase = getSavedTimeStatsUseCase,
                                getAverageSessionDurationUseCase = getAverageSessionDurationUseCase,
                                getPeakUsageTimesUseCase = getPeakUsageTimesUseCase
                            ) as T
                        }

                        if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return ProgressViewModel(
                                getInterventionStatisticsUseCase = getInterventionStatisticsUseCase,
                                getChallengeSuccessRateUseCase = getChallengeSuccessRateUseCase,
                                getAttemptsAndChallengeTypesStatisticsUseCase =
                                    getAttemptsAndChallengeTypesStatisticsUseCase
                            ) as T
                        }

                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                    }
                }

//Ragahd-: Initialize both ViewModels using the Activity ViewModelStore.
                statisticsViewModel = ViewModelProvider(
                    this@MainActivity,
                    statisticsViewModelFactory
                )[StatisticsViewModel::class.java]

                progressViewModel = ViewModelProvider(
                    this@MainActivity,
                    statisticsViewModelFactory
                )[ProgressViewModel::class.java]

                saveUsageSessionsUseCase = SaveUsageSessionsUseCase(
                    restrictedAppRepository = restrictedAppRepository,
                    usageSessionRepository = usageSessionRepository
                )

                updateDailyUsageUseCase = UpdateDailyUsageUseCase(
                    usageSessionRepository = usageSessionRepository,
                    dailyUsageRepository = dailyUsageRepository
                )

                //Ragahd-: Initialize the local user before loading statistics.
                initializeLocalUserUseCase()
                localUserInitialized = true

                //Ragahd-: Retrieve the initialized user and load statistics.
                val currentUser = userRepository.getUser()

                if (currentUser != null) {
                    statisticsViewModel.loadStatistics(
                        userId = currentUser.userId
                    )

                    progressViewModel.loadProgress(
                        userId = currentUser.userId
                    )
                } else {
                    Log.e("MainActivity", "Unable to load statistics: user not found.")
                }

                servicesInitialized = true

                if (splashFinished) {
                    navigateAfterSplash()
                }

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