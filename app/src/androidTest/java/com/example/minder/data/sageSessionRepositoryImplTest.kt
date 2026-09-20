package com.example.minder.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.domain.model.UsageSession
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UsageSessionRepositoryImplTest {

    private lateinit var database: MinderDatabase
    private lateinit var repository: UsageSessionRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = UsageSessionRepositoryImpl(
            database.usageSessionDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun createApp(
        userId: Int,
        appName: String,
        packageName: String
    ): Int {
        return database.restrictedAppDao().insertApp(
            RestrictedAppEntity(
                userId = userId,
                appName = appName,
                packageName = packageName,
                dailyLimit = 60,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()
    }

    private suspend fun createUser(): Int {
        return database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()
    }

    @Test
    fun addSession_returnsIdAndSavesSession() = runTest {
        val userId = createUser()
        val appId = createApp(
            userId = userId,
            appName = "Instagram",
            packageName = "com.instagram.android"
        )

        val session = UsageSession(
            id = 0,
            appId = appId,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000
        )

        val sessionId = repository.addSession(session)

        val savedSession = database.usageSessionDao()
            .getSessionById(sessionId.toInt())

        assertNotNull(savedSession)

        savedSession!!

        assertEquals(sessionId.toInt(), savedSession.sessionId)
        assertEquals(appId, savedSession.appId)
        assertEquals(1000L, savedSession.startTime)
        assertEquals(2000L, savedSession.endTime)
        assertEquals(1000, savedSession.duration)
    }

    @Test
    fun getSessionsByAppId_returnsCorrectSessions() = runTest {
        val userId = createUser()

        val appId = createApp(
            userId = userId,
            appName = "Instagram",
            packageName = "com.instagram.android"
        )

        repository.addSession(
            UsageSession(
                id = 0,
                appId = appId,
                startTime = 1000L,
                endTime = 2000L,
                duration = 1000
            )
        )

        repository.addSession(
            UsageSession(
                id = 0,
                appId = appId,
                startTime = 3000L,
                endTime = 5000L,
                duration = 2000
            )
        )

        val result = repository.getSessionsByAppId(appId)

        assertEquals(2, result.size)

        assertEquals(appId, result[0].appId)
        assertEquals(appId, result[1].appId)
    }

    @Test
    fun getSessionsByAppId_doesNotReturnSessionsFromAnotherApp() = runTest {
        val userId = createUser()

        val instagramId = createApp(
            userId = userId,
            appName = "Instagram",
            packageName = "com.instagram.android"
        )

        val youtubeId = createApp(
            userId = userId,
            appName = "YouTube",
            packageName = "com.youtube"
        )

        repository.addSession(
            UsageSession(
                id = 0,
                appId = instagramId,
                startTime = 1000L,
                endTime = 2000L,
                duration = 1000
            )
        )

        repository.addSession(
            UsageSession(
                id = 0,
                appId = youtubeId,
                startTime = 3000L,
                endTime = 5000L,
                duration = 2000
            )
        )

        val result = repository.getSessionsByAppId(instagramId)

        assertEquals(1, result.size)
        assertEquals(instagramId, result[0].appId)
    }

    @Test
    fun getSessionsForPeriod_returnsOverlappingSessions() = runTest {
        val userId = createUser()

        val appId = createApp(
            userId = userId,
            appName = "TikTok",
            packageName = "com.tiktok"
        )

        repository.addSession(
            UsageSession(
                id = 0,
                appId = appId,
                startTime = 1000L,
                endTime = 3000L,
                duration = 2000
            )
        )

        repository.addSession(
            UsageSession(
                id = 0,
                appId = appId,
                startTime = 4000L,
                endTime = 6000L,
                duration = 2000
            )
        )

        // Requested period: 2000 - 5000
        // Both sessions overlap with this period.
        val result = repository.getSessionsForPeriod(
            appId = appId,
            startTime = 2000L,
            endTime = 5000L
        )

        assertEquals(2, result.size)

        assertTrue(
            result.any {
                it.startTime == 1000L &&
                        it.endTime == 3000L
            }
        )

        assertTrue(
            result.any {
                it.startTime == 4000L &&
                        it.endTime == 6000L
            }
        )
    }

    @Test
    fun getSessionsForPeriod_doesNotReturnSessionsOutsidePeriod() = runTest {
        val userId = createUser()

        val appId = createApp(
            userId = userId,
            appName = "TikTok",
            packageName = "com.tiktok"
        )

        repository.addSession(
            UsageSession(
                id = 0,
                appId = appId,
                startTime = 1000L,
                endTime = 2000L,
                duration = 1000
            )
        )

        repository.addSession(
            UsageSession(
                id = 0,
                appId = appId,
                startTime = 5000L,
                endTime = 6000L,
                duration = 1000
            )
        )

        // Requested period: 2500 - 4500
        // Neither session overlaps with this period.
        val result = repository.getSessionsForPeriod(
            appId = appId,
            startTime = 2500L,
            endTime = 4500L
        )

        assertTrue(result.isEmpty())
    }
}