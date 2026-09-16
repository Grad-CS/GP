package com.example.minder.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.entities.ChallengeEntity
import com.example.minder.data.local.entities.InterventionEntity
import com.example.minder.data.local.entities.RestrictedAppEntity
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
class InterventionDaoTest {

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

    private fun createIntervention(
        appId: Int,
        challengeId: Int,
        triggerTime: Long
    ): InterventionEntity {
        return InterventionEntity(
            appId = appId,
            challengeId = challengeId,
            triggerTime = triggerTime,
            unlockTime = null,
            status = "TRIGGERED",
            savedTime = 0
        )
    }

    @Test
    fun insertIntervention_andGetById_returnsCorrectIntervention() = runBlocking {
        val userId = createUser()
        val appId = createApp(userId)
        val challengeId = createChallenge()

        val interventionId = database.interventionDao()
            .insertIntervention(
                createIntervention(
                    appId = appId,
                    challengeId = challengeId,
                    triggerTime = 1000L
                )
            )
            .toInt()

        val result = database.interventionDao()
            .getInterventionById(interventionId)

        assertNotNull(result)
        assertEquals(appId, result!!.appId)
        assertEquals(challengeId, result.challengeId)
        assertEquals(1000L, result.triggerTime)
        assertEquals("TRIGGERED", result.status)
        assertEquals(0, result.savedTime)
        assertTrue(result.unlockTime == null)
    }

    @Test
    fun getInterventionsByAppId_returnsOnlyInterventionsForGivenApp() = runBlocking {
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

        val challengeId = createChallenge()

        database.interventionDao().insertIntervention(
            createIntervention(appId1, challengeId, 1000L)
        )

        database.interventionDao().insertIntervention(
            createIntervention(appId2, challengeId, 2000L)
        )

        val result = database.interventionDao()
            .getInterventionsByAppId(appId1)

        assertEquals(1, result.size)
        assertEquals(appId1, result[0].appId)
    }

    @Test
    fun getInterventionsByChallengeId_returnsOnlyInterventionsForGivenChallenge() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)

            val challengeId1 = createChallenge()

            val challengeId2 = database.challengeDao().insertChallenge(
                ChallengeEntity(
                    question = "What is 5 + 5?",
                    correctAnswer = "10",
                    difficulty = "EASY",
                    challengeType = "MATH"
                )
            ).toInt()

            database.interventionDao().insertIntervention(
                createIntervention(appId, challengeId1, 1000L)
            )

            database.interventionDao().insertIntervention(
                createIntervention(appId, challengeId2, 2000L)
            )

            val result = database.interventionDao()
                .getInterventionsByChallengeId(challengeId1)

            assertEquals(1, result.size)
            assertEquals(challengeId1, result[0].challengeId)
        }

    @Test
    fun getInterventionsForPeriod_returnsInterventionsInsidePeriod() = runBlocking {
        val userId = createUser()
        val appId = createApp(userId)
        val challengeId = createChallenge()

        database.interventionDao().insertIntervention(
            createIntervention(appId, challengeId, 1000L)
        )

        database.interventionDao().insertIntervention(
            createIntervention(appId, challengeId, 2000L)
        )

        database.interventionDao().insertIntervention(
            createIntervention(appId, challengeId, 3000L)
        )

        val result = database.interventionDao()
            .getInterventionsForPeriod(
                appId = appId,
                startTime = 1500L,
                endTime = 3500L
            )

        assertEquals(2, result.size)
        assertTrue(result.all { it.triggerTime >= 1500L })
        assertTrue(result.all { it.triggerTime < 3500L })
    }

    @Test
    fun getInterventionsForPeriod_excludesInterventionsOutsidePeriod() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)
            val challengeId = createChallenge()

            database.interventionDao().insertIntervention(
                createIntervention(appId, challengeId, 500L)
            )

            database.interventionDao().insertIntervention(
                createIntervention(appId, challengeId, 5000L)
            )

            val result = database.interventionDao()
                .getInterventionsForPeriod(
                    appId = appId,
                    startTime = 1000L,
                    endTime = 4000L
                )

            assertTrue(result.isEmpty())
        }

    @Test
    fun completeIntervention_withUnlockStatus_updatesStatusAndUnlockTime() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)
            val challengeId = createChallenge()

            val interventionId = database.interventionDao()
                .insertIntervention(
                    createIntervention(appId, challengeId, 1000L)
                )
                .toInt()

            database.interventionDao().completeIntervention(
                interventionId = interventionId,
                unlockTime = 2000L,
                status = "UNLOCKED"
            )

            val result = database.interventionDao()
                .getInterventionById(interventionId)

            assertNotNull(result)
            assertEquals("UNLOCKED", result!!.status)
            assertEquals(2000L, result.unlockTime)
        }

    @Test
    fun completeIntervention_withLockedStatus_keepsUnlockTimeNull() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)
            val challengeId = createChallenge()

            val interventionId = database.interventionDao()
                .insertIntervention(
                    createIntervention(appId, challengeId, 1000L)
                )
                .toInt()

            database.interventionDao().completeIntervention(
                interventionId = interventionId,
                unlockTime = null,
                status = "LOCKED"
            )

            val result = database.interventionDao()
                .getInterventionById(interventionId)

            assertNotNull(result)
            assertEquals("LOCKED", result!!.status)
            assertTrue(result.unlockTime == null)
        }
}