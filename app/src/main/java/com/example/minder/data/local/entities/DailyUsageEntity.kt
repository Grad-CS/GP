package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_usage",
    foreignKeys = [
        ForeignKey(
            entity = RestrictedAppEntity::class,
            parentColumns = ["appId"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["appId"]),
        Index(value = ["appId", "date"], unique = true)
    ]
)
data class DailyUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val dailyUsageId: Int = 0,
    val appId: Int,
    val date: Long,
    val totalUsage: Int,
    val savedTime: Int,
    val challengeCount: Int
)