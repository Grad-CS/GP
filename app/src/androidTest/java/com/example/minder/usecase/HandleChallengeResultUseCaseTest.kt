package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.ChallengeAttempt
import com.example.minder.domain.model.Intervention
import com.example.minder.domain.repository.ChallengeAttemptRepository
import com.example.minder.domain.repository.InterventionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HandleChallengeResultUseCaseTest {

    private lateinit var useCase: HandleChallengeResultUseCase
    private lateinit var challengeAttemptRepository: FakeChallengeAttemptRepository
    private lateinit var interventionRepository: FakeInterventionRepository

    private val challenge = Challenge(
        id = 1,
        question = "What is 5 + 3?",
        correctAnswer = "8",
        difficulty = "EASY",
        type = "MATH"
    )

    @Before
    fun setup() {
        challengeAttemptRepository = FakeChallengeAttemptRepository()
        interventionRepository = FakeInterventionRepository()

        val submitChallengeUseCase = SubmitChallengeUseCase(
            validateChallengeUseCase = ValidateChallengeUseCase(),
            challengeAttemptRepository = challengeAttemptRepository
        )

        val getAttemptCountUseCase = GetAttemptCountUseCase(
            challengeAttemptRepository = challengeAttemptRepository
        )

        useCase = HandleChallengeResultUseCase(
            submitChallengeUseCase = submitChallengeUseCase,
            getAttemptCountUseCase = getAttemptCountUseCase,
            interventionRepository = interventionRepository
        )
    }

    @Test
    fun returnsCorrectWhenAnswerIsCorrect() = runTest {
        val result = useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "8"
        )

        assertEquals(ChallengeResult.Correct, result)
    }

    @Test
    fun completesInterventionAsUnlockedWhenAnswerIsCorrect() = runTest {
        useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "8"
        )

        assertEquals(10, interventionRepository.completedInterventionId)
        assertEquals("UNLOCKED", interventionRepository.completedStatus)
        assertTrue(interventionRepository.unlockTime != null)
    }

    @Test
    fun returnsRetryAfterFirstIncorrectAttempt() = runTest {
        val result = useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "9"
        )

        assertEquals(ChallengeResult.Retry, result)
    }

    @Test
    fun returnsRetryAfterSecondIncorrectAttempt() = runTest {
        challengeAttemptRepository.attempts.add(
            createIncorrectAttempt(interventionId = 10)
        )

        val result = useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "9"
        )

        assertEquals(ChallengeResult.Retry, result)
    }

    @Test
    fun returnsLockedAfterThirdIncorrectAttempt() = runTest {
        challengeAttemptRepository.attempts.add(
            createIncorrectAttempt(interventionId = 10)
        )
        challengeAttemptRepository.attempts.add(
            createIncorrectAttempt(interventionId = 10)
        )

        val result = useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "9"
        )

        assertEquals(ChallengeResult.Locked, result)
    }

    @Test
    fun setsInterventionAsLockedWithNullUnlockTime() = runTest {
        challengeAttemptRepository.attempts.add(
            createIncorrectAttempt(interventionId = 10)
        )
        challengeAttemptRepository.attempts.add(
            createIncorrectAttempt(interventionId = 10)
        )

        useCase(
            challenge = challenge,
            interventionId = 10,
            userAnswer = "9"
        )

        assertEquals(10, interventionRepository.completedInterventionId)
        assertEquals("LOCKED", interventionRepository.completedStatus)
        assertNull(interventionRepository.unlockTime)
    }

    private fun createIncorrectAttempt(
        interventionId: Int
    ): ChallengeAttempt {
        return ChallengeAttempt(
            id = 0,
            interventionId = interventionId,
            challengeId = challenge.id,
            userAnswer = "9",
            isCorrect = false,
            attemptTime = System.currentTimeMillis()
        )
    }

    private class FakeChallengeAttemptRepository :
        ChallengeAttemptRepository {

        val attempts = mutableListOf<ChallengeAttempt>()

        override suspend fun addAttempt(
            attempt: ChallengeAttempt
        ): Long {
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
                it.interventionId == interventionId &&
                        it.isCorrect
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

    private class FakeInterventionRepository :
        InterventionRepository {

        var completedInterventionId: Int? = null
        var completedStatus: String? = null
        var unlockTime: Long? = null

        override suspend fun addIntervention(
            intervention: Intervention
        ): Long {
            return 1L
        }

        override suspend fun getInterventionById(
            interventionId: Int
        ): Intervention? {
            return null
        }

        override suspend fun getInterventionsByAppId(
            appId: Int
        ): List<Intervention> {
            return emptyList()
        }

        override suspend fun getInterventionsForPeriod(
            appId: Int,
            startTime: Long,
            endTime: Long
        ): List<Intervention> {
            return emptyList()
        }

        override suspend fun completeIntervention(
            interventionId: Int,
            unlockTime: Long?,
            status: String
        ) {
            completedInterventionId = interventionId
            this.unlockTime = unlockTime
            completedStatus = status
        }
    }
}