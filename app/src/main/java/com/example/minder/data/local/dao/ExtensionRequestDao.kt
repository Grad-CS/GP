package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.minder.data.local.entities.ExtensionRequestEntity

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

    @Update
    suspend fun updateRequest(request: ExtensionRequestEntity)

    @Delete
    suspend fun deleteRequest(request: ExtensionRequestEntity)
}