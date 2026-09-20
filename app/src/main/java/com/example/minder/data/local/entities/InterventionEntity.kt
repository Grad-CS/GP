package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intervention",
    foreignKeys = [
        ForeignKey(
            entity = RestrictedAppEntity::class,
            parentColumns = ["appId"],
            childColumns = ["appId"],
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
        Index(value = ["appId"]),
        Index(value = ["challengeId"])
    ]
)
data class InterventionEntity(
    @PrimaryKey(autoGenerate = true)
    val interventionId: Int = 0,
    val appId: Int,
    val challengeId: Int,
    val triggerTime: Long,
    val unlockTime: Long?,
    val status: String,
    val savedTime: Int
)