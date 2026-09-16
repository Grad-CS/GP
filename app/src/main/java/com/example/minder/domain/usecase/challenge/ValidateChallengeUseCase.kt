package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge

class ValidateChallengeUseCase {

    operator fun invoke(
        challenge: Challenge,
        userAnswer: String
    ): Boolean {
        return challenge.correctAnswer.equals(
            userAnswer.trim(),
            ignoreCase = true
        )
    }
}