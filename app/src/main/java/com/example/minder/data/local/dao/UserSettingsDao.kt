package com.example.minder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.minder.data.local.entities.UserSettingsEntity

@Dao
interface UserSettingsDao {

    @Insert
    suspend fun insertSettings(settings: UserSettingsEntity): Long

    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    suspend fun getSettingsByUserId(userId: Int): UserSettingsEntity?

    @Update
    suspend fun updateSettings(settings: UserSettingsEntity)

    @Delete
    suspend fun deleteSettings(settings: UserSettingsEntity)
}