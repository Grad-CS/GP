
package com.example.minder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "challenge")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val challengeId: Int = 0,

    val question: String,

    val correctAnswer: String,

    val difficulty: String,

    val challengeType: String,

    //Ragahd-: Stores generated game data as a JSON string for restoring the same round.
    //Ragahd-: Stores generated game data and matches the database migration default.
    @ColumnInfo(defaultValue = "'{}'")
    val gameData: String = "{}"
)
