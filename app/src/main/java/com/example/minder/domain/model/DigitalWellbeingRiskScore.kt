package com.example.minder.domain.model

data class DigitalWellbeingRiskScore(
    val totalScore: Int,
    val level: RiskLevel
)

enum class RiskLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}