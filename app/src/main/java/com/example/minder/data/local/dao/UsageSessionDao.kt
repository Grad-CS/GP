package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.minder.data.local.entities.UsageSessionEntity

@Dao
interface UsageSessionDao {

    @Insert
    suspend fun insertSession(session: UsageSessionEntity): Long

    @Query("SELECT * FROM usage_session WHERE appId = :appId")
    suspend fun getSessionsByAppId(appId: Int): List<UsageSessionEntity>

    @Query("""
        SELECT * FROM usage_session
        WHERE appId = :appId
        AND endTime > :startTime
        AND startTime < :endTime
    """)
    suspend fun getSessionsForPeriod(
        appId: Int,
        startTime: Long,
        endTime: Long
    ): List<UsageSessionEntity>

    @Query("SELECT * FROM usage_session WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: Int): UsageSessionEntity?

    @Delete
    suspend fun deleteSession(session: UsageSessionEntity)
}