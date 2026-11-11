package com.example.minder.services.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//Ragahd-: Diagnostic utility for inspecting historical Android usage records.
class HistoricalUsageDiagnostic(
    context: Context
) {

    //Ragahd-: Access Android usage statistics.
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE)
                as UsageStatsManager

    //Ragahd-: Format timestamps for diagnostic logs.
    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    //Ragahd-: Inspect daily usage records across a historical period.
    fun inspectHistoricalUsage(days: Int = 14) {

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -days)
        }

        repeat(days) {

            val startTime = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_MONTH, 1)

            val endTime = calendar.timeInMillis

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            val relevantStats = usageStats.filter {
                it.totalTimeInForeground > 0
            }

            Log.d(
                "HistoricalDiagnostic",
                "======================================"
            )

            Log.d(
                "HistoricalDiagnostic",
                "Requested date: ${dateFormat.format(Date(startTime))}"
            )

            Log.d(
                "HistoricalDiagnostic",
                "Records count: ${usageStats.size}"
            )

            Log.d(
                "HistoricalDiagnostic",
                "Used apps: ${relevantStats.size}"
            )

            relevantStats.forEach { stat ->

                Log.d(
                    "HistoricalDiagnostic",
                    """
                    Package: ${stat.packageName}
                    Usage: ${stat.totalTimeInForeground / 1000} seconds
                    First timestamp: ${dateFormat.format(Date(stat.firstTimeStamp))}
                    Last timestamp: ${dateFormat.format(Date(stat.lastTimeStamp))}
                    """.trimIndent()
                )
            }
        }
    }
}