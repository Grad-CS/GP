package com.example.minder.domain.model

data class DailyUsageStats(
    val date: Long,
    val totalUsageSeconds: Int,
    val apps: List<AppDailyUsageStats>
)