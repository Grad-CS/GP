
package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.ChallengeType
import com.example.minder.domain.model.GeneratedChallenge
import com.example.minder.domain.repository.ChallengeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateChallengeUseCaseTest {

    private lateinit var useCase: GenerateChallengeUseCase
    private lateinit var repository: FakeChallengeRepository

    @Before
    fun setup() {
        repository = FakeChallengeRepository()

        useCase = GenerateChallengeUseCase(
            challengeRepository = repository
        )
    }

    @Test
    fun generatesAndSavesMathChallenge() = runTest {
        val result = useCase(
            type = ChallengeType.MATH,
            difficulty = "EASY"
        )

        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("MATH", result.type)
        assertEquals("EASY", result.difficulty)

        assertNotNull(repository.savedChallenge)
        assertEquals(
            ChallengeType.MATH,
            repository.savedChallenge?.challengeType
        )
    }

    @Test
    fun generatesLogicChallenge() = runTest {
        val result = useCase(
            type = ChallengeType.LOGIC,
            difficulty = "EASY"
        )

        assertEquals("LOGIC", result.type)
        assertEquals("EASY", result.difficulty)
        assertEquals(
            ChallengeType.LOGIC,
            repository.savedChallenge?.challengeType
        )
    }

    @Test
    fun generatesPuzzleChallenge() = runTest {
        val result = useCase(
            type = ChallengeType.PUZZLE,
            difficulty = "EASY",
            puzzleGame = "ODD_SHAPE"
        )

        assertEquals("PUZZLE", result.type)
        assertEquals("EASY", result.difficulty)
        assertEquals(
            ChallengeType.PUZZLE,
            repository.savedChallenge?.challengeType
        )
    }

    @Test
    fun passesRequestedDifficultyToGenerator() = runTest {
        val result = useCase(
            type = ChallengeType.MATH,
            difficulty = "HARD"
        )

        assertEquals("HARD", result.difficulty)
        assertEquals("HARD", repository.savedChallenge?.difficulty)
    }

    @Test
    fun savesGeneratedChallengeAndUsesReturnedId() = runTest {
        repository.nextChallengeId = 42

        val result = useCase(
            type = ChallengeType.MATH,
            difficulty = "MEDIUM"
        )

        assertEquals(42, result.id)
        assertTrue(repository.savedChallenge != null)
    }

    private class FakeChallengeRepository : ChallengeRepository {

        var savedChallenge: GeneratedChallenge? = null
        var nextChallengeId = 1

        override suspend fun getChallengeById(id: Int): Challenge? {
            return null
        }

        override suspend fun getChallengesByDifficulty(
            difficulty: String
        ): List<Challenge> {
            return emptyList()
        }

        override suspend fun getChallengesByType(
            type: String
        ): List<Challenge> {
            return emptyList()
        }

        override suspend fun getAllChallenges(): List<Challenge> {
            return emptyList()
        }

        //Ragahd-: Captures the generated challenge and returns a test database ID.
        override suspend fun saveGeneratedChallenge(
            challenge: GeneratedChallenge
        ): Int {
            savedChallenge = challenge
            return nextChallengeId
        }
    }
}
