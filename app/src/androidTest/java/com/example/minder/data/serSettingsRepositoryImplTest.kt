package com.example.minder.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.domain.model.UserSettings
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class UserSettingsRepositoryImplTest {

    private lateinit var database: MinderDatabase
    private lateinit var repository: UserSettingsRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = UserSettingsRepositoryImpl(
            database.userSettingsDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveSettings_returnsIdAndSavesSettings() = runTest {
        val userId = database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()

        val settings = UserSettings(
            settingsId = 0,
            userId = userId,
            challengeDifficulty = "MEDIUM",
            challengeInterval = 15,
            blockingEnabled = true,
            usageAccessGranted = true
        )

        val settingsId = repository.saveSettings(settings)

        val savedSettings = repository.getSettingsByUserId(userId)

        assertEquals(1, settingsId.toInt())
        assertNotNull(savedSettings)

        savedSettings!!

        assertEquals(settingsId.toInt(), savedSettings.settingsId)
        assertEquals(userId, savedSettings.userId)
        assertEquals("MEDIUM", savedSettings.challengeDifficulty)
        assertEquals(15, savedSettings.challengeInterval)
        assertEquals(true, savedSettings.blockingEnabled)
        assertEquals(true, savedSettings.usageAccessGranted)
    }

    @Test
    fun getSettingsByUserId_returnsCorrectSettings() = runTest {
        val userId1 = database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()

        val userId2 = database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()

        val settings1 = UserSettings(
            settingsId = 0,
            userId = userId1,
            challengeDifficulty = "EASY",
            challengeInterval = 10,
            blockingEnabled = true,
            usageAccessGranted = false
        )

        val settings2 = UserSettings(
            settingsId = 0,
            userId = userId2,
            challengeDifficulty = "HARD",
            challengeInterval = 30,
            blockingEnabled = false,
            usageAccessGranted = true
        )

        repository.saveSettings(settings1)
        repository.saveSettings(settings2)

        val result = repository.getSettingsByUserId(userId2)

        assertNotNull(result)

        result!!

        assertEquals(userId2, result.userId)
        assertEquals("HARD", result.challengeDifficulty)
        assertEquals(30, result.challengeInterval)
        assertEquals(false, result.blockingEnabled)
        assertEquals(true, result.usageAccessGranted)
    }

    @Test
    fun getSettingsByUserId_returnsNullForUserWithoutSettings() = runTest {
        val userId = database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()

        val result = repository.getSettingsByUserId(userId)

        assertNull(result)
    }

    @Test
    fun updateSettings_updatesExistingSettings() = runTest {
        val userId = database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()

        val settingsId = repository.saveSettings(
            UserSettings(
                settingsId = 0,
                userId = userId,
                challengeDifficulty = "EASY",
                challengeInterval = 10,
                blockingEnabled = false,
                usageAccessGranted = false
            )
        ).toInt()

        val updatedSettings = UserSettings(
            settingsId = settingsId,
            userId = userId,
            challengeDifficulty = "HARD",
            challengeInterval = 30,
            blockingEnabled = true,
            usageAccessGranted = true
        )

        repository.updateSettings(updatedSettings)

        val result = repository.getSettingsByUserId(userId)

        assertNotNull(result)

        result!!

        assertEquals(settingsId, result.settingsId)
        assertEquals(userId, result.userId)
        assertEquals("HARD", result.challengeDifficulty)
        assertEquals(30, result.challengeInterval)
        assertEquals(true, result.blockingEnabled)
        assertEquals(true, result.usageAccessGranted)
    }
}