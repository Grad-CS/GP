
package com.example.minder.features.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.ui.theme.PacificoFont
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.ManropeFont
import com.example.minder.ui.theme.BodoniFont

// Lama - Minder design system
private val Purple = Color(0xFF9061D8)
private val Teal = Color(0xFF38C9C5)
private val DarkTeal = Color(0xFF0E8582)
private val Pink = Color(0xFFF3A1C8)
private val Ink = Color(0xFF302342)
private val Muted = Color(0xFF655B76)
private val LightTeal = Color(0xFFE5F8F6)
private val PaleTeal = Color(0xFFF1FCFB)
private val LightPurple = Color(0xFFF5EEFC)
private val LightPink = Color(0xFFFCECF5)
private val ErrorRed = Color(0xFFB3346B)

data class ChallengeIntervalOption(
    val minutes: Int,
    val label: String
)

// Keep the existing options available to other files.
val challengeIntervals = listOf(
    ChallengeIntervalOption(10, "Every 10 minutes"),
    ChallengeIntervalOption(15, "Every 15 minutes"),
    ChallengeIntervalOption(20, "Every 20 minutes"),
    ChallengeIntervalOption(30, "Every 30 minutes")
)

// Lama - Challenge difficulty configuration
enum class ChallengeDifficulty(
    val value: String,
    val title: String,
    val description: String
) {
    EASY(
        "easy",
        "Easy",
        "Simple questions to get started."
    ),
    MEDIUM(
        "medium",
        "Medium",
        "A balanced challenge for everyday focus."
    ),
    HARD(
        "hard",
        "Hard",
        "More demanding questions to test your focus."
    )
}

@Composable
fun ChallengeSetupScreen(
    dailyLimitMinutes: Int,
    onBackClick: () -> Unit = {},
    onNextClick: (selectedIntervalMinutes: Int) -> Unit = {},
    // New callback for interval + difficulty.
    onSettingsSaved: (
        intervalMinutes: Int,
        difficulty: ChallengeDifficulty
    ) -> Unit = { _, _ -> }
) {
    var selectedInterval by remember {
        mutableIntStateOf(20)
    }

    var selectedDifficulty by remember {
        mutableStateOf(ChallengeDifficulty.MEDIUM)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 12.dp
                            )
                    ) {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage.orEmpty(),
                                fontFamily = JakartaFont,
                                fontSize = 13.sp,
                                color = ErrorRed
                            )

                            Spacer(Modifier.height(10.dp))
                        }

                        Button(
                            onClick = {
                                // Preserve the existing validation.
                                if (
                                    dailyLimitMinutes <= 0 ||
                                    selectedInterval >= dailyLimitMinutes
                                ) {
                                    errorMessage =
                                        "Challenge interval " +
                                                "($selectedInterval min) must be " +
                                                "shorter than the daily limit " +
                                                "($dailyLimitMinutes min)."
                                } else {
                                    errorMessage = null

                                    onSettingsSaved(
                                        selectedInterval,
                                        selectedDifficulty
                                    )

                                    // Existing navigation callback.
                                    onNextClick(selectedInterval)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
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
                                            listOf(
                                                DarkTeal,
                                                Teal,
                                                Purple
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Save Interval",
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
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                LightTeal,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "Minder",
                        fontFamily = PacificoFont,
                        fontSize = 32.sp,
                        color = Purple
                    )

                    Spacer(Modifier.weight(1f))

                    Spacer(Modifier.width(46.dp))
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Challenge Interval",
                    fontFamily = OutfitFont,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Choose how often a challenge " +
                            "appears to remind you to take " +
                            "a break from watching.",
                    fontFamily = JakartaFont,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    color = Muted
                )

                Spacer(Modifier.height(22.dp))

                // Interval slider card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            PaleTeal,
                            RoundedCornerShape(24.dp)
                        )
                        .border(
                            1.5.dp,
                            LightTeal,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show a challenge every",
                            fontFamily = JakartaFont,
                            fontSize = 14.sp,
                            color = Muted,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "$selectedInterval min",
                            fontFamily = ManropeFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = Ink
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = selectedInterval.toFloat(),
                        onValueChange = {
                            selectedInterval =
                                it.toInt().coerceIn(10, 30)
                            errorMessage = null
                        },
                        valueRange = 10f..30f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = Teal,
                            activeTrackColor = Teal,
                            inactiveTrackColor = LightTeal
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        listOf(10, 20, 30).forEach { minutes ->
                            TextButton(
                                onClick = {
                                    selectedInterval = minutes
                                    errorMessage = null
                                }
                            ) {
                                Text(
                                    text = "$minutes",
                                    fontFamily = ManropeFont,
                                    fontSize = 16.sp,
                                    color = if (
                                        selectedInterval == minutes
                                    ) {
                                        DarkTeal
                                    } else {
                                        Muted
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "minutes",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = JakartaFont,
                        fontSize = 13.sp,
                        color = Muted
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Challenge Difficulty",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = Ink
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Choose how challenging you " +
                            "want your mind exercises to be.",
                    fontFamily = JakartaFont,
                    fontSize = 14.sp,
                    color = Muted
                )

                Spacer(Modifier.height(14.dp))

                ChallengeDifficulty.entries.forEach { difficulty ->
                    val selected =
                        selectedDifficulty == difficulty

                    val accent = when (difficulty) {
                        ChallengeDifficulty.EASY -> Teal
                        ChallengeDifficulty.MEDIUM -> Purple
                        ChallengeDifficulty.HARD -> Pink
                    }

                    val background = when (difficulty) {
                        ChallengeDifficulty.EASY -> PaleTeal
                        ChallengeDifficulty.MEDIUM -> LightPurple
                        ChallengeDifficulty.HARD -> LightPink
                    }

                    DifficultyCard(
                        difficulty = difficulty,
                        selected = selected,
                        accent = accent,
                        background = background,
                        onClick = {
                            selectedDifficulty = difficulty
                            errorMessage = null
                        }
                    )

                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(10.dp))

                // Informational card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            LightTeal,
                            RoundedCornerShape(22.dp)
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = DarkTeal
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "Why can the time vary?",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Ink
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "There is no established " +
                                "universal time at which " +
                                "short-video watching begins " +
                                "to affect cognitive performance.",
                        fontFamily = JakartaFont,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = Ink
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Minder offers customizable " +
                                "periodic interventions to help " +
                                "you manage continuous watching.",
                        fontFamily = JakartaFont,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = Muted
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    difficulty: ChallengeDifficulty,
    selected: Boolean,
    accent: Color,
    background: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) Teal else accent.copy(alpha = 0.3f),
                shape
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    Color.White,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (difficulty) {
                    ChallengeDifficulty.EASY -> "✦"
                    ChallengeDifficulty.MEDIUM -> "✧"
                    ChallengeDifficulty.HARD -> "◎"
                },
                fontSize = 27.sp,
                color = accent
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = difficulty.title,
                fontFamily = OutfitFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = difficulty.description,
                fontFamily = JakartaFont,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Muted
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .border(
                    2.dp,
                    if (selected) Teal else accent,
                    CircleShape
                )
                .background(
                    if (selected) Teal else Color.White,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
