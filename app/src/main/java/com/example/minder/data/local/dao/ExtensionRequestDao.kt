package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.minder.data.local.entities.ExtensionRequestEntity
import androidx.room.Transaction

@Dao
interface ExtensionRequestDao {

    @Insert
    suspend fun insertRequest(request: ExtensionRequestEntity): Long

    @Query("""
        SELECT * FROM extension_request
        WHERE appId = :appId
        ORDER BY requestTime DESC
    """)
    suspend fun getRequestsByAppId(
        appId: Int
    ): List<ExtensionRequestEntity>

    @Query("""
        SELECT * FROM extension_request
        WHERE extensionId = :extensionId
    """)
    suspend fun getRequestById(
        extensionId: Int
    ): ExtensionRequestEntity?

    @Query("""
        UPDATE extension_request
        SET grantedMinutes = :grantedMinutes,
            status = :status
        WHERE extensionId = :extensionId
    """)
    suspend fun updateRequestStatus(
        extensionId: Int,
        grantedMinutes: Int,
        status: String
    )

    @Query("""
    SELECT * FROM extension_request
    WHERE appId = :appId
      AND requestTime >= :dayStart
      AND requestTime < :nextDayStart
    LIMIT 1
""")
    suspend fun getRequestForAppOnDate(
        appId: Int,
        dayStart: Long,
        nextDayStart: Long
    ): ExtensionRequestEntity?

    //Ragahd-: Prevents granting more than one daily extension for the same app.
    @Transaction
    suspend fun insertDailyExtensionIfAbsent(
        request: ExtensionRequestEntity,
        dayStart: Long,
        nextDayStart: Long
    ): Long? {
        val existingRequest = getRequestForAppOnDate(
            appId = request.appId,
            dayStart = dayStart,
            nextDayStart = nextDayStart
        )

        if (existingRequest != null) return null

        return insertRequest(request)
    }

    @Update
    suspend fun updateRequest(request: ExtensionRequestEntity)

    @Delete
    suspend fun deleteRequest(request: ExtensionRequestEntity)
}