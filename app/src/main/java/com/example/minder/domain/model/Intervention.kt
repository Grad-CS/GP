package com.example.minder.domain.model

data class Intervention(
    val id: Int,
    val appId: Int,
    val challengeId: Int,
    val triggerTime: Long,
    val unlockTime: Long?,
    val status: String,
    val savedTime: Int
)