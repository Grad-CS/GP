package com.example.minder.domain.model

data class AppWeeklyUsageStats(
    val appId: Int,
    val appName: String,
    val packageName: String,
    val totalUsageSeconds: Int,
    val averageDailyUsageSeconds: Int
)