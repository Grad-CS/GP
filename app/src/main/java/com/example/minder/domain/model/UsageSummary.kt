package com.example.minder.domain.model

data class UsageSummary(
    val id: Int,
    val appId: Int,
    val date: Long,
    val totalUsage: Int,
    val savedTime: Int,
    val challengeCount: Int
)