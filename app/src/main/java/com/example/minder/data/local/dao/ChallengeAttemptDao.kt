package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.minder.data.local.entities.ChallengeAttemptEntity

@Dao
interface ChallengeAttemptDao {

    @Insert
    suspend fun insertAttempt(attempt: ChallengeAttemptEntity): Long

    @Query("""
        SELECT * FROM challenge_attempt
        WHERE interventionId = :interventionId
        ORDER BY attemptTime ASC
    """)
    suspend fun getAttemptsByInterventionId(
        interventionId: Int
    ): List<ChallengeAttemptEntity>

    @Query("""
        SELECT * FROM challenge_attempt
        WHERE challengeId = :challengeId
    """)
    suspend fun getAttemptsByChallengeId(
        challengeId: Int
    ): List<ChallengeAttemptEntity>

    @Query("""
        SELECT * FROM challenge_attempt
        WHERE interventionId = :interventionId
        AND isCorrect = 1
    """)
    suspend fun getSuccessfulAttempts(
        interventionId: Int
    ): List<ChallengeAttemptEntity>

    @Query("""
        SELECT COUNT(*) FROM challenge_attempt
        WHERE interventionId = :interventionId
    """)
    suspend fun getAttemptCount(
        interventionId: Int
    ): Int

    @Delete
    suspend fun deleteAttempt(attempt: ChallengeAttemptEntity)
}