
package com.example.minder.features.challenge

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.domain.model.Challenge
import com.example.minder.domain.usecase.challenge.ChallengeResult
import kotlinx.coroutines.delay
import org.json.JSONObject

private val PrimaryDark = Color(0xFF061D3A)
private val CardColor = Color(0xFF0D3461)
private val PrimaryBlue = Color(0xFF3289F5)
private val BorderColor = Color(0xFF28639A)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFB8C7D9)

private val GameShapes = listOf("CIRCLE", "SQUARE", "TRIANGLE", "STAR")

//Ragahd-: Reads the game identifier and game data saved with the challenge.
private fun readGameData(challenge: Challenge): JSONObject =
    try {
        JSONObject(challenge.gameData)
    } catch (_: Exception) {
        JSONObject()
    }

//Ragahd-: Converts a JSON array into a list of shape names.
private fun JSONObject.readStringList(key: String): List<String> {
    val array = optJSONArray(key) ?: return emptyList()
    return (0 until array.length()).map { array.optString(it) }
}

//Ragahd-: Shows the challenge and routes answers through the existing ViewModel.
@Composable
fun ChallengeScreen(
    appId: Int,
    interventionId: Int,
    viewModel: ChallengeViewModel,
    onSuccess: () -> Unit,
    onLocked: () -> Unit,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val challenge = uiState.challenge

    LaunchedEffect(uiState.result, uiState.isResultHandled) {
        if (uiState.isResultHandled) return@LaunchedEffect

        when (uiState.result) {
            ChallengeResult.Correct -> {
                viewModel.markResultHandled()
                onSuccess()
            }
            ChallengeResult.Locked -> {
                viewModel.markResultHandled()
                onLocked()
            }
            ChallengeResult.Retry, null -> Unit
        }
    }

    var selectedIndex by rememberSaveable(challenge?.id) {
        mutableStateOf(-1)
    }
    var selectedPatternShape by rememberSaveable(challenge?.id) {
        mutableStateOf("")
    }
    val enteredSequence = remember(challenge?.id) {
        mutableStateListOf<String>()
    }
    //Ragahd-: Clears the displayed sequence after an incorrect attempt.
    LaunchedEffect(uiState.isWrong) {
        if (uiState.isWrong) {
            enteredSequence.clear()
        }
    }
    var showingSequence by rememberSaveable(challenge?.id) {
        mutableStateOf(true)
    }

    val gameData = remember(challenge?.id, challenge?.gameData) {
        challenge?.let { readGameData(it) } ?: JSONObject()
    }

    val gameType = remember(challenge?.id, challenge?.gameData, challenge?.type) {
        when (challenge?.type) {
            "MATH" -> "MATH"
            else -> gameData.optString("game", "")
        }
    }

    //Ragahd-: Checks whether the current challenge contains the required game data.
    val isGameDataValid = remember(gameType, gameData.toString()) {
        when (gameType) {
            "MATH" -> true
            "ODD_SHAPE" ->
                gameData.readStringList("shapes").isNotEmpty()
            "COMPLETE_PATTERN" ->
                gameData.readStringList("pattern").isNotEmpty() &&
                        gameData.readStringList("options").isNotEmpty()
            "SEQUENCE_MEMORY" ->
                gameData.readStringList("sequence").isNotEmpty()
            else -> false
        }
    }

    //Ragahd-: Shows the memory sequence briefly before allowing the user to reproduce it.
    LaunchedEffect(challenge?.id, gameType) {
        showingSequence = gameType == "SEQUENCE_MEMORY"
        if (gameType == "SEQUENCE_MEMORY" && challenge != null) {
            delay(5000)
            showingSequence = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "×",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable { onClose() }
                .padding(4.dp)
        )

        if (uiState.isLoading || challenge == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "Loading challenge...",
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .border(2.dp, BorderColor, RoundedCornerShape(28.dp))
                    .background(CardColor, RoundedCornerShape(28.dp))
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("♣", color = PrimaryBlue, fontSize = 38.sp)
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Challenge Time!",
                    color = TextPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Solve the challenge to continue using the app.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(22.dp))

                Text(
                    text = challenge.question,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(20.dp))

                when (gameType) {
                    "MATH" -> {
                        OutlinedTextField(
                            value = uiState.userAnswer,
                            onValueChange = { viewModel.updateAnswer(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            label = {
                                Text("Your answer", color = TextSecondary)
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
                    }

                    //Ragahd-: Reads the shapes array saved by the odd shape generator.
                    "ODD_SHAPE" -> {
                        val shapes = gameData.readStringList("shapes")

                        if (shapes.isEmpty()) {
                            GameDataError()
                        } else {
                            Text(
                                "Tap the shape that is different.",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(14.dp))

                            ShapeGrid(
                                shapes = shapes,
                                selectedIndex = selectedIndex,
                                enabled = !uiState.isSubmitting && uiState.result == null,
                                onSelect = { index ->
                                    selectedIndex = index
                                    viewModel.updateAnswer(index.toString())
                                }
                            )
                        }
                    }

                    "COMPLETE_PATTERN" -> {
                        val pattern = gameData.readStringList("pattern")
                        val options = gameData.readStringList("options")

                        if (pattern.isEmpty() || options.isEmpty()) {
                            GameDataError()
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                pattern.forEach { shape ->
                                    Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .size(48.dp)
                                            .border(
                                                1.dp,
                                                BorderColor,
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (shape == "?") {
                                            Text(
                                                "?",
                                                color = PrimaryBlue,
                                                fontSize = 27.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            ShapeDrawing(
                                                shape = shape,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(18.dp))
                            Text(
                                "Choose the missing shape.",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(12.dp))

                            ShapeGrid(
                                shapes = options,
                                selectedIndex = options.indexOf(selectedPatternShape),
                                enabled = !uiState.isSubmitting && uiState.result == null,
                                onSelect = { index ->
                                    selectedPatternShape = options[index]
                                    viewModel.updateAnswer(options[index])
                                }
                            )
                        }
                    }

                    "SEQUENCE_MEMORY" -> {
                        val sequence = gameData.readStringList("sequence")

                        if (sequence.isEmpty()) {
                            GameDataError()
                        } else if (showingSequence) {
                            Text(
                                "Memorize this sequence!",
                                color = PrimaryBlue,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(18.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                sequence.forEach { shape ->
                                    ShapeDrawing(
                                        shape = shape,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(38.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "The sequence will hide shortly.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                "Tap the shapes in the remembered order.",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(sequence.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .size(35.dp)
                                            .border(
                                                1.dp,
                                                BorderColor,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (index < enteredSequence.size) {
                                            ShapeDrawing(
                                                shape = enteredSequence[index],
                                                modifier = Modifier.size(25.dp)
                                            )
                                        } else {
                                            Text(
                                                "•",
                                                color = TextSecondary,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            ShapeGrid(
                                shapes = GameShapes,
                                selectedIndex = -1,
                                enabled = !uiState.isSubmitting && uiState.result == null,
                                onSelect = { index ->
                                    if (enteredSequence.size < sequence.size) {
                                        enteredSequence.add(GameShapes[index])
                                        viewModel.updateAnswer(
                                            enteredSequence.joinToString(",")
                                        )
                                    }
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = {
                                        enteredSequence.clear()
                                        viewModel.updateAnswer("")
                                    },
                                    enabled = !uiState.isSubmitting && uiState.result == null,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BorderColor
                                    )
                                ) {
                                    Text("Clear sequence")
                                }
                            }
                        }
                    }

                    else -> GameDataError()
                }

                if (uiState.isWrong || uiState.errorMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (uiState.isWrong) {
                            "Incorrect answer. Try again."
                        } else {
                            uiState.errorMessage.orEmpty()
                        },
                        color = Color(0xFFFF8A8A),
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.submitAnswer() },
                    enabled = isGameDataValid &&
                        uiState.userAnswer.isNotBlank() &&
                            !uiState.isSubmitting &&
                            !uiState.isLoading &&
                            uiState.result == null &&
                            !(gameType == "SEQUENCE_MEMORY" && showingSequence),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = BorderColor
                    )
                ) {
                    Text(
                        text = if (uiState.isSubmitting) {
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

//Ragahd-: Displays selectable shapes for the visual puzzle games.
//Ragahd-: Displays selectable shapes for the visual puzzle games.
@Composable
private fun ShapeGrid(
    shapes: List<String>,
    selectedIndex: Int,
    enabled: Boolean = true,
    onSelect: (Int) -> Unit
) {
    val columns = 3
    shapes.chunked(columns).forEachIndexed { rowIndex, rowShapes ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            rowShapes.forEachIndexed { columnIndex, shape ->
                val index = rowIndex * columns + columnIndex
                val selected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(76.dp)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) PrimaryBlue else BorderColor,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .background(
                            color = if (selected) {
                                PrimaryBlue.copy(alpha = 0.22f)
                            } else {
                                PrimaryDark.copy(alpha = 0.25f)
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = enabled) { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    ShapeDrawing(shape = shape, modifier = Modifier.size(42.dp))
                }
            }
        }
    }
}

//Ragahd-: Draws each supported geometric shape without requiring image assets.
@Composable
private fun ShapeDrawing(
    shape: String,
    modifier: Modifier = Modifier
) {
    val shapeColor = when (shape.uppercase()) {
        "CIRCLE" -> Color(0xFF55C6FF)
        "SQUARE" -> Color(0xFFFFC857)
        "TRIANGLE" -> Color(0xFFFF7B72)
        "STAR" -> Color(0xFFB69CFF)
        else -> TextSecondary
    }

    Canvas(modifier = modifier) {
        val inset = size.minDimension * 0.12f
        val left = inset
        val top = inset
        val width = size.width - inset * 2
        val height = size.height - inset * 2

        when (shape.uppercase()) {
            "CIRCLE" -> drawCircle(
                color = shapeColor,
                radius = width.coerceAtMost(height) / 2,
                center = Offset(size.width / 2, size.height / 2)
            )

            "SQUARE" -> drawRect(
                color = shapeColor,
                topLeft = Offset(left, top),
                size = Size(width, height)
            )

            "TRIANGLE" -> {
                val path = Path().apply {
                    moveTo(size.width / 2, top)
                    lineTo(left + width, top + height)
                    lineTo(left, top + height)
                    close()
                }
                drawPath(path, shapeColor)
            }

            "STAR" -> {
                val path = Path()
                val centerX = size.width / 2
                val centerY = size.height / 2
                val outerRadius = width.coerceAtMost(height) / 2
                val innerRadius = outerRadius * 0.45f

                for (point in 0 until 10) {
                    val angle = Math.PI * point / 5 - Math.PI / 2
                    val radius = if (point % 2 == 0) outerRadius else innerRadius
                    val x = centerX + (kotlin.math.cos(angle) * radius).toFloat()
                    val y = centerY + (kotlin.math.sin(angle) * radius).toFloat()

                    if (point == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, shapeColor)
            }

            else -> {
                drawCircle(
                    color = TextSecondary,
                    radius = width / 2,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

//Ragahd-: Explains when a challenge's saved visual data cannot be rendered.
@Composable
private fun GameDataError() {
    Text(
        text = "Challenge data is missing or invalid. Please close and reopen the challenge.",
        color = Color(0xFFFF8A8A),
        fontSize = 14.sp
    )
}
