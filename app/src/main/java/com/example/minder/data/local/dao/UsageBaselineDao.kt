
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


    @Transaction
    suspend fun saveBaselinesIfAbsent(
        records: List<UsageBaselineEntity>
    ): Boolean {

        // Lama - Reject empty or incomplete baseline records.
        if (records.size != 7) return false

        val appId = records.first().appId

        // Lama - Validate application and weekday records.
        val expectedDays =
            (java.util.Calendar.SUNDAY..java.util.Calendar.SATURDAY).toSet()

        if (
            records.any { it.appId != appId } ||
            records.map { it.dayOfWeek }.toSet() != expectedDays
        ) {
            return false
        }

        // Lama - Keep the original baseline if it already exists.
        if (getBaselineCount(appId) > 0) return false

        // Lama - Insert all seven records.
        val insertedIds = insertBaselines(records)

        return insertedIds.size == 7 &&
                insertedIds.all { it != -1L }
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
