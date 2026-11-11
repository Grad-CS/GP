package com.example.minder.data.datasource

import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.minder.domain.model.HistoricalDailyUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class HistoricalUsageDataSourceImpl(
    context: Context
) : HistoricalUsageDataSource {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    override suspend fun getDailyUsage(
        packageName: String,
        startDate: Long,
        endDateExclusive: Long
    ): List<HistoricalDailyUsage> = withContext(Dispatchers.IO) {

        require(startDate < endDateExclusive) {
            "Start date must be before end date."
        }

        val results = mutableListOf<HistoricalDailyUsage>()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (calendar.timeInMillis < endDateExclusive) {

            val dayStart = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_MONTH, 1)

            val dayEnd = minOf(
                calendar.timeInMillis,
                endDateExclusive
            )

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                dayStart,
                dayEnd
            )

            val matchingRecords = usageStats.filter {
                it.packageName == packageName
            }

            //Ragahd-: Keep missing records distinct from verified zero usage.
            val usageSeconds = if (matchingRecords.isEmpty()) {
                null
            } else {
                (matchingRecords.sumOf {
                    it.totalTimeInForeground
                } / 1000L).toInt()
            }

            results.add(
                HistoricalDailyUsage(
                    packageName = packageName,
                    date = dayStart,
                    usageSeconds = usageSeconds
                )
            )
        }

        results
    }
}