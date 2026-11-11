
package com.example.minder.domain.model

data class UsageComparisonStats(
    val currentWeekUsageSeconds: Int,
    val previousWeekUsageSeconds: Int,
    val usageChangeSeconds: Int,
    val usageChangePercentage: Double,
    val trend: UsageTrend
)

enum class UsageTrend {
    DECREASED,
    INCREASED,
    UNCHANGED
}
