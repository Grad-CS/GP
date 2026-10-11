package com.example.minder.domain.usecase.settings

import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UserSettingsRepository

class ValidateChallengeIntervalUseCase(
    private val userSettingsRepository: UserSettingsRepository,
    private val restrictedAppRepository: RestrictedAppRepository
) {

    suspend operator fun invoke(
        userId: Int,
        appId: Int
    ): Boolean {

        val settings =
            userSettingsRepository.getSettingsByUserId(userId)
                ?: return false

        val app =
            restrictedAppRepository.getAppById(appId)
                ?: return false

        return settings.challengeInterval < app.dailyLimit
    }
}