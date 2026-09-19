package com.example.minder.services.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

// Lama - Represents the total usage time of an application
data class AppUsageInfo(
    val packageName: String,
    val usageTimeMillis: Long
)

// Lama - Represents one application usage session
data class AppUsageSession(
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int
)

class UsageStatsService(
    private val context: Context
) {

    // Lama - Provides access to Android application usage statistics
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    // Lama - Returns the total usage time for applications used today
    fun getTodayUsage(): List<AppUsageInfo> {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        return stats
            .filter { it.totalTimeInForeground > 0 }
            .map {
                AppUsageInfo(
                    packageName = it.packageName,
                    usageTimeMillis = it.totalTimeInForeground
                )
            }
            .sortedByDescending { it.usageTimeMillis }
    }

    // Lama - Returns today's individual application usage sessions
    fun getTodayUsageSessions(): List<AppUsageSession> {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayStartTime = calendar.timeInMillis
        val currentTime = System.currentTimeMillis()

        val usageEvents =
            usageStatsManager.queryEvents(dayStartTime, currentTime)

        val event = UsageEvents.Event()

        // Lama - Stores the latest foreground start time for each application
        val activeSessions = mutableMapOf<String, Long>()

        // Lama - Stores completed application usage sessions
        val sessions = mutableListOf<AppUsageSession>()

        while (usageEvents.hasNextEvent()) {

            usageEvents.getNextEvent(event)

            when (event.eventType) {

                UsageEvents.Event.MOVE_TO_FOREGROUND -> {

                    // Lama - Records when an application enters the foreground
                    activeSessions[event.packageName] = event.timeStamp
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {

                    // Lama - Finds the matching foreground start time
                    val startTime =
                        activeSessions.remove(event.packageName)

                    if (startTime != null && event.timeStamp > startTime) {

                        val endTime = event.timeStamp

                        // Lama - Calculates the duration of the usage session
                        val duration =
                            ((endTime - startTime) / 1000).toInt()

                        sessions.add(
                            AppUsageSession(
                                packageName = event.packageName,
                                startTime = startTime,
                                endTime = endTime,
                                duration = duration
                            )
                        )
                    }
                }
            }
        }

        return sessions.sortedByDescending {
            it.startTime
        }
    }
}