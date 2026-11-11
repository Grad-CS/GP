package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//Ragahd-: Stores the fixed historical usage baseline for each app and weekday.
@Entity(
    tableName = "usage_baseline",
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
        Index(value = ["appId", "dayOfWeek"], unique = true)
    ]
)
data class UsageBaselineEntity(
    @PrimaryKey(autoGenerate = true)
    val baselineId: Int = 0,
    val appId: Int,
    val dayOfWeek: Int,
    val averageUsageSeconds: Int,
    val sampleCount: Int,
    val periodStart: Long,
    val periodEnd: Long,
    val capturedAt: Long
)