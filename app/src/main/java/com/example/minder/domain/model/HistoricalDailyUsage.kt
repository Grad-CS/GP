package com.example.minder.domain.model

data class HistoricalDailyUsage(
    val packageName: String,
    val date: Long,
    val usageSeconds: Int?
)