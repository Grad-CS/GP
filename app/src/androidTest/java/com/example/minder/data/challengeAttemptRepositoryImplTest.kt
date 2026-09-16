package com.example.minder.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.entities.ChallengeAttemptEntity
import com.example.minder.data.local.entities.ChallengeEntity
import com.example.minder.data.local.entities.InterventionEntity
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.domain.model.ChallengeAttempt
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChallengeAttemptRepositoryImplTest {

    private lateinit var database: MinderDatabase
    private lateinit var repository: ChallengeAttemptRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = ChallengeAttemptRepositoryImpl(
            database.challengeAttemptDao()
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
        question: String,
        answer: String
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
                status = "PENDING",
                savedTime = 0
            )
        ).toInt()
    }

    @Test
    fun addAttempt_returnsIdAndSavesAttempt() = runTest {
        val userId = createUser()
        val appId = createApp(userId)
        val challengeId = createChallenge(
            question = "What is 2 + 2?",
            answer = "4"
        )
        val interventionId = createIntervention(
            appId = appId,
            challengeId = challengeId
        )

        val attempt = ChallengeAttempt(
            id = 0,
            interventionId = interventionId,
            challengeId = challengeId,
            userAnswer = "4",
            isCorrect = true,
            attemptTime = 1000L
        )

        val attemptId = repository.addAttempt(attempt)

        val saved = database.challengeAttemptDao()
            .getAttemptsByInterventionId(interventionId)

        assertEquals(1, saved.size)
        assertEquals(attemptId.toInt(), saved[0].attemptId)
        assertEquals(interventionId, saved[0].interventionId)
        assertEquals(challengeId, saved[0].challengeId)
        assertEquals("4", saved[0].userAnswer)
        assertTrue(saved[0].isCorrect)
        assertEquals(1000L, saved[0].attemptTime)
    }

    @Test
    fun getAttemptsByInterventionId_returnsCorrectAttempts() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        val challengeId = createChallenge(
            question = "What is 5 + 5?",
            answer = "10"
        )

        val interventionId = createIntervention(
            appId = appId,
            challengeId = challengeId
        )

        repository.addAttempt(
            ChallengeAttempt(
                id = 0,
                interventionId = interventionId,
                challengeId = challengeId,
                userAnswer = "8",
                isCorrect = false,
                attemptTime = 1000L
            )
        )

        repository.addAttempt(
            ChallengeAttempt(
                id = 0,
                interventionId = interventionId,
                challengeId = challengeId,
                userAnswer = "10",
                isCorrect = true,
                attemptTime = 2000L
            )
        )

        val result = repository.getAttemptsByInterventionId(
            interventionId
        )

        assertEquals(2, result.size)
        assertEquals("8", result[0].userAnswer)
        assertEquals("10", result[1].userAnswer)
    }

    @Test
    fun getAttemptsByChallengeId_returnsOnlyMatchingChallengeAttempts() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        val challenge1 = createChallenge(
            question = "What is 2 + 2?",
            answer = "4"
        )

        val challenge2 = createChallenge(
            question = "What is 3 + 3?",
            answer = "6"
        )

        val intervention1 = createIntervention(
            appId = appId,
            challengeId = challenge1
        )

        val intervention2 = createIntervention(
            appId = appId,
            challengeId = challenge2
        )

        repository.addAttempt(
            ChallengeAttempt(
                id = 0,
                interventionId = intervention1,
                challengeId = challenge1,
                userAnswer = "4",
                isCorrect = true,
                attemptTime = 1000L
            )
        )

        repository.addAttempt(
            ChallengeAttempt(
                id = 0,
                interventionId = intervention2,
                challengeId = challenge2,
                userAnswer = "6",
                isCorrect = true,
                attemptTime = 2000L
            )
        )

        val result = repository.getAttemptsByChallengeId(
            challenge1
        )

        assertEquals(1, result.size)
        assertEquals(challenge1, result[0].challengeId)
        assertEquals("4", result[0].userAnswer)
    }

    @Test
    fun getSuccessfulAttempts_returnsOnlyCorrectAttempts() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        val challengeId = createChallenge(
            question = "What is 10 - 5?",
            answer = "5"
        )

        val interventionId = createIntervention(
            appId = appId,
            challengeId = challengeId
        )

        repository.addAttempt(
            ChallengeAttempt(
                id = 0,
                interventionId = interventionId,
                challengeId = challengeId,
                userAnswer = "3",
                isCorrect = false,
                attemptTime = 1000L
            )
        )

        repository.addAttempt(
            ChallengeAttempt(
                id = 0,
                interventionId = interventionId,
                challengeId = challengeId,
                userAnswer = "5",
                isCorrect = true,
                attemptTime = 2000L
            )
        )

        repository.addAttempt(
            ChallengeAttempt(
                id = 0,
                interventionId = interventionId,
                challengeId = challengeId,
                userAnswer = "7",
                isCorrect = false,
                attemptTime = 3000L
            )
        )

        val result = repository.getSuccessfulAttempts(
            interventionId
        )

        assertEquals(1, result.size)
        assertTrue(result[0].isCorrect)
        assertEquals("5", result[0].userAnswer)
    }

    @Test
    fun getAttemptCount_returnsCorrectNumberOfAttempts() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        val challengeId = createChallenge(
            question = "What is 1 + 1?",
            answer = "2"
        )

        val interventionId = createIntervention(
            appId = appId,
            challengeId = challengeId
        )

        repeat(3) { index ->
            repository.addAttempt(
                ChallengeAttempt(
                    id = 0,
                    interventionId = interventionId,
                    challengeId = challengeId,
                    userAnswer = index.toString(),
                    isCorrect = false,
                    attemptTime = (index + 1) * 1000L
                )
            )
        }

        val count = repository.getAttemptCount(
            interventionId
        )

        assertEquals(3, count)
    }

    @Test
    fun repository_mapsEntityToDomainCorrectly() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        val challengeId = createChallenge(
            question = "What is 7 + 1?",
            answer = "8"
        )

        val interventionId = createIntervention(
            appId = appId,
            challengeId = challengeId
        )

        val attemptId = database.challengeAttemptDao().insertAttempt(
            ChallengeAttemptEntity(
                interventionId = interventionId,
                challengeId = challengeId,
                userAnswer = "8",
                isCorrect = true,
                attemptTime = 5000L
            )
        ).toInt()

        val result = repository.getAttemptsByInterventionId(
            interventionId
        )

        assertNotNull(result)
        assertEquals(1, result.size)

        val attempt = result[0]

        assertEquals(attemptId, attempt.id)
        assertEquals(interventionId, attempt.interventionId)
        assertEquals(challengeId, attempt.challengeId)
        assertEquals("8", attempt.userAnswer)
        assertTrue(attempt.isCorrect)
        assertEquals(5000L, attempt.attemptTime)
    }
}