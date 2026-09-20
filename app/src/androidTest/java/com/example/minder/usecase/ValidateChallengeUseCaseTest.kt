package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateChallengeUseCaseTest {

    private lateinit var useCase: ValidateChallengeUseCase

    private val challenge = Challenge(
        id = 1,
        question = "What is 5 + 3?",
        correctAnswer = "8",
        difficulty = "EASY",
        type = "MATH"
    )

    @Before
    fun setup() {
        useCase = ValidateChallengeUseCase()
    }

    @Test
    fun returnsTrueWhenAnswerIsCorrect() {
        val result = useCase(
            challenge = challenge,
            userAnswer = "8"
        )

        assertTrue(result)
    }

    @Test
    fun returnsTrueWhenAnswerHasDifferentLetterCase() {
        val textChallenge = challenge.copy(
            correctAnswer = "Paris"
        )

        val result = useCase(
            challenge = textChallenge,
            userAnswer = "paris"
        )

        assertTrue(result)
    }

    @Test
    fun returnsTrueWhenAnswerContainsExtraSpaces() {
        val result = useCase(
            challenge = challenge,
            userAnswer = "   8   "
        )

        assertTrue(result)
    }

    @Test
    fun returnsFalseWhenAnswerIsIncorrect() {
        val result = useCase(
            challenge = challenge,
            userAnswer = "9"
        )

        assertFalse(result)
    }

    @Test
    fun returnsFalseWhenAnswerIsEmpty() {
        val result = useCase(
            challenge = challenge,
            userAnswer = ""
        )

        assertFalse(result)
    }
}