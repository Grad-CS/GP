
package com.example.minder.features.questionnaire

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.ui.theme.PacificoFont
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.BodoniFont
import com.example.minder.ui.theme.ManropeFont
import androidx.compose.foundation.BorderStroke

// Lama - Minder colours
private val Purple = Color(0xFF9061D8)
private val Teal = Color(0xFF38C9C5)
private val DarkTeal = Color(0xFF0E8582)
private val Pink = Color(0xFFF3A1C8)
private val Ink = Color(0xFF302342)
private val Muted = Color(0xFF655B76)
private val LightTeal = Color(0xFFE5F8F6)
private val LightPurple = Color(0xFFF5EEFC)

data class Question(
    val question: String,
    val options: List<String>
)

private val questionnaireQuestions = listOf(
    Question(
        question = "How would you rate your current smartphone usage?",
        options = listOf(
            "Very Low", "Low", "Moderate",
            "High", "Very High"
        )
    ),
    Question(
        question = "How often do you open apps without a specific purpose?",
        options = listOf(
            "Never", "Rarely", "Sometimes",
            "Often", "Very Often"
        )
    ),
    Question(
        question = "Do you feel you spend more time on some apps than you intend to?",
        options = listOf(
            "Never", "Rarely", "Sometimes",
            "Often", "Always"
        )
    )
)

@Composable
fun QuestionnaireScreen(
    onNext: (List<String>) -> Unit
) {
    var currentQuestion by remember {
        mutableIntStateOf(0)
    }

    // Lama - Preserve answers when moving between questions.
    val answers = remember {
        mutableStateListOf<String?>(
            *Array(questionnaireQuestions.size) { null }
        )
    }

    var showError by remember {
        mutableStateOf(false)
    }

    val question = questionnaireQuestions[currentQuestion]
    val selectedOption = answers[currentQuestion]
    val progress = (currentQuestion + 1).toFloat() /
            questionnaireQuestions.size

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            QuestionnaireBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 18.dp)
            ) {

                // Minder brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuestionnaireLogo()

                    Text(
                        text = "Minder",
                        fontFamily = PacificoFont,
                        fontSize = 35.sp,
                        lineHeight = 52.sp,
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(Purple, Pink)
                            )
                        )
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            LightTeal,
                            RoundedCornerShape(50)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Purple, Teal)
                                ),
                                RoundedCornerShape(50)
                            )
                    )
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = question.question,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 29.sp,
                    lineHeight = 35.sp,
                    color = Ink
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Question ${currentQuestion + 1} of " +
                            "${questionnaireQuestions.size}. Select one option.",
                    fontFamily = JakartaFont,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Muted
                )

                Spacer(Modifier.height(22.dp))

                // Scrollable options on small phones
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    question.options.forEach { option ->
                        QuestionnaireOption(
                            text = option,
                            selected = selectedOption == option,
                            onClick = {
                                answers[currentQuestion] = option
                                showError = false
                            }
                        )
                    }

                    if (showError) {
                        Text(
                            text = "Please select one option to continue.",
                            fontFamily = JakartaFont,
                            fontSize = 13.sp,
                            color = Color(0xFFB3346B),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Lama - Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Show Back only from question 2 onwards
                    if (currentQuestion > 0) {
                        OutlinedButton(
                            onClick = {
                                currentQuestion--
                                showError = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.5.dp, Teal),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Ink
                            )
                        ) {
                            Text(
                                text = "Back",
                                fontFamily = BodoniFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedOption == null) {
                                showError = true
                            } else if (
                                currentQuestion <
                                questionnaireQuestions.lastIndex
                            ) {
                                currentQuestion++
                                showError = false
                            } else {
                                onNext(answers.filterNotNull())
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkTeal
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(DarkTeal, Teal, Purple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (
                                    currentQuestion ==
                                    questionnaireQuestions.lastIndex
                                ) "Finish" else "Continue",
                                fontFamily = BodoniFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionnaireOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .background(
                if (selected) LightTeal else Color.White,
                shape
            )
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = if (selected) Teal else LightTeal,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontFamily = JakartaFont,
            fontSize = 17.sp,
            lineHeight = 23.sp,
            color = Ink
        )

        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    if (selected) Teal else Color.White,
                    CircleShape
                )
                .border(2.dp, Teal, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuestionnaireBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            color = LightTeal.copy(alpha = 0.65f),
            radius = size.width * 0.42f,
            center = Offset(
                -size.width * 0.14f,
                -size.width * 0.15f
            )
        )
        drawCircle(
            color = LightPurple.copy(alpha = 0.65f),
            radius = size.width * 0.38f,
            center = Offset(
                size.width * 1.1f,
                size.height * 0.96f
            )
        )
    }
}

@Composable
private fun QuestionnaireLogo() {
    Canvas(Modifier.size(38.dp)) {
        val sx = size.width / 40f
        val sy = size.height / 40f

        val shield = Path().apply {
            moveTo(20f * sx, 3f * sy)
            lineTo(34f * sx, 8f * sy)
            lineTo(34f * sx, 18f * sy)
            cubicTo(
                34f * sx, 27f * sy,
                28f * sx, 34f * sy,
                20f * sx, 37f * sy
            )
            cubicTo(
                12f * sx, 34f * sy,
                6f * sx, 27f * sy,
                6f * sx, 18f * sy
            )
            lineTo(6f * sx, 8f * sy)
            close()
        }

        drawPath(
            path = shield,
            color = LightPurple
        )
        drawPath(
            path = shield,
            color = Purple,
            style = Stroke(
                width = 2.2f * sx,
                join = StrokeJoin.Round
            )
        )
        drawCircle(
            color = Teal,
            radius = 7.5f * sx,
            center = Offset(20f * sx, 20f * sy),
            style = Stroke(width = 2.2f * sx)
        )
        drawLine(
            color = Teal,
            start = Offset(20f * sx, 15.5f * sy),
            end = Offset(20f * sx, 20f * sy),
            strokeWidth = 2.2f * sx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Teal,
            start = Offset(20f * sx, 20f * sy),
            end = Offset(23f * sx, 22f * sy),
            strokeWidth = 2.2f * sx,
            cap = StrokeCap.Round
        )
    }
}
