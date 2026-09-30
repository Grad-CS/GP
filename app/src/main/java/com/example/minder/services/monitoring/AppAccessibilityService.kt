package com.example.minder.services.monitoring

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

import android.content.Intent
import com.example.minder.features.challenge.ChallengeActivity

class AppAccessibilityService : AccessibilityService() {

    // Lama - Tracks the accumulated usage duration across restricted content
    private val usageLimitMonitor = UsageLimitMonitor()

    // Lama - Runs periodic usage-limit checks on the main thread
    private val monitoringHandler =
        Handler(Looper.getMainLooper())

    // Lama - Indicates whether periodic monitoring is currently running
    private var isMonitoring = false

    // Lama - Indicates whether a challenge is currently active
    // and prevents usage monitoring from restarting
    private var isChallengeActive = false

    // Lama - Opens the challenge screen when the shared usage limit is reached
    private fun showChallenge() {

        Log.d(
            "MinderChallenge",
            "Opening ChallengeActivity"
        )

        val intent =
            Intent(this, ChallengeActivity::class.java).apply {

                // Lama - Required because the activity is launched from a service
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // Lama - Brings an existing challenge activity forward
                // instead of creating unnecessary copies
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

        startActivity(intent)
    }

    // Lama - Checks the shared restricted-content usage duration every second
    private val monitoringRunnable = object : Runnable {

        override fun run() {

            Log.d(
                "MinderLimit",
                "Runnable executed | isMonitoring = $isMonitoring"
            )

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

                // Lama - Activates the challenge state when the shared limit is reached
                isChallengeActive = true

                Log.d(
                    "MinderChallenge",
                    "Challenge activated"
                )

                // Lama - Stops the current monitoring cycle
                stopLimitMonitoring()

                // Lama - Displays the challenge to the user
                showChallenge()

                return
            }

            monitoringHandler.postDelayed(
                this,
                1000L
            )
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        // Lama - Prevents monitoring from restarting while a challenge is active
        if (isChallengeActive) {

            Log.d(
                "MinderChallenge",
                "Monitoring blocked - challenge is active"
            )

            return
        }

        // Lama - Handles window changes and content changes
        // to detect navigation inside YouTube
        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName?.toString() ?: return

        // Lama - Determines whether the current screen should count
        // toward the shared restricted-content usage limit
        val shouldCountUsage = when (packageName) {

            // Lama - TikTok usage counts toward the shared limit
            "com.zhiliaoapp.musically" -> true

            // Lama - Instagram usage counts toward the shared limit
            "com.instagram.android" -> true

            // Lama - Only YouTube Shorts counts toward the shared limit
            "com.google.android.youtube" -> {

                val isShorts =
                    isYouTubeShorts()

                Log.d(
                    "MinderYouTube",
                    "Package: $packageName | Shorts: $isShorts"
                )

                isShorts
            }

            // Lama - Other applications do not count
            else -> false
        }

        Log.d(
            "MinderAccessibility",
            "Foreground app: $packageName | Count usage: $shouldCountUsage"
        )

        if (shouldCountUsage) {

            // Lama - Starts or continues the shared restricted-content timer
            usageLimitMonitor.startMonitoring(packageName)

            // Lama - Starts periodic limit checking
            startLimitMonitoring()

            Log.d(
                "MinderAccessibility",
                "Monitoring active for: $packageName"
            )

        } else {

            // Lama - Pauses without deleting accumulated usage
            pauseLimitMonitoring()
        }
    }

    // Lama - Starts periodic usage-limit checking
    private fun startLimitMonitoring() {

        Log.d(
            "MinderLimit",
            "startLimitMonitoring called | isMonitoring = $isMonitoring"
        )

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

    // Lama - Pauses monitoring while preserving accumulated usage
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

    // Lama - Completely stops and resets monitoring
    // after the shared usage limit is reached
    private fun stopLimitMonitoring() {

        if (!isMonitoring) {
            usageLimitMonitor.resetMonitoring()
            return
        }

        Log.d(
            "MinderLimit",
            "Periodic monitoring STOPPED"
        )

        isMonitoring = false

        monitoringHandler.removeCallbacks(
            monitoringRunnable
        )

        usageLimitMonitor.resetMonitoring()
    }

    // Lama - Checks whether the current YouTube screen appears to be Shorts
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

    // Lama - Searches the YouTube accessibility tree for Shorts indicators
    private fun containsShortsIndicator(
        node: AccessibilityNodeInfo
    ): Boolean {

        val text =
            node.text?.toString()?.lowercase() ?: ""

        val description =
            node.contentDescription?.toString()?.lowercase() ?: ""

        // Lama - Temporary indicators used to identify the Shorts interface
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

        // Lama - Stops monitoring if the accessibility service is interrupted
        stopLimitMonitoring()

        Log.d(
            "MinderAccessibility",
            "Accessibility service interrupted"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(
            "MinderAccessibility",
            "Accessibility service connected"
        )
    }

    override fun onDestroy() {

        // Lama - Removes callbacks when the service is destroyed
        monitoringHandler.removeCallbacks(
            monitoringRunnable
        )

        isMonitoring = false

        super.onDestroy()
    }
}