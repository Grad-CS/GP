package com.example.minder.data.local.database

import com.example.minder.data.local.dao.ChallengeDao
import com.example.minder.data.local.entities.ChallengeEntity

object ChallengeSeeder {

    suspend fun seedIfEmpty(
        challengeDao: ChallengeDao
    ) {

        // Lama - Prevents inserting duplicate challenges
        if (challengeDao.getAllChallenges().isNotEmpty()) {
            return
        }

        // Lama - Default challenges available to the challenge engine
        val challenges = listOf(

            ChallengeEntity(
                question = "What is 2 + 2?",
                correctAnswer = "4",
                difficulty = "EASY",
                challengeType = "MATH"
            ),

            ChallengeEntity(
                question = "What is 5 + 3?",
                correctAnswer = "8",
                difficulty = "EASY",
                challengeType = "MATH"
            ),

            ChallengeEntity(
                question = "What is 12 - 4?",
                correctAnswer = "8",
                difficulty = "EASY",
                challengeType = "MATH"
            ),

            ChallengeEntity(
                question = "What comes next: 2, 4, 6, ?",
                correctAnswer = "8",
                difficulty = "MEDIUM",
                challengeType = "SEQUENCE"
            ),

            ChallengeEntity(
                question = "What is 6 * 7?",
                correctAnswer = "42",
                difficulty = "MEDIUM",
                challengeType = "MATH"
            ),

            ChallengeEntity(
                question = "What is 15 + 27?",
                correctAnswer = "42",
                difficulty = "MEDIUM",
                challengeType = "MATH"
            ),

            ChallengeEntity(
                question = "What is 12 * 12?",
                correctAnswer = "144",
                difficulty = "HARD",
                challengeType = "MATH"
            ),

            ChallengeEntity(
                question = "What is 100 / 4?",
                correctAnswer = "25",
                difficulty = "HARD",
                challengeType = "MATH"
            )
        )

        // Lama - Inserts the default challenges into the local database
        challenges.forEach { challenge ->
            challengeDao.insertChallenge(challenge)
        }
    }
}