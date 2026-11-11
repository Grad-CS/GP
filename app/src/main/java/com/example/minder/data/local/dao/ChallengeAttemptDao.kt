package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.minder.data.local.entities.ChallengeAttemptEntity
import com.example.minder.data.local.models.ChallengeAttemptWithTypeEntity

@Dao
interface ChallengeAttemptDao {

    @Insert
    suspend fun insertAttempt(attempt: ChallengeAttemptEntity): Long

    //Ragahd-: Retrieves all attempts belonging to an intervention.
    @Query("""
    SELECT * FROM challenge_attempt
    WHERE interventionId = :interventionId
    ORDER BY attemptTime ASC
""")
    suspend fun getAttemptsByInterventionId(
        interventionId: Int
    ): List<ChallengeAttemptEntity>

    //Ragahd-: Retrieves attempts within a period for currently enabled apps belonging to a user.
    @Query("""
    SELECT
        ca.interventionId,
        ca.challengeId,
        ca.isCorrect,
        ca.attemptTime,
        c.challengeType
    FROM challenge_attempt AS ca
    INNER JOIN intervention AS i
        ON ca.interventionId = i.interventionId
    INNER JOIN restricted_app AS r
        ON i.appId = r.appId
    INNER JOIN challenge AS c
        ON ca.challengeId = c.challengeId
    WHERE r.userId = :userId
        AND r.isEnabled = 1
        AND ca.attemptTime >= :startTime
        AND ca.attemptTime <= :endTime
    ORDER BY ca.attemptTime ASC
""")
    suspend fun getAttemptsWithTypeForPeriod(
        userId: Int,
        startTime: Long,
        endTime: Long
    ): List<ChallengeAttemptWithTypeEntity>

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