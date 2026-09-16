package com.example.minder.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.entities.ChallengeEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChallengeRepositoryImplTest {

    private lateinit var database: MinderDatabase
    private lateinit var repository: ChallengeRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = ChallengeRepositoryImpl(
            database.challengeDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun insertChallenge(
        question: String,
        correctAnswer: String,
        difficulty: String,
        type: String
    ): Int {
        return database.challengeDao().insertChallenge(
            ChallengeEntity(
                question = question,
                correctAnswer = correctAnswer,
                difficulty = difficulty,
                challengeType = type
            )
        ).toInt()
    }

    @Test
    fun getChallengeById_returnsCorrectChallenge() = runTest {
        val challengeId = insertChallenge(
            question = "What is 2 + 2?",
            correctAnswer = "4",
            difficulty = "EASY",
            type = "MATH"
        )

        val result = repository.getChallengeById(challengeId)

        assertNotNull(result)

        result!!

        assertEquals(challengeId, result.id)
        assertEquals("What is 2 + 2?", result.question)
        assertEquals("4", result.correctAnswer)
        assertEquals("EASY", result.difficulty)
        assertEquals("MATH", result.type)
    }

    @Test
    fun getChallengeById_returnsNullForNonExistingChallenge() = runTest {
        val result = repository.getChallengeById(999)

        assertNull(result)
    }

    @Test
    fun getChallengesByDifficulty_returnsOnlyMatchingChallenges() = runTest {
        insertChallenge(
            question = "Easy question",
            correctAnswer = "A",
            difficulty = "EASY",
            type = "MCQ"
        )

        insertChallenge(
            question = "Another easy question",
            correctAnswer = "B",
            difficulty = "EASY",
            type = "MATH"
        )

        insertChallenge(
            question = "Hard question",
            correctAnswer = "C",
            difficulty = "HARD",
            type = "LOGIC"
        )

        val result = repository.getChallengesByDifficulty("EASY")

        assertEquals(2, result.size)

        assertTrue(
            result.all {
                it.difficulty == "EASY"
            }
        )
    }

    @Test
    fun getChallengesByType_returnsOnlyMatchingChallenges() = runTest {
        insertChallenge(
            question = "Math question",
            correctAnswer = "4",
            difficulty = "EASY",
            type = "MATH"
        )

        insertChallenge(
            question = "Another math question",
            correctAnswer = "8",
            difficulty = "MEDIUM",
            type = "MATH"
        )

        insertChallenge(
            question = "Logic question",
            correctAnswer = "YES",
            difficulty = "HARD",
            type = "LOGIC"
        )

        val result = repository.getChallengesByType("MATH")

        assertEquals(2, result.size)

        assertTrue(
            result.all {
                it.type == "MATH"
            }
        )
    }

    @Test
    fun getAllChallenges_returnsAllChallenges() = runTest {
        insertChallenge(
            question = "Question 1",
            correctAnswer = "A",
            difficulty = "EASY",
            type = "MCQ"
        )

        insertChallenge(
            question = "Question 2",
            correctAnswer = "B",
            difficulty = "MEDIUM",
            type = "MATH"
        )

        insertChallenge(
            question = "Question 3",
            correctAnswer = "C",
            difficulty = "HARD",
            type = "LOGIC"
        )

        val result = repository.getAllChallenges()

        assertEquals(3, result.size)

        assertEquals(
            setOf(
                "Question 1",
                "Question 2",
                "Question 3"
            ),
            result.map { it.question }.toSet()
        )
    }

    @Test
    fun repository_mapsEntityToDomainCorrectly() = runTest {
        val challengeId = insertChallenge(
            question = "What comes next: 2, 4, 6, ?",
            correctAnswer = "8",
            difficulty = "MEDIUM",
            type = "SEQUENCE"
        )

        val result: Challenge? = repository.getChallengeById(challengeId)

        assertNotNull(result)

        result!!

        assertEquals(
            Challenge(
                id = challengeId,
                question = "What comes next: 2, 4, 6, ?",
                correctAnswer = "8",
                difficulty = "MEDIUM",
                type = "SEQUENCE"
            ),
            result
        )
    }
}