
package com.example.minder.domain.model

import kotlin.random.Random

object ChallengeGenerator {

    //Ragahd-: Stores the previous question to avoid immediate repetition.
    private var lastQuestion: String? = null

    //Ragahd-: Generates a math challenge based on the selected difficulty.
    fun generateMathChallenge(difficulty: String): GeneratedChallenge {
        val level = difficulty.uppercase()

        while (true) {
            //Ragahd-: Generates three operands for two-operation challenges.
            val minNumber = if (level == "EASY") 1 else if (level == "MEDIUM") 2 else 3
            val maxNumber = if (level == "EASY") 10 else if (level == "MEDIUM") 20 else 50

            val a = Random.nextInt(minNumber, maxNumber + 1)
            val b = Random.nextInt(minNumber, maxNumber + 1)
            val c = Random.nextInt(minNumber, maxNumber + 1)

            //Ragahd-: EASY uses one operation, while MEDIUM and HARD use two.
            val op1 = listOf("+", "-", "*").random()
            val op2 = if (level == "EASY") "" else listOf("+", "-", "*").random()

            //Ragahd-: HARD sometimes groups operations using parentheses.
            val parentheses = level == "HARD" && Random.nextBoolean()

            val question: String
            val answer: Int

            if (level == "EASY") {
                val first = if (op1 == "-" && b > a) b else a
                val second = if (op1 == "-" && b > a) a else b

                question = "$first $op1 $second = ?"
                answer = calculate(first, op1, second)
            } else if (parentheses) {
                val groupLeft = Random.nextBoolean()

                if (groupLeft) {
                    question = "($a $op1 $b) $op2 $c = ?"
                    answer = calculate(calculate(a, op1, b), op2, c)
                } else {
                    question = "$a $op1 ($b $op2 $c) = ?"
                    answer = calculate(a, op1, calculate(b, op2, c))
                }
            } else {
                question = "$a $op1 $b $op2 $c = ?"

                //Ragahd-: Applies multiplication precedence when parentheses are absent.
                answer = when {
                    op1 == "*" -> calculate(calculate(a, op1, b), op2, c)
                    op2 == "*" -> calculate(a, op1, calculate(b, op2, c))
                    else -> calculate(calculate(a, op1, b), op2, c)
                }
            }

            //Ragahd-: Rejects negative answers and immediate duplicate questions.
            if (answer < 0 || question == lastQuestion) continue

            lastQuestion = question

            //Ragahd-: Stores the generated expression and operands for restoring the round.
            val gameData =
                """{"a":$a,"b":$b,"c":$c,"op1":"$op1","op2":"$op2","parentheses":$parentheses}"""

            return GeneratedChallenge(
                question = question,
                correctAnswer = answer.toString(),
                difficulty = level,
                challengeType = ChallengeType.MATH,
                gameData = gameData
            )
        }
    }

    //Ragahd-: Evaluates a single arithmetic operation.
    private fun calculate(a: Int, operation: String, b: Int): Int {
        return when (operation) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            else -> error("Unsupported operation: $operation")
        }
    }
}
