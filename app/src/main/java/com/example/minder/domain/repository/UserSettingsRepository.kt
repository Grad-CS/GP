package com.example.minder.domain.repository

import com.example.minder.domain.model.UserSettings

interface UserSettingsRepository {

    suspend fun getSettingsByUserId(
        userId: Int
    ): UserSettings?

    suspend fun saveSettings(
        settings: UserSettings
    ): Long

    suspend fun updateSettings(
        settings: UserSettings
    )
}