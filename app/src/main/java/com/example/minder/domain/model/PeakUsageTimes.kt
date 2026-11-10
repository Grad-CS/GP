
package com.example.minder.domain.model

data class PeakUsageTimes(
    val periodStart: Long,
    val periodEnd: Long,
    val hourlyStats: List<HourlyUsageStats>,
    val peakHour: Int?,
    val peakUsageSeconds: Long
)
