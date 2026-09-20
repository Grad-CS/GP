package com.example.minder.data.repository

import com.example.minder.data.local.dao.UserSettingsDao
import com.example.minder.data.local.entities.UserSettingsEntity
import com.example.minder.domain.model.UserSettings
import com.example.minder.domain.repository.UserSettingsRepository

class UserSettingsRepositoryImpl(
    private val userSettingsDao: UserSettingsDao
) : UserSettingsRepository {

    override suspend fun getSettingsByUserId(
        userId: Int
    ): UserSettings? {
        return userSettingsDao
            .getSettingsByUserId(userId)
            ?.toDomain()
    }

    override suspend fun saveSettings(
        settings: UserSettings
    ): Long {
        return userSettingsDao.insertSettings(
            settings.toEntity()
        )
    }

    override suspend fun updateSettings(
        settings: UserSettings
    ) {
        userSettingsDao.updateSettings(
            settings.toEntity()
        )
    }
}

private fun UserSettingsEntity.toDomain(): UserSettings {
    return UserSettings(
        settingsId = settingsId,
        userId = userId,
        challengeDifficulty = challengeDifficulty,
        challengeInterval = challengeInterval,
        blockingEnabled = blockingEnabled,
        usageAccessGranted = usageAccessGranted
    )
}

private fun UserSettings.toEntity(): UserSettingsEntity {
    return UserSettingsEntity(
        settingsId = settingsId,
        userId = userId,
        challengeDifficulty = challengeDifficulty,
        challengeInterval = challengeInterval,
        blockingEnabled = blockingEnabled,
        usageAccessGranted = usageAccessGranted
    )
}