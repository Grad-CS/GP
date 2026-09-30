package com.example.minder.domain.usecase.intervention

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.Intervention
import com.example.minder.domain.repository.ChallengeRepository
import com.example.minder.domain.repository.InterventionRepository
import com.example.minder.domain.usecase.challenge.GenerateChallengeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartInterventionUseCaseTest {

    private lateinit var useCase: StartInterventionUseCase
    private lateinit var challengeRepository: FakeChallengeRepository
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
        challengeRepository = FakeChallengeRepository()
        interventionRepository = FakeInterventionRepository()

        val generateChallengeUseCase = GenerateChallengeUseCase(
            challengeRepository = challengeRepository
        )

        useCase = StartInterventionUseCase(
            generateChallengeUseCase = generateChallengeUseCase,
            interventionRepository = interventionRepository
        )
    }

    @Test
    fun returnsNullWhenNoChallengeIsAvailable() = runTest {

        challengeRepository.challengesByDifficulty = emptyList()

        val result = useCase(
            appId = 10,
            difficulty = "EASY"
        )

        assertNull(result)
        assertNull(interventionRepository.savedIntervention)
    }

    @Test
    fun createsInterventionWithCorrectData() = runTest {

        challengeRepository.challengesByDifficulty = listOf(challenge)

        useCase(
            appId = 10,
            difficulty = "EASY"
        )

        val intervention = interventionRepository.savedIntervention

        assertTrue(intervention != null)
        assertEquals(10, intervention!!.appId)
        assertEquals(challenge.id, intervention.challengeId)
        assertEquals("PENDING", intervention.status)
        assertEquals(null, intervention.unlockTime)
        assertEquals(0, intervention.savedTime)
    }

    @Test
    fun returnsChallengeAndInterventionId() = runTest {

        challengeRepository.challengesByDifficulty = listOf(challenge)
        interventionRepository.nextId = 25L

        val result = useCase(
            appId = 10,
            difficulty = "EASY"
        )

        assertTrue(result != null)
        assertEquals(challenge, result!!.challenge)
        assertEquals(25, result.interventionId)
    }

    private class FakeChallengeRepository : ChallengeRepository {

        var challengesByDifficulty = emptyList<Challenge>()

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
            return emptyList()
        }

        override suspend fun getAllChallenges(): List<Challenge> {
            return challengesByDifficulty
        }
    }

    private class FakeInterventionRepository :
        InterventionRepository {

        var savedIntervention: Intervention? = null
        var nextId = 1L

        override suspend fun addIntervention(
            intervention: Intervention
        ): Long {
            savedIntervention = intervention
            return nextId
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
        }
    }
}