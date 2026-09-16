package com.example.minder.domain.model

data class Challenge(
    val id: Int,
    val question: String,
    val correctAnswer: String,
    val difficulty: String,
    val type: String
)