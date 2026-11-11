
package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Challenge

class ValidateChallengeUseCase {

    //Ragahd-: Validates the user's answer according to the generated game type.
    operator fun invoke(
        challenge: Challenge,
        userAnswer: String
    ): Boolean {
        val answer = userAnswer.trim()

        //Ragahd-: Keeps mathematical answers case-insensitive and whitespace-trimmed.
        if (challenge.type == "MATH") {
            return challenge.correctAnswer.equals(answer, ignoreCase = true)
        }

        //Ragahd-: Uses the stored game identifier to distinguish visual puzzle variants.
        val gameType = Regex(""""game"\s*:\s*"([^"]+)"""")
            .find(challenge.gameData)
            ?.groupValues
            ?.getOrNull(1)

        return when (gameType) {
            "ODD_SHAPE" -> {
                //Ragahd-: Checks the selected shape index.
                challenge.correctAnswer == answer
            }

            "COMPLETE_PATTERN" -> {
                //Ragahd-: Checks the selected pattern element.
                challenge.correctAnswer.equals(answer, ignoreCase = true)
            }

            "SEQUENCE_MEMORY" -> {
                //Ragahd-: Checks the entire sequence in the original order.
                val submittedSequence = answer
                    .split(",")
                    .map { it.trim().uppercase() }

                val correctSequence = challenge.correctAnswer
                    .split(",")
                    .map { it.trim().uppercase() }

                submittedSequence == correctSequence
            }

            else -> {
                //Ragahd-: Falls back to the existing validation behavior for older challenges.
                challenge.correctAnswer.equals(answer, ignoreCase = true)
            }
        }
    }
}
