package com.example.minder.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.entities.ChallengeEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChallengeDaoTest {

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

    private fun createChallenge(
        question: String,
        answer: String,
        difficulty: String,
        type: String
    ): ChallengeEntity {
        return ChallengeEntity(
            question = question,
            correctAnswer = answer,
            difficulty = difficulty,
            challengeType = type
        )
    }

    @Test
    fun insertChallenge_andGetById_returnsCorrectChallenge() = runBlocking {
        val challenge = createChallenge(
            question = "What is 2 + 2?",
            answer = "4",
            difficulty = "EASY",
            type = "MATH"
        )

        val challengeId = database.challengeDao()
            .insertChallenge(challenge)
            .toInt()

        val result = database.challengeDao()
            .getChallengeById(challengeId)

        assertNotNull(result)
        assertEquals("What is 2 + 2?", result!!.question)
        assertEquals("4", result.correctAnswer)
        assertEquals("EASY", result.difficulty)
        assertEquals("MATH", result.challengeType)
    }

    @Test
    fun getChallengesByDifficulty_returnsOnlyMatchingChallenges() = runBlocking {
        database.challengeDao().insertChallenge(
            createChallenge(
                question = "Easy question",
                answer = "4",
                difficulty = "EASY",
                type = "MATH"
            )
        )

        database.challengeDao().insertChallenge(
            createChallenge(
                question = "Hard question",
                answer = "42",
                difficulty = "HARD",
                type = "MATH"
            )
        )

        val result = database.challengeDao()
            .getChallengesByDifficulty("EASY")

        assertEquals(1, result.size)
        assertEquals("EASY", result[0].difficulty)
        assertEquals("Easy question", result[0].question)
    }

    @Test
    fun getChallengesByType_returnsOnlyMatchingChallenges() = runBlocking {
        database.challengeDao().insertChallenge(
            createChallenge(
                question = "Math question",
                answer = "4",
                difficulty = "EASY",
                type = "MATH"
            )
        )

        database.challengeDao().insertChallenge(
            createChallenge(
                question = "Logic question",
                answer = "B",
                difficulty = "MEDIUM",
                type = "LOGIC"
            )
        )

        val result = database.challengeDao()
            .getChallengesByType("LOGIC")

        assertEquals(1, result.size)
        assertEquals("LOGIC", result[0].challengeType)
        assertEquals("Logic question", result[0].question)
    }

    @Test
    fun getAllChallenges_returnsAllInsertedChallenges() = runBlocking {
        database.challengeDao().insertChallenge(
            createChallenge(
                question = "Question 1",
                answer = "A",
                difficulty = "EASY",
                type = "LOGIC"
            )
        )

        database.challengeDao().insertChallenge(
            createChallenge(
                question = "Question 2",
                answer = "B",
                difficulty = "MEDIUM",
                type = "MATH"
            )
        )

        database.challengeDao().insertChallenge(
            createChallenge(
                question = "Question 3",
                answer = "C",
                difficulty = "HARD",
                type = "PATTERN"
            )
        )

        val result = database.challengeDao()
            .getAllChallenges()

        assertEquals(3, result.size)
    }

    @Test
    fun updateChallenge_changesChallengeData() = runBlocking {
        val challengeId = database.challengeDao().insertChallenge(
            createChallenge(
                question = "Old question",
                answer = "A",
                difficulty = "EASY",
                type = "LOGIC"
            )
        ).toInt()

        val updatedChallenge = ChallengeEntity(
            challengeId = challengeId,
            question = "Updated question",
            correctAnswer = "B",
            difficulty = "MEDIUM",
            challengeType = "MATH"
        )

        database.challengeDao().updateChallenge(updatedChallenge)

        val result = database.challengeDao()
            .getChallengeById(challengeId)

        assertNotNull(result)
        assertEquals("Updated question", result!!.question)
        assertEquals("B", result.correctAnswer)
        assertEquals("MEDIUM", result.difficulty)
        assertEquals("MATH", result.challengeType)
    }

    @Test
    fun deleteChallenge_removesChallenge() = runBlocking {
        val challengeId = database.challengeDao().insertChallenge(
            createChallenge(
                question = "Question to delete",
                answer = "A",
                difficulty = "EASY",
                type = "LOGIC"
            )
        ).toInt()

        val beforeDelete = database.challengeDao()
            .getChallengeById(challengeId)

        assertNotNull(beforeDelete)

        database.challengeDao().deleteChallenge(
            beforeDelete!!
        )

        val afterDelete = database.challengeDao()
            .getChallengeById(challengeId)

        assertTrue(afterDelete == null)
    }
}