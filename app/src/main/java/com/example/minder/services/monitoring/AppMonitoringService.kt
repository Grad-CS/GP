package com.example.minder.services.monitoring

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

class AppMonitoringService(
    private val context: Context
) {

    // Lama - Provides access to Android usage events
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    // Lama - Returns the package name of the application
    // currently detected in the foreground
    fun getCurrentForegroundApp(): String? {

        val currentTime = System.currentTimeMillis()

        // Lama - Checks recent usage events to detect the latest foreground app
        val usageEvents = usageStatsManager.queryEvents(
            currentTime - MONITORING_WINDOW,
            currentTime
        )

        val event = UsageEvents.Event()

        var currentPackageName: String? = null
        var latestForegroundTime = 0L

        while (usageEvents.hasNextEvent()) {

            usageEvents.getNextEvent(event)

            if (
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND &&
                event.packageName != context.packageName &&
                !event.packageName.contains("launcher") &&
                event.timeStamp > latestForegroundTime
            ) {

                latestForegroundTime = event.timeStamp
                currentPackageName = event.packageName
            }
        }

        return currentPackageName
    }

    fun isTargetApp(
        packageName: String?,
        targetPackages: Set<String>
    ): Boolean {

        // Lama - Checks whether the detected application
        // is one of the applications selected for monitoring
        return packageName != null &&
                packageName in targetPackages
    }

    companion object {

        // Lama - Searches usage events from the last five minutes
        private const val MONITORING_WINDOW = 5 * 60 * 1000L
    }
}