package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.ChallengeAttempt
import com.example.minder.domain.repository.ChallengeAttemptRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubmitChallengeUseCaseTest {

    private lateinit var useCase: SubmitChallengeUseCase
    private lateinit var repository: FakeChallengeAttemptRepository

    private val challenge = Challenge(
        id = 1,
        question = "What is 5 + 3?",
        correctAnswer = "8",
        difficulty = "EASY",
        type = "MATH"
    )

    @Before
    fun setup() {
        repository = FakeChallengeAttemptRepository()

        useCase = SubmitChallengeUseCase(
            validateChallengeUseCase = ValidateChallengeUseCase(),
            challengeAttemptRepository = repository
        )
    }

    @Test
    fun returnsTrueAndSavesCorrectAttempt() = runTest {
        val result = useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "8"
        )

        assertTrue(result)
        assertEquals(1, repository.attempts.size)
        assertEquals(10, repository.attempts[0].interventionId)
        assertEquals(1, repository.attempts[0].challengeId)
        assertEquals("8", repository.attempts[0].userAnswer)
        assertTrue(repository.attempts[0].isCorrect)
    }

    @Test
    fun returnsFalseAndSavesIncorrectAttempt() = runTest {
        val result = useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "9"
        )

        assertFalse(result)
        assertEquals(1, repository.attempts.size)
        assertEquals("9", repository.attempts[0].userAnswer)
        assertFalse(repository.attempts[0].isCorrect)
    }

    @Test
    fun savesAttemptWithCorrectChallengeIdAndInterventionId() = runTest {
        useCase(
            challenge = challenge,
            interventionId = 25,
            userAnswer = "8"
        )

        val savedAttempt = repository.attempts[0]

        assertEquals(25, savedAttempt.interventionId)
        assertEquals(challenge.id, savedAttempt.challengeId)
    }

    @Test
    fun savesAttemptWithUserAnswer() = runTest {
        useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "   8   "
        )

        val savedAttempt = repository.attempts[0]

        assertEquals("   8   ", savedAttempt.userAnswer)
        assertTrue(savedAttempt.isCorrect)
    }

    @Test
    fun savesAttemptWithGeneratedIdAsZero() = runTest {
        useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "8"
        )

        assertEquals(0, repository.attempts[0].id)
    }

    private class FakeChallengeAttemptRepository : ChallengeAttemptRepository {

        val attempts = mutableListOf<ChallengeAttempt>()

        override suspend fun addAttempt(attempt: ChallengeAttempt): Long {
            attempts.add(attempt)
            return attempts.size.toLong()
        }

        override suspend fun getAttemptsByInterventionId(
            interventionId: Int
        ): List<ChallengeAttempt> {
            return attempts.filter {
                it.interventionId == interventionId
            }
        }

        override suspend fun getAttemptsByChallengeId(
            challengeId: Int
        ): List<ChallengeAttempt> {
            return attempts.filter {
                it.challengeId == challengeId
            }
        }

        override suspend fun getSuccessfulAttempts(
            interventionId: Int
        ): List<ChallengeAttempt> {
            return attempts.filter {
                it.interventionId == interventionId && it.isCorrect
            }
        }

        override suspend fun getAttemptCount(
            interventionId: Int
        ): Int {
            return attempts.count {
                it.interventionId == interventionId
            }
        }
    }
}