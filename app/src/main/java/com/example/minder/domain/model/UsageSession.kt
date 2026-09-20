package com.example.minder.domain.model

data class UsageSession(
    val id: Int,
    val appId: Int,
    val startTime: Long,
    val endTime: Long,
    val duration: Int
)