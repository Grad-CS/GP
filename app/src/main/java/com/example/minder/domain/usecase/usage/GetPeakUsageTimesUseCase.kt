
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.HourlyUsageStats
import com.example.minder.domain.model.PeakUsageTimes
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UsageSessionRepository
import java.util.Calendar

class GetPeakUsageTimesUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val usageSessionRepository: UsageSessionRepository
) {

    suspend operator fun invoke(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ): PeakUsageTimes {

        //Ragahd-: Define the last seven calendar days including today.
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -6)
        }

        val periodStart = calendar.timeInMillis
        val periodEnd = date

        //Ragahd-: Store usage milliseconds for each hour.
        val hourlyUsageMillis = LongArray(24)

        //Ragahd-: Get only enabled restricted apps.
        val enabledApps = restrictedAppRepository
            .getAppsByUserId(userId)
            .filter { it.isEnabled }

        //Ragahd-: Retrieve and distribute sessions across hourly intervals.
        enabledApps.forEach { app ->

            val sessions = usageSessionRepository.getSessionsForPeriod(
                appId = app.id,
                startTime = periodStart,
                endTime = periodEnd
            )

            sessions.forEach { session ->

                if (session.duration <= 0 ||
                    session.endTime <= session.startTime
                ) {
                    return@forEach
                }

                //Ragahd-: Clip the session to the selected measurement period.
                var cursor = maxOf(session.startTime, periodStart)
                val sessionEnd = minOf(session.endTime, periodEnd)

                while (cursor < sessionEnd) {

                    val hourCalendar = Calendar.getInstance().apply {
                        timeInMillis = cursor
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        add(Calendar.HOUR_OF_DAY, 1)
                    }

                    val nextHour = hourCalendar.timeInMillis
                    val segmentEnd = minOf(nextHour, sessionEnd)

                    val hour = Calendar.getInstance().apply {
                        timeInMillis = cursor
                    }.get(Calendar.HOUR_OF_DAY)

                    //Ragahd-: Add the actual overlapping time to its local hour.
                    hourlyUsageMillis[hour] += segmentEnd - cursor

                    cursor = segmentEnd
                }
            }
        }

        //Ragahd-: Convert hourly usage from milliseconds to seconds.
        val hourlyStats = (0 until 24).map { hour ->
            HourlyUsageStats(
                hourOfDay = hour,
                totalUsageSeconds = hourlyUsageMillis[hour] / 1000L
            )
        }

        //Ragahd-: Find the hour with the highest recorded usage.
        val peakStat = hourlyStats
            .filter { it.totalUsageSeconds > 0 }
            .maxByOrNull { it.totalUsageSeconds }

        return PeakUsageTimes(
            periodStart = periodStart,
            periodEnd = periodEnd,
            hourlyStats = hourlyStats,
            peakHour = peakStat?.hourOfDay,
            peakUsageSeconds = peakStat?.totalUsageSeconds ?: 0L
        )
    }
}
