package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"], unique = true)
    ]
)
data class UserSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val settingsId: Int = 0,
    val userId: Int,
    val challengeDifficulty: String,
    val challengeInterval: Int,
    val blockingEnabled: Boolean,
    val usageAccessGranted: Boolean
)