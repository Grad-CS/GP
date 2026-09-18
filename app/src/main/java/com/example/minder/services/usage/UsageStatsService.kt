package com.example.minder.services.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

data class AppUsageInfo(
    val packageName: String,
    val usageTimeMillis: Long
)

class UsageStatsService(
    private val context: Context
) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

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
}