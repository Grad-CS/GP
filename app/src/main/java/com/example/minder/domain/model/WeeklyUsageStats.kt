package com.example.minder.domain.model

data class WeeklyUsageStats(
    val weekStartDate: Long,
    val weekEndDate: Long,
    val totalUsageSeconds: Int,
    val averageDailyUsageSeconds: Int,
    val apps: List<AppWeeklyUsageStats>
)