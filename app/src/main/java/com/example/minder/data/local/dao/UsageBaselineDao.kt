
package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.minder.data.local.entities.UsageBaselineEntity


@Dao
interface UsageBaselineDao {

    //Ragahd-: Insert baseline records without replacing existing records.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBaselines(
        records: List<UsageBaselineEntity>
    ): List<Long>

    //Ragahd-: Count existing baseline records for an application.
    @Query("""
        SELECT COUNT(*) FROM usage_baseline
        WHERE appId = :appId
    """)
    suspend fun getBaselineCount(appId: Int): Int

    //Ragahd-: Save all seven weekday baselines only when none exists.
    @Transaction
    suspend fun saveBaselinesIfAbsent(
        records: List<UsageBaselineEntity>
    ): Boolean {
        //Ragahd-: Reject incomplete, mixed-app, or invalid weekday records.

        val appId = records.first().appId
        if (
            records.any { it.appId != appId } ||
            records.map { it.dayOfWeek }.toSet() !=
            (java.util.Calendar.SUNDAY..java.util.Calendar.SATURDAY).toSet()
        ) {
            return false
        }
        //Ragahd-: Reject incomplete or mixed-application baseline records.
        if (records.any { it.appId != appId } ||
            records.map { it.dayOfWeek }.distinct().size != 7
        ) {
            return false
        }

        //Ragahd-: Preserve the existing fixed baseline if one already exists.
        if (getBaselineCount(appId) > 0) return false

        //Ragahd-: Accept the baseline only when all seven records are inserted.
        val insertedIds = insertBaselines(records)

        return insertedIds.size == 7 && insertedIds.all { it != -1L }
    }

    //Ragahd-: Retrieve the baseline for a specific app and weekday.
    @Query("""
        SELECT * FROM usage_baseline
        WHERE appId = :appId
        AND dayOfWeek = :dayOfWeek
        LIMIT 1
    """)
    suspend fun getBaselineForDay(
        appId: Int,
        dayOfWeek: Int
    ): UsageBaselineEntity?

    //Ragahd-: Retrieve all baseline records belonging to an app.
    @Query("""
        SELECT * FROM usage_baseline
        WHERE appId = :appId
        ORDER BY dayOfWeek ASC
    """)
    suspend fun getBaselineByAppId(
        appId: Int
    ): List<UsageBaselineEntity>
}
