package com.example.minder.domain.usecase.settings

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.UserSettings
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UserSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateChallengeIntervalUseCaseTest {

    @Test
    fun `returns true when challenge interval is less than daily limit`() =
        runTest {

            val userSettingsRepository =
                FakeUserSettingsRepository(
                    UserSettings(
                        settingsId = 1,
                        userId = 1,
                        challengeDifficulty = "Medium",
                        challengeInterval = 10,
                        blockingEnabled = true,
                        usageAccessGranted = true
                    )
                )

            val restrictedAppRepository =
                FakeRestrictedAppRepository(
                    RestrictedApp(
                        id = 1,
                        userId = 1,
                        appName = "TikTok",
                        packageName = "com.tiktok",
                        dailyLimit = 30,
                        isEnabled = true,
                        addedAt = 0L
                    )
                )

            val useCase = ValidateChallengeIntervalUseCase(
                userSettingsRepository,
                restrictedAppRepository
            )

            val result = useCase(
                userId = 1,
                appId = 1
            )

            assertTrue(result)
        }

    @Test
    fun `returns false when challenge interval equals daily limit`() =
        runTest {

            val userSettingsRepository =
                FakeUserSettingsRepository(
                    UserSettings(
                        settingsId = 1,
                        userId = 1,
                        challengeDifficulty = "Medium",
                        challengeInterval = 30,
                        blockingEnabled = true,
                        usageAccessGranted = true
                    )
                )

            val restrictedAppRepository =
                FakeRestrictedAppRepository(
                    RestrictedApp(
                        id = 1,
                        userId = 1,
                        appName = "TikTok",
                        packageName = "com.tiktok",
                        dailyLimit = 30,
                        isEnabled = true,
                        addedAt = 0L
                    )
                )

            val useCase = ValidateChallengeIntervalUseCase(
                userSettingsRepository,
                restrictedAppRepository
            )

            val result = useCase(
                userId = 1,
                appId = 1
            )

            assertFalse(result)
        }

    @Test
    fun `returns false when challenge interval is greater than daily limit`() =
        runTest {

            val userSettingsRepository =
                FakeUserSettingsRepository(
                    UserSettings(
                        settingsId = 1,
                        userId = 1,
                        challengeDifficulty = "Medium",
                        challengeInterval = 40,
                        blockingEnabled = true,
                        usageAccessGranted = true
                    )
                )

            val restrictedAppRepository =
                FakeRestrictedAppRepository(
                    RestrictedApp(
                        id = 1,
                        userId = 1,
                        appName = "TikTok",
                        packageName = "com.tiktok",
                        dailyLimit = 30,
                        isEnabled = true,
                        addedAt = 0L
                    )
                )

            val useCase = ValidateChallengeIntervalUseCase(
                userSettingsRepository,
                restrictedAppRepository
            )

            val result = useCase(
                userId = 1,
                appId = 1
            )

            assertFalse(result)
        }

    @Test
    fun `returns false when user settings do not exist`() =
        runTest {

            val userSettingsRepository =
                FakeUserSettingsRepository(null)

            val restrictedAppRepository =
                FakeRestrictedAppRepository(
                    RestrictedApp(
                        id = 1,
                        userId = 1,
                        appName = "TikTok",
                        packageName = "com.tiktok",
                        dailyLimit = 30,
                        isEnabled = true,
                        addedAt = 0L
                    )
                )

            val useCase = ValidateChallengeIntervalUseCase(
                userSettingsRepository,
                restrictedAppRepository
            )

            val result = useCase(
                userId = 1,
                appId = 1
            )

            assertFalse(result)
        }

    @Test
    fun `returns false when restricted app does not exist`() =
        runTest {

            val userSettingsRepository =
                FakeUserSettingsRepository(
                    UserSettings(
                        settingsId = 1,
                        userId = 1,
                        challengeDifficulty = "Medium",
                        challengeInterval = 10,
                        blockingEnabled = true,
                        usageAccessGranted = true
                    )
                )

            val restrictedAppRepository =
                FakeRestrictedAppRepository(null)

            val useCase = ValidateChallengeIntervalUseCase(
                userSettingsRepository,
                restrictedAppRepository
            )

            val result = useCase(
                userId = 1,
                appId = 1
            )

            assertFalse(result)
        }
}

private class FakeUserSettingsRepository(
    private val settings: UserSettings?
) : UserSettingsRepository {

    override suspend fun getSettingsByUserId(
        userId: Int
    ): UserSettings? = settings

    override suspend fun saveSettings(
        settings: UserSettings
    ): Long = 1L

    override suspend fun updateSettings(
        settings: UserSettings
    ) {
    }
}

private class FakeRestrictedAppRepository(
    private val app: RestrictedApp?
) : RestrictedAppRepository {

    override suspend fun getAppsByUserId(
        userId: Int
    ): List<RestrictedApp> =
        app?.let { listOf(it) } ?: emptyList()

    override suspend fun getAppById(
        appId: Int
    ): RestrictedApp? = app

    override suspend fun getAppByPackageName(
        userId: Int,
        packageName: String
    ): RestrictedApp? = app

    override suspend fun addApp(
        app: RestrictedApp
    ): Long = 1L

    override suspend fun updateApp(
        app: RestrictedApp
    ) {
    }

    override suspend fun deleteApp(
        app: RestrictedApp
    ) {
    }
}