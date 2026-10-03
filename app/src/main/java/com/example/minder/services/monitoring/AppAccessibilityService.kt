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


class AppAccessibilityService : AccessibilityService() {

    companion object {

        // Lama - Holds the currently running accessibility service instance
        private var instance: AppAccessibilityService? = null

        // Lama - Notifies the service that the current challenge has ended
        fun notifyChallengeCompleted() {
            instance?.onChallengeCompleted()
        }
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

    // Lama - Repositories used to connect monitoring with stored app data
    private lateinit var userRepository: UserRepository
    private lateinit var restrictedAppRepository: RestrictedAppRepository

    // Lama - Creates the challenge and intervention when the limit is reached
    private lateinit var startInterventionUseCase: StartInterventionUseCase

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

            Log.d(
                "MinderLimit",
                "App: $packageName | Usage: $elapsedSeconds sec"
            )

            if (usageLimitMonitor.hasReachedLimit()) {

                Log.d(
                    "MinderLimit",
                    "30 SECOND LIMIT REACHED for: $packageName"
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

        // Lama - Resolves package names to restricted applications
        restrictedAppRepository =
            RestrictedAppRepositoryImpl(
                database.restrictedAppDao()
            )

        // Lama - Uses the existing challenge engine repositories
        val challengeRepository =
            ChallengeRepositoryImpl(
                database.challengeDao()
            )

        val interventionRepository =
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

        // Lama - Prevents monitoring from restarting
        // while the challenge is active
        if (isChallengeActive) {
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

    private fun showChallenge(
        appId: Int,
        interventionId: Int,
        packageName: String
    ) {

        Log.d(
            "MinderChallenge",
            "Opening ChallengeActivity | appId=$appId | interventionId=$interventionId | package=$packageName"
        )

        val intent =
            Intent(
                this,
                ChallengeActivity::class.java
            ).apply {

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

        // Lama - Starts the next shared usage interval from zero
        usageLimitMonitor.resetMonitoring()

        Log.d(
            "MinderLimit",
            "New 30-second monitoring cycle is ready"
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