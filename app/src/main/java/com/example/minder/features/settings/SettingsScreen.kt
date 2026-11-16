package com.example.minder.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.features.setup.ChallengeDifficulty
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.ManropeFont

private val Ink = Color(0xFF302342)
private val Muted = Color(0xFF655B76)
private val Purple = Color(0xFF9061D8)
private val Teal = Color(0xFF38C9C5)
private val DarkTeal = Color(0xFF0E8582)
private val Pink = Color(0xFFF3A1C8)

/**
 * Lama: Settings UI matching the onboarding ChallengeSetupScreen.
 * Parent must supply stored values and persist changes in onSaveSettings.
 */
@Composable
fun SettingsScreen(
    challengeIntervalMinutes: Int? = null,
    challengeDifficulty: String? = null,
    onSaveSettings: (Int, ChallengeDifficulty) -> Unit = { _, _ -> },
    // Compatibility with existing MainActivity call sites.
    onEditChallengeInterval: () -> Unit = {},
    onChangeChallengeDifficulty: (String) -> Unit = {},
    onManageApps: () -> Unit = {},
    onOpenPermissions: () -> Unit = {},
    onOpenNotifications: () -> Unit = {}
) {
    val storedDifficulty = ChallengeDifficulty.entries.firstOrNull {
        it.value.equals(challengeDifficulty, ignoreCase = true)
    }
    var interval by remember(challengeIntervalMinutes) {
        mutableIntStateOf(challengeIntervalMinutes?.coerceIn(10, 30) ?: 20)
    }
    var difficulty by remember(challengeDifficulty) {
        mutableStateOf(storedDifficulty ?: ChallengeDifficulty.MEDIUM)
    }
    val hasLoadedValues = challengeIntervalMinutes != null && storedDifficulty != null
    val hasChanges = hasLoadedValues &&
            (interval != challengeIntervalMinutes || difficulty != storedDifficulty)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            Modifier.fillMaxSize().background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Settings", fontFamily = OutfitFont, fontSize = 30.sp,
                fontWeight = FontWeight.Bold, color = Ink)
            Text("Customise when and how your challenges appear.",
                fontFamily = JakartaFont, fontSize = 14.sp, color = Muted)

            Column(
                Modifier.fillMaxWidth()
                    .background(Color(0xFFF1FCFB), RoundedCornerShape(24.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Challenge Interval", fontFamily = OutfitFont, fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold, color = Ink)
                Text("Choose how often a challenge appears while using restricted apps.",
                    fontFamily = JakartaFont, fontSize = 13.sp, color = Muted)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Show a challenge every", fontFamily = JakartaFont,
                        fontSize = 13.sp, color = Muted)
                    Text("$interval min", fontFamily = ManropeFont,
                        fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
                Slider(
                    value = interval.toFloat(),
                    onValueChange = { interval = it.toInt().coerceIn(10, 30) },
                    valueRange = 10f..30f,
                    steps = 19,
                    enabled = hasLoadedValues,
                    colors = SliderDefaults.colors(
                        thumbColor = Teal, activeTrackColor = Teal,
                        inactiveTrackColor = Color(0xFFE5F8F6)
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(10, 20, 30).forEach { minutes ->
                        TextButton(onClick = { interval = minutes }, enabled = hasLoadedValues) {
                            Text("$minutes", fontFamily = ManropeFont,
                                color = if (interval == minutes) DarkTeal else Muted)
                        }
                    }
                }
            }

            Text("Challenge Difficulty", fontFamily = OutfitFont, fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold, color = Ink)
            Text("Choose how challenging you want your mind exercises to be.",
                fontFamily = JakartaFont, fontSize = 13.sp, color = Muted)

            ChallengeDifficulty.entries.forEach { level ->
                val accent = when (level) {
                    ChallengeDifficulty.EASY -> Teal
                    ChallengeDifficulty.MEDIUM -> Purple
                    ChallengeDifficulty.HARD -> Pink
                }
                val bg = when (level) {
                    ChallengeDifficulty.EASY -> Color(0xFFF1FCFB)
                    ChallengeDifficulty.MEDIUM -> Color(0xFFF5EEFC)
                    ChallengeDifficulty.HARD -> Color(0xFFFCECF5)
                }
                val selected = difficulty == level
                Row(
                    Modifier.fillMaxWidth()
                        .background(bg, RoundedCornerShape(20.dp))
                        .then(if (selected) Modifier else Modifier)
                        .clickable(enabled = hasLoadedValues) { difficulty = level }
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(level.title, fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Ink)
                        Text(level.description, fontFamily = JakartaFont,
                            fontSize = 12.sp, color = Muted)
                    }
                    RadioButton(
                        selected = selected,
                        onClick = { difficulty = level },
                        enabled = hasLoadedValues,
                        colors = RadioButtonDefaults.colors(selectedColor = accent)
                    )
                }
            }

            if (!hasLoadedValues) {
                Text("Loading saved challenge settings. Editing is unavailable until values are connected.",
                    fontFamily = JakartaFont, fontSize = 12.sp, color = Muted)
            }
            Button(
                onClick = { onSaveSettings(interval, difficulty) },
                enabled = hasChanges,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTeal)
            ) {
                Text("Save Changes", fontFamily = OutfitFont,
                    fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
