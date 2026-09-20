package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.minder.data.local.entities.DailyUsageEntity

@Dao
interface DailyUsageDao {

    @Insert
    suspend fun insertDailyUsage(usage: DailyUsageEntity): Long

    @Query("""
        SELECT * FROM daily_usage
        WHERE appId = :appId
        ORDER BY date DESC
    """)
    suspend fun getDailyUsageByAppId(
        appId: Int
    ): List<DailyUsageEntity>

    @Query("""
        SELECT * FROM daily_usage
        WHERE appId = :appId
        AND date >= :startDate
        AND date < :endDate
    """)
    suspend fun getUsageForPeriod(
        appId: Int,
        startDate: Long,
        endDate: Long
    ): List<DailyUsageEntity>

    @Query("""
        SELECT * FROM daily_usage
        WHERE appId = :appId
        AND date = :date
        LIMIT 1
    """)
    suspend fun getUsageForDate(
        appId: Int,
        date: Long
    ): DailyUsageEntity?

    @Update
    suspend fun updateDailyUsage(usage: DailyUsageEntity)

    @Delete
    suspend fun deleteDailyUsage(usage: DailyUsageEntity)
}