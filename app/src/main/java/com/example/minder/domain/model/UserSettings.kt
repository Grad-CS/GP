package com.example.minder.domain.model

data class UserSettings(
    val settingsId: Int,
    val userId: Int,
    val challengeDifficulty: String,
    val challengeInterval: Int,
    val blockingEnabled: Boolean,
    val usageAccessGranted: Boolean
)