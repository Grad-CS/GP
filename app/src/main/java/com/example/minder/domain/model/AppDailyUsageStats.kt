package com.example.minder.domain.model

data class AppDailyUsageStats(
    val appId: Int,
    val appName: String,
    val packageName: String,
    val totalUsageSeconds: Int
)