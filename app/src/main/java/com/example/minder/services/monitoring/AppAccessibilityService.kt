package com.example.minder.services.monitoring

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.repository.ChallengeRepositoryImpl
import com.example.minder.data.repository.InterventionRepositoryImpl
import com.example.minder.data.repository.RestrictedAppRepositoryImpl
import com.example.minder.data.repository.UserRepositoryImpl
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UserRepository
import com.example.minder.domain.usecase.challenge.GenerateChallengeUseCase
import com.example.minder.domain.usecase.intervention.StartInterventionUseCase
import com.example.minder.features.challenge.ChallengeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.minder.data.repository.DailyUsageRepositoryImpl
import com.example.minder.domain.usecase.usage.CheckDailyLimitUseCase
import com.example.minder.features.blocking.BlockingActivity
import java.util.Calendar
import com.example.minder.domain.repository.InterventionRepository
import com.example.minder.data.repository.UserSettingsRepositoryImpl
import com.example.minder.services.usage.UsageStatsService
import android.view.accessibility.AccessibilityWindowInfo


class AppAccessibilityService : AccessibilityService() {

    companion object {

        // Lama - Holds the currently running accessibility service instance
        private var instance: AppAccessibilityService? = null

        // Lama - Notifies the service that the current challenge has ended
        fun notifyChallengeCompleted() {
            instance?.onChallengeCompleted()
        }
        // Lama - Notifies monitoring when the challenge screen is no longer visible
        fun notifyChallengeHidden(interventionId: Int) {
            instance?.onChallengeHidden(interventionId)
        }

        // Lama - Notifies the service when the blocking screen closes
        fun notifyBlockingClosed() {
            instance?.onBlockingClosed()
        }
    }

    // Lama - Clears the blocking screen state
    private fun onBlockingClosed() {

        isBlockingActive = false

        // Force a new daily limit check on the next app entry
        dailyLimitCheckedPackage = null

        Log.d(
            "MinderBlocking",
            "Blocking screen closed"
        )
    }

    // Lama - Tracks accumulated usage across restricted content
    private val usageLimitMonitor = UsageLimitMonitor()

    // Lama - Runs periodic usage checks on the main thread
    private val monitoringHandler =
        Handler(Looper.getMainLooper())

    // Lama - Coroutine scope used for database operations
    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Lama - Indicates whether periodic monitoring is running
    private var isMonitoring = false

    // Lama - Prevents another challenge from being created
    // while the current challenge is active
    private var isChallengeActive = false

    // Lama - Tracks the app that has an active challenge
    private var pendingChallengePackage: String? = null

    // Lama - Prevents overlapping database checks
    private var isCheckingPendingChallenge = false

    // Lama - Prevents opening the same challenge repeatedly
    private var displayedInterventionId: Int? = null

    // Lama - Accesses saved interventions
    private lateinit var interventionRepository: InterventionRepository

    // Lama - Repositories used to connect monitoring with stored app data
    private lateinit var userRepository: UserRepository
    private lateinit var restrictedAppRepository: RestrictedAppRepository

    // Lama - Creates the challenge and intervention when the limit is reached
    private lateinit var startInterventionUseCase: StartInterventionUseCase

    private lateinit var checkDailyLimitUseCase: CheckDailyLimitUseCase

    // Lama - Reads Android usage statistics for daily limit checks
    private lateinit var usageStatsService: UsageStatsService


    // Lama - Prevents overlapping daily limit checks
    private var isCheckingDailyLimit = false

    // Lama - Prevents opening the blocking screen repeatedly
    private var isBlockingActive = false

    // Lama - Tracks the app whose daily limit was already checked
    private var dailyLimitCheckedPackage: String? = null

    // Lama - Tracks the latest foreground application
    private var foregroundPackageName: String? = null

    // Lama - Tracks foreground app changes to identify outdated checks
    private var foregroundChangeVersion = 0

    // Lama - Stores enabled apps selected by the user
    private var selectedPackages: Set<String> = emptySet()

    // Lama - Indicates that the current app passed the pending challenge check
    private var monitoringApprovedPackage: String? = null

    // Lama - Indicates whether the latest selected-app list has finished loading
    private var areSelectedAppsLoaded = false

    // Lama - Tracks the latest selected-app loading request
    private var selectedAppsLoadVersion = 0

    // Lama - Tracks the last daily limit check
    private var lastDailyLimitCheckTime = 0L

    // Lama - Checks the daily limit every 5 seconds
    private val dailyLimitCheckIntervalMillis = 5000L


    private fun logSystemUiWindow(event: AccessibilityEvent) {
        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.packageName?.toString() != "com.android.systemui"
        ) return

        val matchingWindow = windows.firstOrNull {
            it.id == event.windowId
        }

        Log.d(
            "MinderSystemUI",
            "SystemUI window | " +
                    "class=${event.className} | " +
                    "type=${matchingWindow?.type} | " +
                    "title=${matchingWindow?.title} | " +
                    "rootClass=${matchingWindow?.root?.className} | " +
                    "focused=${matchingWindow?.isFocused} | " +
                    "active=${matchingWindow?.isActive}"
        )
    }

    // Lama - Starts monitoring only after all checks are completed
    private fun resumeApprovedMonitoring(packageName: String) {

        if (
            foregroundPackageName != packageName ||
            !areSelectedAppsLoaded ||
            packageName !in selectedPackages ||
            monitoringApprovedPackage != packageName ||
            dailyLimitCheckedPackage != packageName ||
            isCheckingDailyLimit ||
            isCheckingPendingChallenge ||
            isBlockingActive ||
            isChallengeActive
        ) {
            return
        }

        val shouldCountUsage =
            if (packageName == "com.google.android.youtube") {
                isYouTubeShorts()
            } else {
                true
            }

        if (shouldCountUsage) {

            // Lama - Avoids restarting monitoring for the same active app
            if (
                !isMonitoring ||
                usageLimitMonitor.getCurrentPackage() != packageName
            ) {
                usageLimitMonitor.startMonitoring(packageName)
                startLimitMonitoring()
            }

        } else {
            pauseLimitMonitoring()
        }
    }

    // Lama - Loads the user's selected and enabled apps from Room
    // Lama - Loads enabled apps selected by the user from Room
    private fun loadSelectedApps() {

        selectedAppsLoadVersion++
        val requestVersion = selectedAppsLoadVersion

        // Lama - Prevents monitoring while selected apps are being refreshed
        areSelectedAppsLoaded = false
        pauseLimitMonitoring()

        serviceScope.launch {
            try {
                val user = userRepository.getUser() ?: return@launch

                val apps = restrictedAppRepository.getAppsByUserId(
                    userId = user.userId
                )

                val enabledPackages = apps
                    .filter { it.isEnabled }
                    .map { it.packageName }
                    .toSet()

                withContext(Dispatchers.Main) {

                    // Lama - Ignores results from an older loading request
                    if (requestVersion != selectedAppsLoadVersion) {
                        return@withContext
                    }

                    selectedPackages = enabledPackages

                    // Lama - The latest selected-app list is ready
                    areSelectedAppsLoaded = true

                    // Lama - Stops monitoring if the current app is no longer selected
                    if (foregroundPackageName !in selectedPackages) {
                        monitoringApprovedPackage = null
                        pauseLimitMonitoring()
                    }

                    Log.d(
                        "MinderMonitoring",
                        "Selected apps loaded: $selectedPackages"
                    )

                    // Lama - Checks the current selected app after its list finishes loading
                    val currentPackage = foregroundPackageName

                    if (currentPackage != null) {
                        if (
                            currentPackage in selectedPackages &&
                            dailyLimitCheckedPackage != currentPackage
                        ) {
                            checkDailyLimitOnOpen(currentPackage)
                        } else {
                            resumeApprovedMonitoring(currentPackage)
                        }
                    }
                }

            } catch (exception: Exception) {
                Log.e(
                    "MinderMonitoring",
                    "Failed to load selected apps",
                    exception
                )
            }
        }
    }

    // Lama - Allows reopening the same unfinished challenge
    private fun onChallengeHidden(interventionId: Int) {

        if (displayedInterventionId != interventionId) {
            return
        }

        displayedInterventionId = null

        // Keep the challenge active until it is solved
        isChallengeActive = true

        Log.d(
            "MinderChallenge",
            "Challenge hidden | interventionId=$interventionId"
        )
    }

    // Lama - Checks the shared restricted-content usage every second
    private val monitoringRunnable = object : Runnable {

        override fun run() {

            if (!isMonitoring) {
                return
            }

            val packageName =
                usageLimitMonitor.getCurrentPackage()

            val elapsedSeconds =
                usageLimitMonitor.getElapsedSeconds()

            // Lama - Checks the daily limit periodically while the app is active
            val currentTime = System.currentTimeMillis()

            if (
                packageName != null &&
                !isCheckingDailyLimit &&
                !isBlockingActive &&
                !isChallengeActive &&
                currentTime - lastDailyLimitCheckTime >= dailyLimitCheckIntervalMillis
            ) {
                lastDailyLimitCheckTime = currentTime
                checkDailyLimitWhileUsing(packageName)
            }

            Log.d(
                "MinderLimit",
                "App: $packageName | Usage: $elapsedSeconds sec"
            )

            if (
                usageLimitMonitor.hasReachedLimit() &&
                !isCheckingDailyLimit &&
                !isBlockingActive &&
                !isChallengeActive
            ) {

                Log.d(
                    "MinderLimit",
                    "CHALLENGE INTERVAL REACHED for: $packageName"
                )

                // Lama - Prevents duplicate interventions caused by
                // repeated accessibility events
                isChallengeActive = true

                pauseLimitMonitoring()

                if (packageName != null) {
                    createInterventionAndOpenChallenge(packageName)
                } else {

                    Log.e(
                        "MinderChallenge",
                        "Cannot start intervention: package name is null"
                    )

                    isChallengeActive = false
                }

                return
            }

            monitoringHandler.postDelayed(
                this,
                1000L
            )
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Lama - Stores the active service instance so ChallengeActivity
        // can notify monitoring when the challenge is completed
        instance = this

        // Lama - Initializes the existing Minder database
        val database =
            MinderDatabase.getDatabase(applicationContext)

        // Lama - Gets the current local user
        userRepository =
            UserRepositoryImpl(
                database.userDao()
            )

        usageStatsService = UsageStatsService(applicationContext)

        val userSettingsRepository =
            UserSettingsRepositoryImpl(database.userSettingsDao())

        serviceScope.launch {
            try {
                val user = userRepository.getUser()

                if (user != null) {
                    val settings =
                        userSettingsRepository.getSettingsByUserId(user.userId)

                    if (settings != null) {
                        withContext(Dispatchers.Main) {
                            usageLimitMonitor.setChallengeInterval(
                                settings.challengeInterval
                            )

                            Log.d(
                                "MinderLimit",
                                "Challenge interval loaded: ${settings.challengeInterval} minutes"
                            )
                        }
                    }
                }
            } catch (exception: Exception) {
                Log.e(
                    "MinderLimit",
                    "Failed to load challenge interval",
                    exception
                )
            }
        }

        // Lama - Resolves package names to restricted applications
        restrictedAppRepository =
            RestrictedAppRepositoryImpl(
                database.restrictedAppDao()
            )


        val dailyUsageRepository =
            DailyUsageRepositoryImpl(
                database.dailyUsageDao()
            )

        checkDailyLimitUseCase =
            CheckDailyLimitUseCase(
                restrictedAppRepository = restrictedAppRepository,
                dailyUsageRepository = dailyUsageRepository
            )

        // Lama - Uses the existing challenge engine repositories
        val challengeRepository =
            ChallengeRepositoryImpl(
                database.challengeDao()
            )

        interventionRepository =
            InterventionRepositoryImpl(
                database.interventionDao()
            )

        // Lama - Uses the existing challenge generation logic
        val generateChallengeUseCase =
            GenerateChallengeUseCase(
                challengeRepository = challengeRepository
            )


        // Lama - Uses the existing intervention creation logic
        startInterventionUseCase =
            StartInterventionUseCase(
                generateChallengeUseCase = generateChallengeUseCase,
                interventionRepository = interventionRepository
            )

        // Lama - Starts loading selected apps after all monitoring dependencies are ready
        loadSelectedApps()

        Log.d(
            "MinderAccessibility",
            "Accessibility service connected"
        )
    }

    // Checks whether the event belongs to an input-method window,
    // such as a keyboard displayed over another application.
    private fun isInputMethodWindow(event: AccessibilityEvent): Boolean {
        val eventWindowId = event.windowId

        return windows.any { window ->
            window.id == eventWindowId &&
                    window.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD
        }
    }

    private fun handleForegroundPackage(packageName: String) {

        if (foregroundPackageName != packageName) {

            Log.d(
                "MinderForeground",
                "Foreground changed | from=$foregroundPackageName | to=$packageName"
            )

            foregroundPackageName = packageName
            foregroundChangeVersion++

            dailyLimitCheckedPackage = null
            monitoringApprovedPackage = null

            pauseLimitMonitoring()
            loadSelectedApps()
        }

        checkDailyLimitOnOpen(packageName)

        if (
            isChallengeActive ||
            isBlockingActive ||
            isCheckingDailyLimit ||
            isCheckingPendingChallenge ||
            monitoringApprovedPackage != packageName
        ) {
            return
        }

        resumeApprovedMonitoring(packageName)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {

            val activePackage =
                rootInActiveWindow?.packageName?.toString()

            if (
                activePackage != null &&
                activePackage in selectedPackages
            ) {
                Log.d(
                    "MinderForeground",
                    "Restoring monitoring from windows | active=$activePackage"
                )

                handleForegroundPackage(activePackage)
            }

            return
        }

        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName?.toString() ?: return

        logSystemUiWindow(event)

        // A keyboard window does not mean the user left the restricted app.
        if (
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            isInputMethodWindow(event)
        ) {
            Log.d(
                "MinderForeground",
                "Input method window ignored | foreground=$foregroundPackageName"
            )
            return
        }

        // Ignore the temporary Samsung volume panel.
        // It does not mean the user left the restricted app.
        if (
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            packageName == "com.android.systemui" &&
            event.className?.toString() ==
            "com.android.systemui.volume.view.standard.VolumePanelWindow"
        ) {
            Log.d(
                "MinderSystemUI",
                "Volume panel ignored | foreground=$foregroundPackageName"
            )
            return
        }


        // Lama - Ignores content changes from apps outside the foreground
        if (
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            packageName != foregroundPackageName
        ) {
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            handleForegroundPackage(packageName)
        } else {
            checkDailyLimitOnOpen(packageName)

            if (
                !isChallengeActive &&
                !isBlockingActive &&
                !isCheckingDailyLimit &&
                !isCheckingPendingChallenge &&
                monitoringApprovedPackage == packageName
            ) {
                resumeApprovedMonitoring(packageName)
            }
        }

        // Lama - Checks the daily limit for the detected app.
        // The function verifies that the app is selected and enabled in Room.
        checkDailyLimitOnOpen(packageName)

        // Lama - Waits for the daily limit check before monitoring
        // Lama - Waits until daily limit and pending challenge checks finish
        if (
            isChallengeActive ||
            isBlockingActive ||
            isCheckingDailyLimit ||
            isCheckingPendingChallenge ||
            monitoringApprovedPackage != packageName
        ) {
            return
        }

        // Lama - Monitors only approved selected apps
        resumeApprovedMonitoring(packageName)
    }

    // Lama - Checks whether today's Android usage has reached the app's daily limit
    private suspend fun hasReachedDailyLimit(
        packageName: String,
        dailyLimitMinutes: Int
    ): Boolean {

        val usedSeconds = usageStatsService.getTodayUsageSeconds(packageName)
        val limitSeconds = dailyLimitMinutes.toLong() * 60L

        Log.d(
            "MinderBlocking",
            "Android usage | package=$packageName | used=$usedSeconds sec | limit=$limitSeconds sec"
        )

        return usedSeconds >= limitSeconds
    }

    // Lama - Checks the daily limit during active app usage
    private fun checkDailyLimitWhileUsing(packageName: String) {

        if (isCheckingDailyLimit || isBlockingActive || isChallengeActive) {
            return
        }

        isCheckingDailyLimit = true

        serviceScope.launch {
            try {
                val user = userRepository.getUser() ?: return@launch

                val app = restrictedAppRepository.getAppByPackageName(
                    userId = user.userId,
                    packageName = packageName
                ) ?: return@launch

                if (!app.isEnabled) return@launch

                val reached = hasReachedDailyLimit(
                    packageName = packageName,
                    dailyLimitMinutes = app.dailyLimit
                )

                withContext(Dispatchers.Main) {

                    if (
                        reached &&
                        !isBlockingActive &&
                        !isChallengeActive &&
                        usageLimitMonitor.getCurrentPackage() == packageName
                    ) {
                        isBlockingActive = true
                        pauseLimitMonitoring()

                        Log.d(
                            "MinderBlocking",
                            "DAILY LIMIT REACHED DURING USAGE | appId=${app.id}"
                        )

                        showBlocking(
                            appId = app.id,
                            packageName = packageName
                        )
                    }
                }

            } catch (exception: Exception) {

                Log.e(
                    "MinderBlocking",
                    "Periodic daily limit check failed",
                    exception
                )

            } finally {

                withContext(Dispatchers.Main) {

                    isCheckingDailyLimit = false

                    // Lama - Checks the new foreground app after the periodic check
                    val currentPackage = foregroundPackageName

                    // Lama - Rechecks the current app if its daily limit check is still pending
                    if (
                        currentPackage != null &&
                        dailyLimitCheckedPackage != currentPackage &&
                        !isBlockingActive &&
                        !isChallengeActive
                    ) {
                        checkDailyLimitOnOpen(currentPackage)
                    }
                }
            }
        }
    }
    // Lama - Checks the daily limit before allowing monitoring
    private fun checkDailyLimitOnOpen(packageName: String) {

        if (
            !areSelectedAppsLoaded ||
            packageName !in selectedPackages ||
            isCheckingDailyLimit ||
            isBlockingActive
        ) return

        // Recheck an unfinished challenge when entering a restricted app.
        if (isChallengeActive) {
            if (displayedInterventionId == null && !isCheckingPendingChallenge) {
                checkPendingChallenge(packageName)
            }
            return
        }

        if (dailyLimitCheckedPackage == packageName) return

        isCheckingDailyLimit = true

        // Lama - Saves the foreground version when this daily check starts
        val checkVersion = foregroundChangeVersion

        pauseLimitMonitoring()

        serviceScope.launch {
            try {
                val user = userRepository.getUser() ?: return@launch

                val app = restrictedAppRepository.getAppByPackageName(
                    userId = user.userId,
                    packageName = packageName
                ) ?: return@launch

                if (!app.isEnabled) return@launch

                val reached = hasReachedDailyLimit(
                    packageName = packageName,
                    dailyLimitMinutes = app.dailyLimit
                )

                withContext(Dispatchers.Main) {

                    // Lama - Ignores outdated checks even if the user returns to the same app
                    if (
                        foregroundPackageName != packageName ||
                        checkVersion != foregroundChangeVersion
                    ) {
                        return@withContext
                    }

                    dailyLimitCheckedPackage = packageName

                    if (reached) {

                        // Lama - Blocks the app when its daily limit is reached
                        isBlockingActive = true
                        pauseLimitMonitoring()

                        Log.d(
                            "MinderBlocking",
                            "DAILY LIMIT REACHED ON OPEN | appId=${app.id}"
                        )

                        showBlocking(
                            appId = app.id,
                            packageName = packageName
                        )

                    } else {

                        // Lama - Checks pending challenges before resuming monitoring
                        Log.d(
                            "MinderBlocking",
                            "Daily limit not reached | Checking pending challenge"
                        )

                        checkPendingChallenge(packageName)
                    }
                }

            } catch (exception: Exception) {

                Log.e(
                    "MinderBlocking",
                    "Daily limit check failed",
                    exception
                )

            } finally {
                withContext(Dispatchers.Main) {

                    isCheckingDailyLimit = false

                    // Lama - Checks the new foreground app if the user switched apps
                    val currentPackage = foregroundPackageName

                    if (
                        currentPackage != null &&
                        (
                                currentPackage != packageName ||
                                        checkVersion != foregroundChangeVersion
                                ) &&
                        !isBlockingActive &&
                        !isChallengeActive
                    ) {
                        // Lama - Rechecks the current app after an outdated daily check
                        checkDailyLimitOnOpen(currentPackage)
                    } else if (
                        !isCheckingPendingChallenge &&
                        monitoringApprovedPackage == packageName
                    ) {
                        // Lama - Resumes monitoring after both checks are completed
                        resumeApprovedMonitoring(packageName)
                    }
                }
            }
        }
    }
    // Lama - Checks for unfinished challenges before resuming monitoring
    private fun checkPendingChallenge(packageName: String) {

        if (isCheckingPendingChallenge || isBlockingActive) return

        isCheckingPendingChallenge = true

        // Lama - Saves the foreground version when the pending challenge check starts
        val checkVersion = foregroundChangeVersion

        serviceScope.launch {
            try {
                val user = userRepository.getUser()
                    ?: return@launch

                val restrictedApp =
                    restrictedAppRepository.getAppByPackageName(
                        userId = user.userId,
                        packageName = packageName
                    ) ?: return@launch

                if (!restrictedApp.isEnabled) return@launch

                val pending =
                    interventionRepository.getPendingInterventionForUser(
                        user.userId
                    )

                withContext(Dispatchers.Main) {

                    // Lama - Ignores pending challenge results from an older app session
                    if (
                        foregroundPackageName != packageName ||
                        checkVersion != foregroundChangeVersion ||
                        isBlockingActive
                    ) {
                        return@withContext
                    }

                    if (pending != null) {

                        // Lama - Reopens the unfinished challenge
                        pauseLimitMonitoring()

                        showChallenge(
                            appId = pending.appId,
                            interventionId = pending.id,
                            packageName = packageName
                        )

                    } else if (!isChallengeActive) {

                        // Lama - Approves monitoring after confirming no pending challenge
                        monitoringApprovedPackage = packageName

                        Log.d(
                            "MinderChallenge",
                            "Monitoring approved | package=$packageName"
                        )
                    }
                }

            } catch (exception: Exception) {

                Log.e(
                    "MinderChallenge",
                    "Failed to check pending intervention",
                    exception
                )

            }finally {
                withContext(Dispatchers.Main) {

                    isCheckingPendingChallenge = false

                    val currentPackage = foregroundPackageName

                    if (
                        currentPackage != null &&
                        (
                                currentPackage != packageName ||
                                        checkVersion != foregroundChangeVersion
                                ) &&
                        !isCheckingDailyLimit &&
                        !isBlockingActive &&
                        !isChallengeActive
                    ) {
                        // Lama - Restarts the correct check after an outdated pending challenge check
                        if (dailyLimitCheckedPackage == currentPackage) {
                            checkPendingChallenge(currentPackage)
                        } else {
                            checkDailyLimitOnOpen(currentPackage)
                        }
                    } else if (
                        !isCheckingDailyLimit &&
                        monitoringApprovedPackage == packageName
                    ) {
                        // Lama - Resumes monitoring after all checks finish
                        resumeApprovedMonitoring(packageName)
                    }
                }
            }
        }
    }

    // Lama - Connects the monitoring layer with the existing
    // user, restricted-app, challenge, and intervention layers
    private fun createInterventionAndOpenChallenge(
        packageName: String
    ) {

        serviceScope.launch {

            try {

                // Lama - Gets the existing local user
                val user =
                    userRepository.getUser()

                if (user == null) {

                    Log.e(
                        "MinderChallenge",
                        "Cannot start intervention: no local user found"
                    )

                    resetAfterInterventionFailure()
                    return@launch
                }

                // Lama - Finds the selected restricted app
                // using the detected Android package name
                val restrictedApp =
                    restrictedAppRepository.getAppByPackageName(
                        userId = user.userId,
                        packageName = packageName
                    )

                if (restrictedApp == null) {

                    Log.e(
                        "MinderChallenge",
                        "Cannot start intervention: $packageName is not stored as a restricted app"
                    )

                    resetAfterInterventionFailure()
                    return@launch
                }

                Log.d(
                    "MinderChallenge",
                    "Restricted app found | appId=${restrictedApp.id} | package=$packageName"
                )

                val existingIntervention =
                    interventionRepository.getPendingInterventionForUser(
                        user.userId
                    )

                if (existingIntervention != null) {

                    withContext(Dispatchers.Main) {

                        pendingChallengePackage = packageName

                        showChallenge(
                            appId = existingIntervention.appId,
                            interventionId = existingIntervention.id,
                            packageName = packageName
                        )
                    }

                    return@launch
                }

                // Lama - Checks actual Android usage before creating a challenge
                val dailyLimitReached = hasReachedDailyLimit(
                    packageName = packageName,
                    dailyLimitMinutes = restrictedApp.dailyLimit
                )

                Log.d(
                    "MinderBlocking",
                    "Daily limit check | appId=${restrictedApp.id} | reached=$dailyLimitReached"
                )

                if (dailyLimitReached) {

                    Log.d(
                        "MinderBlocking",
                        "DAILY LIMIT REACHED | Opening BlockingActivity"
                    )

                    withContext(Dispatchers.Main) {
                        isBlockingActive = true
                        isChallengeActive = false

                        showBlocking(
                            appId = restrictedApp.id,
                            packageName = packageName
                        )
                    }

                    return@launch
                }

                // Lama - Creates a real challenge and intervention
                // using the existing challenge engine
                val result =
                    startInterventionUseCase(
                        appId = restrictedApp.id,
                        difficulty = "EASY"
                    )

                if (result == null) {

                    Log.e(
                        "MinderChallenge",
                        "Cannot start intervention: no challenge available"
                    )

                    resetAfterInterventionFailure()
                    return@launch
                }

                Log.d(
                    "MinderChallenge",
                    "Intervention created | interventionId=${result.interventionId} | challengeId=${result.challenge.id}"
                )

                // Lama - Opens the real challenge screen
                withContext(Dispatchers.Main) {

                    showChallenge(
                        appId = restrictedApp.id,
                        interventionId = result.interventionId,
                        packageName = packageName
                    )
                }

            } catch (exception: Exception) {

                Log.e(
                    "MinderChallenge",
                    "Failed to create intervention",
                    exception
                )

                resetAfterInterventionFailure()
            }
        }
    }
    private fun showBlocking(
        appId: Int,
        packageName: String
    ) {

        val intent =
            Intent(
                this,
                BlockingActivity::class.java
            ).apply {

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                putExtra(
                    BlockingActivity.EXTRA_APP_ID,
                    appId
                )

                putExtra(
                    BlockingActivity.EXTRA_INTERVENTION_ID,
                    -1
                )

                putExtra(
                    BlockingActivity.EXTRA_PACKAGE_NAME,
                    packageName
                )
            }

        startActivity(intent)
    }
    private fun showChallenge(
        appId: Int,
        interventionId: Int,
        packageName: String
    ) {

        // Lama - Prevents duplicate challenge screens
        if (displayedInterventionId == interventionId) {
            Log.d(
                "MinderChallenge",
                "Challenge already displayed: $interventionId"
            )
            return
        }

        displayedInterventionId = interventionId
        pendingChallengePackage = packageName
        isChallengeActive = true

        Log.d(
            "MinderChallenge",
            "Opening ChallengeActivity | interventionId=$interventionId"
        )

        val intent = Intent(
            this,
            ChallengeActivity::class.java
        ).apply {

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            putExtra(
                ChallengeActivity.EXTRA_APP_ID,
                appId
            )

            putExtra(
                ChallengeActivity.EXTRA_INTERVENTION_ID,
                interventionId
            )

            putExtra(
                ChallengeActivity.EXTRA_PACKAGE_NAME,
                packageName
            )
        }

        startActivity(intent)
    }

    // Lama - Restores monitoring if intervention creation fails
    private suspend fun resetAfterInterventionFailure() {

        withContext(Dispatchers.Main) {

            isChallengeActive = false


            // Lama - Resets the completed interval so the
            // service does not immediately trigger again
            usageLimitMonitor.resetMonitoring()
        }
    }

    // Lama - Unlocks monitoring after the user successfully
// completes the current challenge
    private fun onChallengeCompleted() {

        Log.d(
            "MinderChallenge",
            "Challenge completed - monitoring unlocked"
        )

        // Lama - Allows accessibility events to be processed again
        isChallengeActive = false

        pendingChallengePackage = null

        displayedInterventionId = null

        // Lama - Starts the next shared usage interval from zero
        usageLimitMonitor.resetMonitoring()

        Log.d(
            "MinderLimit",
            "New challenge monitoring cycle is ready"
        )
    }

    // Lama - Starts periodic usage-limit checking
    private fun startLimitMonitoring() {

        if (isMonitoring) {
            return
        }

        isMonitoring = true

        Log.d(
            "MinderLimit",
            "Periodic monitoring STARTED"
        )

        monitoringHandler.post(
            monitoringRunnable
        )
    }

    // Lama - Pauses monitoring and preserves accumulated usage
    private fun pauseLimitMonitoring() {

        if (isMonitoring) {

            Log.d(
                "MinderLimit",
                "Periodic monitoring PAUSED at ${usageLimitMonitor.getElapsedSeconds()} sec"
            )

            isMonitoring = false

            monitoringHandler.removeCallbacks(
                monitoringRunnable
            )
        }

        // Lama - Always pauses the shared usage timer
        usageLimitMonitor.pauseMonitoring()
    }

    // Lama - Checks whether the current YouTube screen is Shorts
    private fun isYouTubeShorts(): Boolean {

        val rootNode =
            rootInActiveWindow ?: return false

        val shortsDetected =
            containsShortsIndicator(rootNode)

        Log.d(
            "MinderYouTube",
            "YouTube Shorts detected: $shortsDetected"
        )

        return shortsDetected
    }

    // Lama - Searches the YouTube accessibility tree
    // for the Shorts interface
    private fun containsShortsIndicator(
        node: AccessibilityNodeInfo
    ): Boolean {

        val text =
            node.text?.toString()?.lowercase() ?: ""

        val description =
            node.contentDescription?.toString()?.lowercase() ?: ""

        if (
            text == "shorts" ||
            description == "shorts"
        ) {
            return true
        }

        for (index in 0 until node.childCount) {

            val child =
                node.getChild(index) ?: continue

            if (containsShortsIndicator(child)) {
                return true
            }
        }

        return false
    }

    override fun onInterrupt() {

        pauseLimitMonitoring()

        Log.d(
            "MinderAccessibility",
            "Accessibility service interrupted"
        )
    }

    override fun onDestroy() {

        // Lama - Stops monitoring and preserves accumulated usage before service shutdown
        pauseLimitMonitoring()

        monitoringHandler.removeCallbacks(
            monitoringRunnable
        )

        // Lama - Removes the stored service instance
        instance = null

        serviceScope.cancel()

        super.onDestroy()
    }

}