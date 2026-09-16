package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.repository.ChallengeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateChallengeUseCaseTest {

    private lateinit var useCase: GenerateChallengeUseCase
    private lateinit var repository: FakeChallengeRepository

    private val easyMathChallenge = Challenge(
        id = 1,
        question = "What is 5 + 3?",
        correctAnswer = "8",
        difficulty = "EASY",
        type = "MATH"
    )

    private val easyLogicChallenge = Challenge(
        id = 2,
        question = "Which number comes next: 2, 4, 6, ?",
        correctAnswer = "8",
        difficulty = "EASY",
        type = "LOGIC"
    )

    private val hardMathChallenge = Challenge(
        id = 3,
        question = "What is 15 × 8?",
        correctAnswer = "120",
        difficulty = "HARD",
        type = "MATH"
    )

    @Before
    fun setup() {
        repository = FakeChallengeRepository()

        useCase = GenerateChallengeUseCase(
            challengeRepository = repository
        )
    }

    @Test
    fun returnsChallengeForRequestedDifficulty() = runTest {
        repository.challengesByDifficulty = listOf(
            easyMathChallenge,
            easyLogicChallenge
        )

        val result = useCase(
            difficulty = "EASY"
        )

        assertNotNull(result)
        assertTrue(result!!.difficulty.equals("EASY", ignoreCase = true))
    }

    @Test
    fun returnsNullWhenNoChallengesMatchDifficulty() = runTest {
        repository.challengesByDifficulty = emptyList()

        val result = useCase(
            difficulty = "EASY"
        )

        assertNull(result)
    }

    @Test
    fun returnsChallengeMatchingDifficultyAndType() = runTest {
        repository.challengesByType = listOf(
            easyMathChallenge,
            hardMathChallenge
        )

        val result = useCase(
            difficulty = "EASY",
            type = "MATH"
        )

        assertNotNull(result)
        assertTrue(result!!.difficulty.equals("EASY", ignoreCase = true))
        assertTrue(result.type == "MATH")
    }

    @Test
    fun ignoresChallengesWithWrongDifficultyWhenTypeIsProvided() = runTest {
        repository.challengesByType = listOf(
            hardMathChallenge
        )

        val result = useCase(
            difficulty = "EASY",
            type = "MATH"
        )

        assertNull(result)
    }

    @Test
    fun handlesDifficultyCaseInsensitivelyWhenTypeIsProvided() = runTest {
        repository.challengesByType = listOf(
            easyMathChallenge
        )

        val result = useCase(
            difficulty = "easy",
            type = "MATH"
        )

        assertNotNull(result)
        assertTrue(result!!.difficulty.equals("EASY", ignoreCase = true))
    }

    @Test
    fun returnsNullWhenTypeHasNoChallenges() = runTest {
        repository.challengesByType = emptyList()

        val result = useCase(
            difficulty = "EASY",
            type = "MATH"
        )

        assertNull(result)
    }

    private class FakeChallengeRepository : ChallengeRepository {

        var challengesByDifficulty = emptyList<Challenge>()
        var challengesByType = emptyList<Challenge>()

        override suspend fun getChallengeById(
            id: Int
        ): Challenge? {
            return null
        }

        override suspend fun getChallengesByDifficulty(
            difficulty: String
        ): List<Challenge> {
            return challengesByDifficulty
        }

        override suspend fun getChallengesByType(
            type: String
        ): List<Challenge> {
            return challengesByType
        }

        override suspend fun getAllChallenges(): List<Challenge> {
            return challengesByDifficulty
        }
    }
}