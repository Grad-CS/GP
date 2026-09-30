package com.example.minder.features.questionnaire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrimaryDark = Color(0xFF071B33)
private val PrimaryBlue = Color(0xFF3289F5)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB8C7D9)
private val BorderColor = Color(0xFF28517D)

data class Question(
    val question: String,
    val options: List<String>
)

@Composable
fun QuestionnaireScreen(
    onNext: () -> Unit
) {
    val questions = listOf(

        Question(
            question = "How would you rate your current smartphone usage?",
            options = listOf(
                "Very Low",
                "Low",
                "Moderate",
                "High",
                "Very High"
            )
        ),

        Question(
            question = "How often do you open apps without a specific purpose?",
            options = listOf(
                "Never",
                "Rarely",
                "Sometimes",
                "Often",
                "Very Often"
            )
        ),

        Question(
            question = "Do you feel you spend more time on some apps than you intend to?",
            options = listOf(
                "Never",
                "Rarely",
                "Sometimes",
                "Often",
                "Always"
            )
        )
    )

    var currentQuestion by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    val question = questions[currentQuestion]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "${currentQuestion + 1}/${questions.size}",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(45.dp))

            Text(
                text = question.question,
                color = TextPrimary,
                fontSize = 22.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Choose the option that best describes you.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                question.options.forEach { option ->

                    val isSelected = selectedOption == option

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .background(
                                color = if (isSelected) {
                                    PrimaryBlue
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) {
                                    PrimaryBlue
                                } else {
                                    BorderColor
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                selectedOption = option
                            }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {

                        Text(
                            text = option,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) {
                                FontWeight.Medium
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(
                        color = if (selectedOption != null) {
                            PrimaryBlue
                        } else {
                            Color(0xFF28517D)
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(
                        enabled = selectedOption != null
                    ) {

                        if (currentQuestion < questions.lastIndex) {

                            currentQuestion++
                            selectedOption = null

                        } else {

                            onNext()

                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = if (currentQuestion == questions.lastIndex) {
                        "Finish"
                    } else {
                        "Next"
                    },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}