package com.example.minder.domain.model

data class ExtensionRequest(
    val id: Int = 0,
    val appId: Int,
    val requestedMinutes: Int,
    val grantedMinutes: Int,
    val requestTime: Long,
    val status: String
)