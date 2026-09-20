package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.ChallengeAttempt
import com.example.minder.domain.repository.ChallengeAttemptRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAttemptCountUseCaseTest {

    private lateinit var useCase: GetAttemptCountUseCase
    private lateinit var repository: FakeChallengeAttemptRepository

    @Before
    fun setup() {
        repository = FakeChallengeAttemptRepository()

        useCase = GetAttemptCountUseCase(
            challengeAttemptRepository = repository
        )
    }

    @Test
    fun returnsZeroWhenThereAreNoAttempts() = runTest {
        val result = useCase(
            interventionId = 10
        )

        assertEquals(0, result)
    }

    @Test
    fun returnsCorrectAttemptCount() = runTest {
        repository.attempts.add(
            createAttempt(interventionId = 10)
        )
        repository.attempts.add(
            createAttempt(interventionId = 10)
        )

        val result = useCase(
            interventionId = 10
        )

        assertEquals(2, result)
    }

    @Test
    fun countsOnlyAttemptsForRequestedIntervention() = runTest {
        repository.attempts.add(
            createAttempt(interventionId = 10)
        )
        repository.attempts.add(
            createAttempt(interventionId = 10)
        )
        repository.attempts.add(
            createAttempt(interventionId = 20)
        )

        val result = useCase(
            interventionId = 10
        )

        assertEquals(2, result)
    }

    @Test
    fun returnsOneWhenThereIsOneAttempt() = runTest {
        repository.attempts.add(
            createAttempt(interventionId = 10)
        )

        val result = useCase(
            interventionId = 10
        )

        assertEquals(1, result)
    }

    private fun createAttempt(
        interventionId: Int
    ): ChallengeAttempt {
        return ChallengeAttempt(
            id = 0,
            interventionId = interventionId,
            challengeId = 1,
            userAnswer = "8",
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
}