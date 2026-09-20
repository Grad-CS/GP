package com.example.minder.features.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private val PrimaryDark = Color(0xFF061D3A)
private val CardColor = Color(0xFF0D3461)
private val PrimaryBlue = Color(0xFF3289F5)
private val BorderColor = Color(0xFF28639A)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFB8C7D9)

private data class MathQuestion(
    val number1: Int,
    val number2: Int,
    val answer: Int,
    val options: List<Int>
)

private fun generateQuestion(): MathQuestion {
    val number1 = Random.nextInt(1, 20)
    val number2 = Random.nextInt(1, 20)
    val answer = number1 + number2

    val wrongAnswers = mutableSetOf<Int>()

    while (wrongAnswers.size < 3) {
        val wrongAnswer = answer + Random.nextInt(-8, 9)

        if (wrongAnswer >= 0 && wrongAnswer != answer) {
            wrongAnswers.add(wrongAnswer)
        }
    }

    val options = (wrongAnswers + answer).shuffled()

    return MathQuestion(
        number1 = number1,
        number2 = number2,
        answer = answer,
        options = options
    )
}

@Composable
fun ChallengeScreen(
    onSuccess: () -> Unit,
    onClose: () -> Unit
) {
    var question by remember {
        mutableStateOf(generateQuestion())
    }

    var attemptsLeft by remember {
        mutableStateOf(2)
    }

    var selectedAnswer by remember {
        mutableStateOf<Int?>(null)
    }

    var isWrong by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
            .padding(horizontal = 30.dp, vertical = 30.dp)
    ) {

        Text(
            text = "×",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable {
                    onClose()
                }
                .padding(4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 185.dp)
                .border(
                    width = 2.dp,
                    color = BorderColor,
                    shape = RoundedCornerShape(28.dp)
                )
                .background(
                    color = CardColor,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(
                    horizontal = 36.dp,
                    vertical = 28.dp
                )
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = PrimaryBlue,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♣",
                        color = Color.White,
                        fontSize = 34.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Challenge Time!",
                    color = TextPrimary,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Solve the equation to continue\nusing the app.",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 25.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .border(
                            width = 1.dp,
                            color = BorderColor,
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${question.number1} + ${question.number2} = ?",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    ChallengeOption(
                        value = question.options[0],
                        selected = selectedAnswer == question.options[0],
                        wrong = isWrong && selectedAnswer == question.options[0],
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedAnswer = question.options[0]
                        isWrong = false

                        if (question.options[0] == question.answer) {
                            onSuccess()
                        } else {
                            attemptsLeft--

                            if (attemptsLeft > 0) {
                                isWrong = true
                            }
                        }
                    }

                    ChallengeOption(
                        value = question.options[1],
                        selected = selectedAnswer == question.options[1],
                        wrong = isWrong && selectedAnswer == question.options[1],
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedAnswer = question.options[1]
                        isWrong = false

                        if (question.options[1] == question.answer) {
                            onSuccess()
                        } else {
                            attemptsLeft--

                            if (attemptsLeft > 0) {
                                isWrong = true
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    ChallengeOption(
                        value = question.options[2],
                        selected = selectedAnswer == question.options[2],
                        wrong = isWrong && selectedAnswer == question.options[2],
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedAnswer = question.options[2]
                        isWrong = false

                        if (question.options[2] == question.answer) {
                            onSuccess()
                        } else {
                            attemptsLeft--

                            if (attemptsLeft > 0) {
                                isWrong = true
                            }
                        }
                    }

                    ChallengeOption(
                        value = question.options[3],
                        selected = selectedAnswer == question.options[3],
                        wrong = isWrong && selectedAnswer == question.options[3],
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedAnswer = question.options[3]
                        isWrong = false

                        if (question.options[3] == question.answer) {
                            onSuccess()
                        } else {
                            attemptsLeft--

                            if (attemptsLeft > 0) {
                                isWrong = true
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "◷ You have $attemptsLeft attempts left",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ChallengeOption(
    value: Int,
    selected: Boolean,
    wrong: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val backgroundColor = when {
        selected && !wrong -> PrimaryBlue
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .height(62.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(15.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected && !wrong) {
                    PrimaryBlue
                } else {
                    BorderColor
                },
                shape = RoundedCornerShape(15.dp)
            )
            .clickable(
                enabled = true,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}