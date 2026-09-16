package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_session",
    foreignKeys = [
        ForeignKey(
            entity = RestrictedAppEntity::class,
            parentColumns = ["appId"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["appId"])
    ]
)
data class UsageSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    val appId: Int,
    val startTime: Long,
    val endTime: Long,
    val duration: Int
)