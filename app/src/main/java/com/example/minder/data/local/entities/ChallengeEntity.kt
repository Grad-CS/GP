package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val challengeId: Int = 0,

    val question: String,

    val correctAnswer: String,

    val difficulty: String,

    val challengeType: String
)