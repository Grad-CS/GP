package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.minder.data.local.entities.ChallengeEntity

@Dao
interface ChallengeDao {

    @Insert
    suspend fun insertChallenge(challenge: ChallengeEntity): Long

    @Query("SELECT * FROM challenge WHERE challengeId = :challengeId")
    suspend fun getChallengeById(challengeId: Int): ChallengeEntity?

    @Query("SELECT * FROM challenge WHERE difficulty = :difficulty")
    suspend fun getChallengesByDifficulty(
        difficulty: String
    ): List<ChallengeEntity>

    @Query("SELECT * FROM challenge WHERE challengeType = :challengeType")
    suspend fun getChallengesByType(
        challengeType: String
    ): List<ChallengeEntity>

    @Query("SELECT * FROM challenge")
    suspend fun getAllChallenges(): List<ChallengeEntity>

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Delete
    suspend fun deleteChallenge(challenge: ChallengeEntity)
}