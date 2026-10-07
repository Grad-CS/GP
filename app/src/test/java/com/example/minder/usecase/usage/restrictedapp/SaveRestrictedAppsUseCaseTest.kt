package com.example.minder.domain.usecase.restrictedapp

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.repository.RestrictedAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveRestrictedAppsUseCaseTest {

    @Test
    fun `saves selected app when it does not already exist`() = runTest {

        val repository = FakeRestrictedAppRepository()

        val useCase = SaveRestrictedAppsUseCase(repository)

        val selectedApps = listOf(
            SelectedApp(
                appName = "TikTok",
                packageName = "com.tiktok",
                dailyLimit = 30
            )
        )

        useCase(
            userId = 1,
            selectedApps = selectedApps
        )

        assertEquals(1, repository.savedApps.size)

        val savedApp = repository.savedApps.first()

        assertEquals("TikTok", savedApp.appName)
        assertEquals("com.tiktok", savedApp.packageName)
        assertEquals(30, savedApp.dailyLimit)
        assertEquals(1, savedApp.userId)
        assertEquals(true, savedApp.isEnabled)
    }

    @Test
    fun `does not save duplicate app for same user`() = runTest {

        val repository = FakeRestrictedAppRepository()

        repository.existingApps.add(
            RestrictedApp(
                id = 1,
                userId = 1,
                appName = "TikTok",
                packageName = "com.tiktok",
                dailyLimit = 30,
                isEnabled = true,
                addedAt = 123L
            )
        )

        val useCase = SaveRestrictedAppsUseCase(repository)

        val selectedApps = listOf(
            SelectedApp(
                appName = "TikTok",
                packageName = "com.tiktok",
                dailyLimit = 50
            )
        )

        useCase(
            userId = 1,
            selectedApps = selectedApps
        )

        assertEquals(0, repository.savedApps.size)
    }

    @Test
    fun `saves multiple selected apps`() = runTest {

        val repository = FakeRestrictedAppRepository()

        val useCase = SaveRestrictedAppsUseCase(repository)

        val selectedApps = listOf(
            SelectedApp(
                appName = "TikTok",
                packageName = "com.tiktok",
                dailyLimit = 30
            ),
            SelectedApp(
                appName = "Instagram",
                packageName = "com.instagram",
                dailyLimit = 50
            ),
            SelectedApp(
                appName = "YouTube",
                packageName = "com.youtube",
                dailyLimit = 20
            )
        )

        useCase(
            userId = 1,
            selectedApps = selectedApps
        )

        assertEquals(3, repository.savedApps.size)
    }

    private class FakeRestrictedAppRepository :
        RestrictedAppRepository {

        val savedApps = mutableListOf<RestrictedApp>()
        val existingApps = mutableListOf<RestrictedApp>()

        override suspend fun getAppsByUserId(
            userId: Int
        ): List<RestrictedApp> {
            return existingApps.filter {
                it.userId == userId
            }
        }

        override suspend fun getAppById(
            appId: Int
        ): RestrictedApp? {
            return existingApps.find {
                it.id == appId
            }
        }

        override suspend fun getAppByPackageName(
            userId: Int,
            packageName: String
        ): RestrictedApp? {
            return existingApps.find {
                it.userId == userId &&
                        it.packageName == packageName
            }
        }

        override suspend fun addApp(
            app: RestrictedApp
        ): Long {
            savedApps.add(app)
            return savedApps.size.toLong()
        }

        override suspend fun updateApp(
            app: RestrictedApp
        ) {
            // Not needed for this test
        }

        override suspend fun deleteApp(
            app: RestrictedApp
        ) {
            // Not needed for this test
        }
    }
}