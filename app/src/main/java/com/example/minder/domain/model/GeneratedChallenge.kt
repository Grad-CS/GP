
package com.example.minder.domain.model

//Ragahd-: Represents a newly generated challenge before it is saved to the database.
data class GeneratedChallenge(
    val question: String,
    val correctAnswer: String,
    val difficulty: String,
    val challengeType: ChallengeType,
    val gameData: String = "{}"
)
