package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "restricted_app",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "packageName"], unique = true)
    ]
)
data class RestrictedAppEntity(
    @PrimaryKey(autoGenerate = true)
    val appId: Int = 0,
    val userId: Int,
    val appName: String,
    val packageName: String,
    val dailyLimit: Int,
    val isEnabled: Boolean,
    val addedAt: Long
)