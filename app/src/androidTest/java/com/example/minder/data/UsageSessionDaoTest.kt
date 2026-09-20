package com.example.minder.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UsageSessionEntity
import com.example.minder.data.local.entities.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.example.minder.data.local.database.MinderDatabase

@RunWith(AndroidJUnit4::class)
class UsageSessionDaoTest {

    private lateinit var database: MinderDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
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

    private suspend fun createApp(userId: Int): Int {
        return database.restrictedAppDao().insertApp(
            RestrictedAppEntity(
                userId = userId,
                appName = "Instagram",
                packageName = "com.instagram.android",
                dailyLimit = 60,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()
    }

    @Test
    fun insertSession_andGetSessionById_returnsCorrectSession() = runBlocking {
        val userId = createUser()
        val appId = createApp(userId)

        val session = UsageSessionEntity(
            appId = appId,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000
        )

        val sessionId = database.usageSessionDao()
            .insertSession(session)
            .toInt()

        val result = database.usageSessionDao()
            .getSessionById(sessionId)

        assertNotNull(result)
        assertEquals(appId, result!!.appId)
        assertEquals(1000L, result.startTime)
        assertEquals(2000L, result.endTime)
        assertEquals(1000, result.duration)
    }

    @Test
    fun getSessionsByAppId_returnsOnlySessionsForGivenApp() = runBlocking {
        val userId = createUser()

        val appId1 = createApp(userId)

        val appId2 = database.restrictedAppDao().insertApp(
            RestrictedAppEntity(
                userId = userId,
                appName = "TikTok",
                packageName = "com.tiktok.android",
                dailyLimit = 60,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()

        database.usageSessionDao().insertSession(
            UsageSessionEntity(
                appId = appId1,
                startTime = 1000L,
                endTime = 2000L,
                duration = 1000
            )
        )

        database.usageSessionDao().insertSession(
            UsageSessionEntity(
                appId = appId2,
                startTime = 3000L,
                endTime = 4000L,
                duration = 1000
            )
        )

        val result = database.usageSessionDao()
            .getSessionsByAppId(appId1)

        assertEquals(1, result.size)
        assertEquals(appId1, result[0].appId)
    }

    @Test
    fun getSessionsForPeriod_returnsOverlappingSessions() = runBlocking {
        val userId = createUser()
        val appId = createApp(userId)

        // Completely inside the period
        database.usageSessionDao().insertSession(
            UsageSessionEntity(
                appId = appId,
                startTime = 2000L,
                endTime = 3000L,
                duration = 1000
            )
        )

        // Starts before the period but ends inside it
        database.usageSessionDao().insertSession(
            UsageSessionEntity(
                appId = appId,
                startTime = 500L,
                endTime = 1500L,
                duration = 1000
            )
        )

        // Starts inside the period but ends after it
        database.usageSessionDao().insertSession(
            UsageSessionEntity(
                appId = appId,
                startTime = 3500L,
                endTime = 5000L,
                duration = 1500
            )
        )

        val result = database.usageSessionDao()
            .getSessionsForPeriod(
                appId = appId,
                startTime = 1000L,
                endTime = 4000L
            )

        assertEquals(3, result.size)
    }

    @Test
    fun getSessionsForPeriod_excludesSessionsOutsidePeriod() = runBlocking {
        val userId = createUser()
        val appId = createApp(userId)

        // Completely before the period
        database.usageSessionDao().insertSession(
            UsageSessionEntity(
                appId = appId,
                startTime = 100L,
                endTime = 500L,
                duration = 400
            )
        )

        // Completely after the period
        database.usageSessionDao().insertSession(
            UsageSessionEntity(
                appId = appId,
                startTime = 5000L,
                endTime = 6000L,
                duration = 1000
            )
        )

        val result = database.usageSessionDao()
            .getSessionsForPeriod(
                appId = appId,
                startTime = 1000L,
                endTime = 4000L
            )

        assertTrue(result.isEmpty())
    }

    @Test
    fun deletingRestrictedApp_deletesItsUsageSessions() = runBlocking {
        val userId = createUser()
        val appId = createApp(userId)

        database.usageSessionDao().insertSession(
            UsageSessionEntity(
                appId = appId,
                startTime = 1000L,
                endTime = 2000L,
                duration = 1000
            )
        )

        val sessionsBeforeDelete = database.usageSessionDao()
            .getSessionsByAppId(appId)

        assertEquals(1, sessionsBeforeDelete.size)

        database.restrictedAppDao().deleteApp(
            database.restrictedAppDao().getAppById(appId)!!
        )

        val sessionsAfterDelete = database.usageSessionDao()
            .getSessionsByAppId(appId)

        assertTrue(sessionsAfterDelete.isEmpty())
    }
}