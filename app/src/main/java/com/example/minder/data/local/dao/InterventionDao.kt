package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.minder.data.local.entities.InterventionEntity

@Dao
interface InterventionDao {

    @Insert
    suspend fun insertIntervention(intervention: InterventionEntity): Long

    @Query("SELECT * FROM intervention WHERE interventionId = :interventionId")
    suspend fun getInterventionById(
        interventionId: Int
    ): InterventionEntity?

    @Query("SELECT * FROM intervention WHERE appId = :appId")
    suspend fun getInterventionsByAppId(
        appId: Int
    ): List<InterventionEntity>

    @Query("SELECT * FROM intervention WHERE challengeId = :challengeId")
    suspend fun getInterventionsByChallengeId(
        challengeId: Int
    ): List<InterventionEntity>

    @Query("""
        SELECT * FROM intervention
        WHERE appId = :appId
        AND triggerTime >= :startTime
        AND triggerTime < :endTime
    """)
    suspend fun getInterventionsForPeriod(
        appId: Int,
        startTime: Long,
        endTime: Long
    ): List<InterventionEntity>

    @Query("""
        UPDATE intervention
        SET unlockTime = :unlockTime,
            status = :status
        WHERE interventionId = :interventionId
    """)
    suspend fun completeIntervention(
        interventionId: Int,
        unlockTime: Long?,
        status: String
    )

    @Update
    suspend fun updateIntervention(intervention: InterventionEntity)

    @Delete
    suspend fun deleteIntervention(intervention: InterventionEntity)
}