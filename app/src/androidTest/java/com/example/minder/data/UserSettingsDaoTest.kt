package com.example.minder.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.dao.UserSettingsDao
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.data.local.entities.UserSettingsEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.example.minder.data.local.database.MinderDatabase

@RunWith(AndroidJUnit4::class)
class UserSettingsDaoTest {

    private lateinit var database: MinderDatabase
    private lateinit var userSettingsDao: UserSettingsDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        userSettingsDao = database.userSettingsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun createUser(): Int {
        return database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()
    }

    @Test
    fun insertSettings_andGetSettingsByUserId_returnsCorrectSettings() = runBlocking {

        val userId = createUser()

        userSettingsDao.insertSettings(
            UserSettingsEntity(
                userId = userId,
                challengeDifficulty = "Medium",
                challengeInterval = 30,
                blockingEnabled = true,
                usageAccessGranted = true
            )
        )

        val result = userSettingsDao.getSettingsByUserId(userId)

        assertNotNull(result)
        assertEquals(userId, result?.userId)
        assertEquals("Medium", result?.challengeDifficulty)
        assertEquals(30, result?.challengeInterval)
        assertEquals(true, result?.blockingEnabled)
        assertEquals(true, result?.usageAccessGranted)
    }

    @Test
    fun sameUser_cannotHaveTwoSettings() = runBlocking {

        val userId = createUser()

        val settings = UserSettingsEntity(
            userId = userId,
            challengeDifficulty = "Medium",
            challengeInterval = 30,
            blockingEnabled = true,
            usageAccessGranted = true
        )

        userSettingsDao.insertSettings(settings)

        try {
            userSettingsDao.insertSettings(
                settings.copy(settingsId = 0)
            )

            fail("Expected SQLiteConstraintException")
        } catch (e: SQLiteConstraintException) {
            // Expected: one settings record per user
        }
    }

    @Test
    fun differentUsers_canHaveTheirOwnSettings() = runBlocking {

        val firstUserId = createUser()
        val secondUserId = createUser()

        userSettingsDao.insertSettings(
            UserSettingsEntity(
                userId = firstUserId,
                challengeDifficulty = "Easy",
                challengeInterval = 20,
                blockingEnabled = true,
                usageAccessGranted = true
            )
        )

        userSettingsDao.insertSettings(
            UserSettingsEntity(
                userId = secondUserId,
                challengeDifficulty = "Hard",
                challengeInterval = 60,
                blockingEnabled = false,
                usageAccessGranted = false
            )
        )

        val firstSettings =
            userSettingsDao.getSettingsByUserId(firstUserId)

        val secondSettings =
            userSettingsDao.getSettingsByUserId(secondUserId)

        assertNotNull(firstSettings)
        assertNotNull(secondSettings)

        assertEquals("Easy", firstSettings?.challengeDifficulty)
        assertEquals("Hard", secondSettings?.challengeDifficulty)
    }

    @Test
    fun settings_withNonExistingUser_isRejected() = runBlocking {

        try {
            userSettingsDao.insertSettings(
                UserSettingsEntity(
                    userId = 9999,
                    challengeDifficulty = "Medium",
                    challengeInterval = 30,
                    blockingEnabled = true,
                    usageAccessGranted = true
                )
            )

            fail("Expected SQLiteConstraintException")
        } catch (e: SQLiteConstraintException) {
            // Expected: userId must exist
        }
    }

    @Test
    fun deletingUser_deletesItsSettings() = runBlocking {

        val userId = createUser()

        userSettingsDao.insertSettings(
            UserSettingsEntity(
                userId = userId,
                challengeDifficulty = "Medium",
                challengeInterval = 30,
                blockingEnabled = true,
                usageAccessGranted = true
            )
        )

        assertNotNull(
            userSettingsDao.getSettingsByUserId(userId)
        )

        database.userDao().deleteUser(
            UserEntity(
                userId = userId,
                createdAt = System.currentTimeMillis()
            )
        )

        val result =
            userSettingsDao.getSettingsByUserId(userId)

        assertEquals(null, result)
    }
}