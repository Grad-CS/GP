package com.example.minder.domain.model

data class RestrictedApp(
    val id: Int,
    val userId: Int,
    val appName: String,
    val packageName: String,
    val dailyLimit: Int,
    val isEnabled: Boolean,
    val addedAt: Long
)