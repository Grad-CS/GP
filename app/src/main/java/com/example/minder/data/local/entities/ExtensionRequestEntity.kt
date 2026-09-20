package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "extension_request",
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
data class ExtensionRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val extensionId: Int = 0,
    val appId: Int,
    val requestedMinutes: Int,
    val grantedMinutes: Int,
    val requestTime: Long,
    val status: String
)