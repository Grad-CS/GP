package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "challenge_attempt",
    foreignKeys = [
        ForeignKey(
            entity = InterventionEntity::class,
            parentColumns = ["interventionId"],
            childColumns = ["interventionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChallengeEntity::class,
            parentColumns = ["challengeId"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["interventionId"]),
        Index(value = ["challengeId"])
    ]
)
data class ChallengeAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val attemptId: Int = 0,
    val interventionId: Int,
    val challengeId: Int,
    val userAnswer: String,
    val isCorrect: Boolean,
    val attemptTime: Long
)