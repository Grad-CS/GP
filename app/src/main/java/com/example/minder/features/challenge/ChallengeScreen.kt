package com.example.minder.features.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.domain.model.Challenge
import com.example.minder.domain.usecase.challenge.ChallengeResult
import com.example.minder.domain.usecase.challenge.HandleChallengeResultUseCase
// Gets the same challenge linked to this intervention.
import com.example.minder.domain.usecase.intervention.GetInterventionChallengeUseCase
import kotlinx.coroutines.launch

private val PrimaryDark = Color(0xFF061D3A)
private val CardColor = Color(0xFF0D3461)
private val PrimaryBlue = Color(0xFF3289F5)
private val BorderColor = Color(0xFF28639A)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFB8C7D9)

@Composable
fun ChallengeScreen(
    appId: Int,
    interventionId: Int,
    // Ragahd: Gets the challenge already linked to the intervention.
    getInterventionChallengeUseCase: GetInterventionChallengeUseCase,
    handleChallengeResultUseCase: HandleChallengeResultUseCase,
    onSuccess: () -> Unit,
    onLocked: () -> Unit,
    onClose: () -> Unit
) {
    var challenge by remember {
        mutableStateOf<Challenge?>(null)
    }

    var userAnswer by remember {
        mutableStateOf("")
    }

    var isWrong by remember {
        mutableStateOf(false)
    }

    var isSubmitting by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    // Ragahd: Loads the same challenge saved inside the intervention.
    LaunchedEffect(interventionId) {
        challenge = getInterventionChallengeUseCase(
            interventionId = interventionId
        )
    }

    val currentChallenge = challenge

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
            .padding(
                horizontal = 30.dp,
                vertical = 30.dp
            )
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

        if (currentChallenge == null) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading challenge...",
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }

        } else {

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

                    Text(
                        text = "♣",
                        color = PrimaryBlue,
                        fontSize = 42.sp
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Text(
                        text = "Challenge Time!",
                        color = TextPrimary,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "Solve the challenge to continue\nusing the app.",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.height(36.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = BorderColor,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 22.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = currentChallenge.question,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(26.dp)
                    )

                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = {
                            userAnswer = it
                            isWrong = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text(
                                text = "Your answer",
                                color = TextSecondary
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = PrimaryBlue,
                            unfocusedIndicatorColor = BorderColor,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = PrimaryBlue
                        )
                    )

                    if (isWrong) {

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Text(
                            text = "Incorrect answer. Try again.",
                            color = Color(0xFFFF8A8A),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Button(
                        onClick = {

                            if (
                                userAnswer.isBlank() ||
                                isSubmitting
                            ) {
                                return@Button
                            }

                            isSubmitting = true

                            scope.launch {

                                val result =
                                    handleChallengeResultUseCase(
                                        challenge = currentChallenge,
                                        interventionId = interventionId,
                                        userAnswer = userAnswer
                                    )

                                isSubmitting = false

                                when (result) {

                                    ChallengeResult.Correct -> {
                                        onSuccess()
                                    }

                                    ChallengeResult.Retry -> {
                                        userAnswer = ""
                                        isWrong = true
                                    }

                                    ChallengeResult.Locked -> {
                                        onLocked()
                                    }
                                }
                            }
                        },
                        enabled = userAnswer.isNotBlank() && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            disabledContainerColor = BorderColor
                        )
                    ) {

                        Text(
                            text = if (isSubmitting) {
                                "Checking..."
                            } else {
                                "Submit Answer"
                            },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}