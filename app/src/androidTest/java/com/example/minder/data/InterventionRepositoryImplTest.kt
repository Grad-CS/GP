package com.example.minder.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.entities.ChallengeEntity
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.domain.model.Intervention
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test



class InterventionRepositoryImplTest {

    private lateinit var database: MinderDatabase
    private lateinit var repository: InterventionRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = InterventionRepositoryImpl(
            database.interventionDao()
        )
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

    private suspend fun createChallenge(): Int {
        return database.challengeDao().insertChallenge(
            ChallengeEntity(
                question = "What is 2 + 2?",
                correctAnswer = "4",
                difficulty = "EASY",
                challengeType = "MATH"
            )
        ).toInt()
    }

    private suspend fun insertIntervention(
        appId: Int,
        challengeId: Int,
        triggerTime: Long,
        unlockTime: Long? = null,
        status: String = "PENDING",
        savedTime: Int = 0
    ): Int {
        return database.interventionDao().insertIntervention(
            com.example.minder.data.local.entities.InterventionEntity(
                appId = appId,
                challengeId = challengeId,
                triggerTime = triggerTime,
                unlockTime = unlockTime,
                status = status,
                savedTime = savedTime
            )
        ).toInt()
    }

    @Test
    fun addIntervention_returnsIdAndSavesIntervention() = runTest {
        val userId = createUser()
        val appId = createApp(
            userId = userId,
            appName = "Instagram",
            packageName = "com.instagram.android"
        )
        val challengeId = createChallenge()

        val intervention = Intervention(
            id = 0,
            appId = appId,
            challengeId = challengeId,
            triggerTime = 1000L,
            unlockTime = null,
            status = "PENDING",
            savedTime = 0
        )

        val interventionId = repository.addIntervention(intervention)

        val saved = repository.getInterventionById(
            interventionId.toInt()
        )

        assertNotNull(saved)

        saved!!

        assertEquals(interventionId.toInt(), saved.id)
        assertEquals(appId, saved.appId)
        assertEquals(challengeId, saved.challengeId)
        assertEquals(1000L, saved.triggerTime)
        assertNull(saved.unlockTime)
        assertEquals("PENDING", saved.status)
        assertEquals(0, saved.savedTime)
    }

    @Test
    fun getInterventionById_returnsCorrectIntervention() = runTest {
        val userId = createUser()
        val appId = createApp(
            userId = userId,
            appName = "YouTube",
            packageName = "com.youtube"
        )
        val challengeId = createChallenge()

        val interventionId = insertIntervention(
            appId = appId,
            challengeId = challengeId,
            triggerTime = 5000L,
            status = "UNLOCKED",
            unlockTime = 8000L,
            savedTime = 120
        )

        val result = repository.getInterventionById(interventionId)

        assertNotNull(result)

        result!!

        assertEquals(interventionId, result.id)
        assertEquals(appId, result.appId)
        assertEquals(challengeId, result.challengeId)
        assertEquals(5000L, result.triggerTime)
        assertEquals(8000L, result.unlockTime)
        assertEquals("UNLOCKED", result.status)
        assertEquals(120, result.savedTime)
    }

    @Test
    fun getInterventionsByAppId_returnsOnlyCorrectAppInterventions() = runTest {
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

        val challengeId = createChallenge()

        insertIntervention(
            appId = instagramId,
            challengeId = challengeId,
            triggerTime = 1000L
        )

        insertIntervention(
            appId = instagramId,
            challengeId = challengeId,
            triggerTime = 2000L
        )

        insertIntervention(
            appId = youtubeId,
            challengeId = challengeId,
            triggerTime = 3000L
        )

        val result = repository.getInterventionsByAppId(
            instagramId
        )

        assertEquals(2, result.size)

        assertTrue(
            result.all {
                it.appId == instagramId
            }
        )
    }

    @Test
    fun getInterventionsForPeriod_returnsOnlyInterventionsInsidePeriod() = runTest {
        val userId = createUser()

        val appId = createApp(
            userId = userId,
            appName = "TikTok",
            packageName = "com.tiktok"
        )

        val challengeId = createChallenge()

        insertIntervention(
            appId = appId,
            challengeId = challengeId,
            triggerTime = 1000L
        )

        insertIntervention(
            appId = appId,
            challengeId = challengeId,
            triggerTime = 3000L
        )

        insertIntervention(
            appId = appId,
            challengeId = challengeId,
            triggerTime = 7000L
        )

        val result = repository.getInterventionsForPeriod(
            appId = appId,
            startTime = 2000L,
            endTime = 6000L
        )

        assertEquals(1, result.size)
        assertEquals(3000L, result[0].triggerTime)
    }

    @Test
    fun completeIntervention_setsUnlockedStatusAndUnlockTime() = runTest {
        val userId = createUser()

        val appId = createApp(
            userId = userId,
            appName = "Instagram",
            packageName = "com.instagram.android"
        )

        val challengeId = createChallenge()

        val interventionId = insertIntervention(
            appId = appId,
            challengeId = challengeId,
            triggerTime = 1000L
        )

        repository.completeIntervention(
            interventionId = interventionId,
            unlockTime = 5000L,
            status = "UNLOCKED"
        )

        val result = repository.getInterventionById(
            interventionId
        )

        assertNotNull(result)

        result!!

        assertEquals("UNLOCKED", result.status)
        assertEquals(5000L, result.unlockTime)
    }

    @Test
    fun completeIntervention_setsLockedStatusAndNullUnlockTime() = runTest {
        val userId = createUser()

        val appId = createApp(
            userId = userId,
            appName = "TikTok",
            packageName = "com.tiktok"
        )

        val challengeId = createChallenge()

        val interventionId = insertIntervention(
            appId = appId,
            challengeId = challengeId,
            triggerTime = 1000L
        )

        repository.completeIntervention(
            interventionId = interventionId,
            unlockTime = null,
            status = "LOCKED"
        )

        val result = repository.getInterventionById(
            interventionId
        )

        assertNotNull(result)

        result!!

        assertEquals("LOCKED", result.status)
        assertNull(result.unlockTime)
    }
}