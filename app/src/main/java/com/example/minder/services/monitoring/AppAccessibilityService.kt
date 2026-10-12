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

    // Lama - Tracks the last daily limit check
    private var lastDailyLimitCheckTime = 0L

    // Lama - Checks the daily limit every 5 seconds
    private val dailyLimitCheckIntervalMillis = 5000L

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

        Log.d(
            "MinderAccessibility",
            "Accessibility service connected"
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName?.toString() ?: return

        // Lama - Detects when the foreground application changes
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            if (foregroundPackageName != packageName) {

                foregroundPackageName = packageName

                // Check the daily limit again when the user returns to an app
                dailyLimitCheckedPackage = null
            }
        }


        // Lama - Checks for an unfinished challenge before monitoring usage
        if (packageName == "com.zhiliaoapp.musically" ||
            packageName == "com.instagram.android" ||
            packageName == "com.google.android.youtube"
        ) {
            // Lama - Checks the daily usage limit first
            checkDailyLimitOnOpen(packageName)

            // Lama - Checks for an unfinished challenge
            checkPendingChallenge(packageName)
        }

        // Lama - Waits for the daily limit check before monitoring
        if (
            isChallengeActive ||
            isBlockingActive ||
            isCheckingDailyLimit
        ) {
            return
        }

        // Lama - Determines whether the current content
        // contributes to the shared restricted-content timer
        val shouldCountUsage = when (packageName) {

            "com.zhiliaoapp.musically" -> true

            "com.instagram.android" -> true

            // Lama - Only YouTube Shorts contributes to the timer
            "com.google.android.youtube" -> {

                val isShorts =
                    isYouTubeShorts()

                Log.d(
                    "MinderYouTube",
                    "Package: $packageName | Shorts: $isShorts"
                )

                isShorts
            }

            else -> false
        }

        Log.d(
            "MinderAccessibility",
            "Foreground app: $packageName | Count usage: $shouldCountUsage"
        )

        if (shouldCountUsage) {

            // Lama - Continues the same shared interval even
            // when the user switches between restricted apps
            usageLimitMonitor.startMonitoring(packageName)

            startLimitMonitoring()

        } else {

            // Lama - Pauses the shared interval without resetting it
            pauseLimitMonitoring()
        }
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
                }
            }
        }
    }
    // Lama - Checks the daily limit before allowing monitoring
    private fun checkDailyLimitOnOpen(packageName: String) {

        if (
            isCheckingDailyLimit ||
            isBlockingActive ||
            isChallengeActive ||
            dailyLimitCheckedPackage == packageName
        ) return

        isCheckingDailyLimit = true

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

                    // Ignore the result if the user switched apps
                    if (foregroundPackageName != packageName) {
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
                }
            }
        }
    }
    // Lama - Checks for unfinished challenges before resuming monitoring
    private fun checkPendingChallenge(packageName: String) {

        if (isCheckingPendingChallenge || isBlockingActive) return

        isCheckingPendingChallenge = true

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

                    // Lama - Ignores results from an app no longer in foreground
                    if (foregroundPackageName != packageName ||
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

                        // Lama - Resumes the shared challenge timer
                        usageLimitMonitor.startMonitoring(packageName)
                        startLimitMonitoring()

                        Log.d(
                            "MinderLimit",
                            "Monitoring resumed | package=$packageName"
                        )
                    }
                }

            } catch (exception: Exception) {

                Log.e(
                    "MinderChallenge",
                    "Failed to check pending intervention",
                    exception
                )

            } finally {

                withContext(Dispatchers.Main) {
                    isCheckingPendingChallenge = false
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

    // Lama - Pauses monitoring without deleting accumulated usage
    private fun pauseLimitMonitoring() {

        if (!isMonitoring) {
            return
        }

        Log.d(
            "MinderLimit",
            "Periodic monitoring PAUSED at ${usageLimitMonitor.getElapsedSeconds()} sec"
        )

        isMonitoring = false

        monitoringHandler.removeCallbacks(
            monitoringRunnable
        )

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

        monitoringHandler.removeCallbacks(
            monitoringRunnable
        )

        isMonitoring = false

        // Lama - Removes the stored service instance
        instance = null

        serviceScope.cancel()

        super.onDestroy()
    }

}