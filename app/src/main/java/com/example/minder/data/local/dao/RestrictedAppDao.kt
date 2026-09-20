package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.minder.data.local.entities.RestrictedAppEntity

@Dao
interface RestrictedAppDao {

    @Insert
    suspend fun insertApp(app: RestrictedAppEntity): Long

    @Query("SELECT * FROM restricted_app WHERE userId = :userId")
    suspend fun getAppsByUserId(userId: Int): List<RestrictedAppEntity>

    @Query("SELECT * FROM restricted_app WHERE appId = :appId")
    suspend fun getAppById(appId: Int): RestrictedAppEntity?

    @Query("""
        SELECT * FROM restricted_app
        WHERE userId = :userId AND packageName = :packageName
    """)
    suspend fun getAppByPackageName(
        userId: Int,
        packageName: String
    ): RestrictedAppEntity?

    @Update
    suspend fun updateApp(app: RestrictedAppEntity)

    @Delete
    suspend fun deleteApp(app: RestrictedAppEntity)
}