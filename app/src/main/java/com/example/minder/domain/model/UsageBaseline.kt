package com.example.minder.domain.model

data class UsageBaseline(
    val baselineId: Int,
    val appId: Int,
    val dayOfWeek: Int,
    val averageUsageSeconds: Int,
    val sampleCount: Int,
    val periodStart: Long,
    val periodEnd: Long,
    val capturedAt: Long
)