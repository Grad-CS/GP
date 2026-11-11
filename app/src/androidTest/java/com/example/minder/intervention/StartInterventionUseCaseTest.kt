
package com.example.minder.domain.usecase.intervention

import com.example.minder.domain.model.ChallengeType
import com.example.minder.domain.model.GeneratedChallenge
import com.example.minder.domain.model.Intervention
import com.example.minder.domain.repository.ChallengeRepository
import com.example.minder.domain.repository.InterventionRepository
import com.example.minder.domain.usecase.challenge.GenerateChallengeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartInterventionUseCaseTest {

    private lateinit var useCase: StartInterventionUseCase
    private lateinit var challengeRepository: FakeChallengeRepository
    private lateinit var interventionRepository: FakeInterventionRepository

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
    fun createsInterventionWithGeneratedChallenge() = runTest {
        val result = useCase(
            appId = 10,
            difficulty = "EASY",
            type = ChallengeType.MATH
        )

        val intervention = interventionRepository.savedIntervention

        assertNotNull(intervention)
        assertEquals(10, intervention!!.appId)
        assertEquals(result.challenge.id, intervention.challengeId)
        assertEquals("PENDING", intervention.status)
        assertNull(intervention.unlockTime)
        assertEquals(0, intervention.savedTime)

        assertEquals(1, result.challenge.id)
        assertEquals("MATH", result.challenge.type)
        assertEquals("EASY", result.challenge.difficulty)
        assertNotNull(challengeRepository.savedChallenge)
    }

    @Test
    fun returnsChallengeAndInterventionId() = runTest {
        interventionRepository.nextId = 25L
        challengeRepository.nextChallengeId = 7

        val result = useCase(
            appId = 10,
            difficulty = "HARD",
            type = ChallengeType.MATH
        )

        assertEquals(7, result.challenge.id)
        assertEquals("MATH", result.challenge.type)
        assertEquals("HARD", result.challenge.difficulty)
        assertEquals(25, result.interventionId)

        assertEquals(7, interventionRepository.savedIntervention?.challengeId)
    }

    private class FakeChallengeRepository : ChallengeRepository {

        var savedChallenge: GeneratedChallenge? = null
        var nextChallengeId = 1

        override suspend fun getChallengeById(id: Int) = null

        override suspend fun getChallengesByDifficulty(
            difficulty: String
        ) = emptyList<com.example.minder.domain.model.Challenge>()

        override suspend fun getChallengesByType(
            type: String
        ) = emptyList<com.example.minder.domain.model.Challenge>()

        override suspend fun getAllChallenges() =
            emptyList<com.example.minder.domain.model.Challenge>()

        //Ragahd-: Stores the generated challenge and returns its test database ID.
        override suspend fun saveGeneratedChallenge(
            challenge: GeneratedChallenge
        ): Int {
            savedChallenge = challenge
            return nextChallengeId
        }
    }

    private class FakeInterventionRepository : InterventionRepository {

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
        ): Intervention? = null

        override suspend fun getInterventionsByAppId(
            appId: Int
        ): List<Intervention> = emptyList()

        //Ragahd-: Returns no pending intervention in this test fake.
        override suspend fun getPendingIntervention(appId: Int): Intervention? = null

        //Ragahd-: Implements the pending intervention lookup for a user.
        override suspend fun getPendingInterventionForUser(userId: Int): Intervention? = null

        override suspend fun getInterventionsForPeriod(
            appId: Int,
            startTime: Long,
            endTime: Long
        ): List<Intervention> = emptyList()

        override suspend fun completeIntervention(
            interventionId: Int,
            unlockTime: Long?,
            status: String
        ) {
        }
    }
}
