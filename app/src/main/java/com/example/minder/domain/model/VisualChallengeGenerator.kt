
package com.example.minder.domain.model

import kotlin.random.Random

object VisualChallengeGenerator {

    //Ragahd-: Defines the visual shapes available in generated challenges.
    private val shapes = listOf("CIRCLE", "SQUARE", "TRIANGLE", "STAR")

    //Ragahd-: Normalizes the requested difficulty to a supported level.
    private fun normalizeDifficulty(difficulty: String): String =
        when (difficulty.uppercase()) {
            "MEDIUM" -> "MEDIUM"
            "HARD" -> "HARD"
            else -> "EASY"
        }

    //Ragahd-: Generates an odd-shape puzzle with a difficulty-based board size.
    fun generateOddShape(difficulty: String = "EASY"): GeneratedChallenge {
        val level = normalizeDifficulty(difficulty)

        //Ragahd-: Increases the number of shapes according to difficulty.
        val boardSize = when (level) {
            "MEDIUM" -> 7
            "HARD" -> 9
            else -> 5
        }

        val commonShape = shapes.random()
        val differentShape = shapes.filter { it != commonShape }.random()
        val board = MutableList(boardSize) { commonShape }

        board[Random.nextInt(board.size)] = differentShape
        board.shuffle()

        //Ragahd-: Finds the unique different shape after shuffling.
        val answerIndex = board.indexOf(differentShape)

        //Ragahd-: Stores the board and answer index for restoring the same round.
        val gameData = """
            {
              "game":"ODD_SHAPE",
              "shapes":${board.toJsonArray()},
              "answerIndex":$answerIndex
            }
        """.trimIndent()

        return GeneratedChallenge(
            question = "Tap the shape that is different.",
            correctAnswer = answerIndex.toString(),
            difficulty = level,
            challengeType = ChallengeType.PUZZLE,
            gameData = gameData
        )
    }

    //Ragahd-: Generates a repeating visual pattern based on difficulty.
    fun generatePattern(difficulty: String = "EASY"): GeneratedChallenge {
        val level = normalizeDifficulty(difficulty)

        //Ragahd-: Uses longer repeating cycles for higher difficulty levels.
        val cycleLength = when (level) {
            "MEDIUM" -> 3
            "HARD" -> 4
            else -> 2
        }

        val cycle = shapes.shuffled().take(cycleLength)

        //Ragahd-: Creates two repetitions with the final element hidden.
        val visibleLength = cycleLength * 2 - 1
        val visiblePattern = List(visibleLength) { cycle[it % cycleLength] }
        val answerShape = cycle[visibleLength % cycleLength]

        //Ragahd-: Includes the correct answer and distractors in the options.
        val options = shapes.shuffled()

        //Ragahd-: Stores the pattern, options, and correct answer for the UI.
        val gameData = """
            {
              "game":"COMPLETE_PATTERN",
              "pattern":${(visiblePattern + "?").toJsonArray()},
              "options":${options.toJsonArray()},
              "answer":"$answerShape"
            }
        """.trimIndent()

        return GeneratedChallenge(
            question = "Choose the shape that completes the pattern.",
            correctAnswer = answerShape,
            difficulty = level,
            challengeType = ChallengeType.LOGIC,
            gameData = gameData
        )
    }

    //Ragahd-: Generates a sequence-memory challenge with difficulty-based sequence length.
    fun generateSequenceMemory(
        difficulty: String = "EASY"
    ): GeneratedChallenge {
        val level = normalizeDifficulty(difficulty)

        //Ragahd-: Increases the sequence length according to difficulty.
        val sequenceLength = when (level) {
            "MEDIUM" -> 5
            "HARD" -> 7
            else -> 3
        }

        val sequence = List(sequenceLength) { shapes.random() }

        //Ragahd-: Stores the exact sequence that the user must reproduce.
        val gameData = """
            {
              "game":"SEQUENCE_MEMORY",
              "sequence":${sequence.toJsonArray()}
            }
        """.trimIndent()

        return GeneratedChallenge(
            question = "Memorize the shapes and repeat their order.",
            correctAnswer = sequence.joinToString(","),
            difficulty = level,
            challengeType = ChallengeType.PUZZLE,
            gameData = gameData
        )
    }

    //Ragahd-: Converts string values into a JSON array without an extra dependency.
    private fun List<String>.toJsonArray(): String =
        joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
}
