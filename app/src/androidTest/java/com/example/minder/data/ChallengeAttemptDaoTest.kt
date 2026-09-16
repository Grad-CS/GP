package com.example.minder.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.entities.ChallengeAttemptEntity
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

@RunWith(AndroidJUnit4::class)
class ChallengeAttemptDaoTest {

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

    private suspend fun createChallenge(
        question: String = "What is 2 + 2?",
        answer: String = "4"
    ): Int {
        return database.challengeDao().insertChallenge(
            ChallengeEntity(
                question = question,
                correctAnswer = answer,
                difficulty = "EASY",
                challengeType = "MATH"
            )
        ).toInt()
    }

    private suspend fun createIntervention(
        appId: Int,
        challengeId: Int
    ): Int {
        return database.interventionDao().insertIntervention(
            InterventionEntity(
                appId = appId,
                challengeId = challengeId,
                triggerTime = System.currentTimeMillis(),
                unlockTime = null,
                status = "TRIGGERED",
                savedTime = 0
            )
        ).toInt()
    }

    private fun createAttempt(
        interventionId: Int,
        challengeId: Int,
        answer: String,
        isCorrect: Boolean,
        attemptTime: Long
    ): ChallengeAttemptEntity {
        return ChallengeAttemptEntity(
            interventionId = interventionId,
            challengeId = challengeId,
            userAnswer = answer,
            isCorrect = isCorrect,
            attemptTime = attemptTime
        )
    }

    @Test
    fun insertAttempt_andGetAttemptsByInterventionId_returnsCorrectAttempt() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)
            val challengeId = createChallenge()
            val interventionId = createIntervention(appId, challengeId)

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId = interventionId,
                    challengeId = challengeId,
                    answer = "4",
                    isCorrect = true,
                    attemptTime = 1000L
                )
            )

            val result = database.challengeAttemptDao()
                .getAttemptsByInterventionId(interventionId)

            assertEquals(1, result.size)
            assertEquals(interventionId, result[0].interventionId)
            assertEquals(challengeId, result[0].challengeId)
            assertEquals("4", result[0].userAnswer)
            assertTrue(result[0].isCorrect)
        }

    @Test
    fun getAttemptsByInterventionId_returnsAttemptsInTimeOrder() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)
            val challengeId = createChallenge()
            val interventionId = createIntervention(appId, challengeId)

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "Wrong",
                    false,
                    3000L
                )
            )

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "4",
                    true,
                    1000L
                )
            )

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "Wrong again",
                    false,
                    2000L
                )
            )

            val result = database.challengeAttemptDao()
                .getAttemptsByInterventionId(interventionId)

            assertEquals(3, result.size)
            assertEquals(1000L, result[0].attemptTime)
            assertEquals(2000L, result[1].attemptTime)
            assertEquals(3000L, result[2].attemptTime)
        }

    @Test
    fun getAttemptsByChallengeId_returnsOnlyAttemptsForGivenChallenge() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)

            val challengeId1 = createChallenge()

            val challengeId2 = createChallenge(
                question = "What is 5 + 5?",
                answer = "10"
            )

            val interventionId1 = createIntervention(appId, challengeId1)
            val interventionId2 = createIntervention(appId, challengeId2)

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId1,
                    challengeId1,
                    "4",
                    true,
                    1000L
                )
            )

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId2,
                    challengeId2,
                    "10",
                    true,
                    2000L
                )
            )

            val result = database.challengeAttemptDao()
                .getAttemptsByChallengeId(challengeId1)

            assertEquals(1, result.size)
            assertEquals(challengeId1, result[0].challengeId)
        }

    @Test
    fun getSuccessfulAttempts_returnsOnlyCorrectAttempts() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)
            val challengeId = createChallenge()
            val interventionId = createIntervention(appId, challengeId)

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "Wrong",
                    false,
                    1000L
                )
            )

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "4",
                    true,
                    2000L
                )
            )

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "Also wrong",
                    false,
                    3000L
                )
            )

            val result = database.challengeAttemptDao()
                .getSuccessfulAttempts(interventionId)

            assertEquals(1, result.size)
            assertTrue(result[0].isCorrect)
            assertEquals("4", result[0].userAnswer)
        }

    @Test
    fun getAttemptCount_returnsCorrectNumberOfAttempts() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)
            val challengeId = createChallenge()
            val interventionId = createIntervention(appId, challengeId)

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "Wrong 1",
                    false,
                    1000L
                )
            )

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "Wrong 2",
                    false,
                    2000L
                )
            )

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "4",
                    true,
                    3000L
                )
            )

            val count = database.challengeAttemptDao()
                .getAttemptCount(interventionId)

            assertEquals(3, count)
        }

    @Test
    fun deletingIntervention_deletesItsAttempts() =
        runBlocking {
            val userId = createUser()
            val appId = createApp(userId)
            val challengeId = createChallenge()
            val interventionId = createIntervention(appId, challengeId)

            database.challengeAttemptDao().insertAttempt(
                createAttempt(
                    interventionId,
                    challengeId,
                    "Wrong",
                    false,
                    1000L
                )
            )

            val beforeDelete = database.challengeAttemptDao()
                .getAttemptsByInterventionId(interventionId)

            assertEquals(1, beforeDelete.size)

            val intervention = database.interventionDao()
                .getInterventionById(interventionId)

            assertNotNull(intervention)

            database.interventionDao().deleteIntervention(
                intervention!!
            )

            val afterDelete = database.challengeAttemptDao()
                .getAttemptsByInterventionId(interventionId)

            assertTrue(afterDelete.isEmpty())
        }
}