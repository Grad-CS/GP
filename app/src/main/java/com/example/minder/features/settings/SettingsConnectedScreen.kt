package com.example.minder.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minder.features.setup.ChallengeDifficulty

/** Place inside the MainActivity Settings branch, above the existing shared bottom bar. */
@Composable
fun SettingsConnectedScreen(
    viewModel: SettingsViewModel,
    userId: Int,
    dailyLimitMinutes: Int?,
    onSaved: (Int) -> Unit = {}
) {
    LaunchedEffect(userId) { viewModel.load(userId) }
    val state by viewModel.uiState.collectAsState()
    when (val current = state) {
        SettingsUiState.Loading -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
        is SettingsUiState.Error -> Column(Modifier.padding(20.dp)) {
            Text(current.message)
            TextButton(onClick = { viewModel.load(userId) }) { Text("Retry") }
        }
        is SettingsUiState.Ready -> {
            Box(Modifier.fillMaxSize()) {
                SettingsScreen(
                    challengeIntervalMinutes = current.settings.challengeInterval,
                    challengeDifficulty = current.settings.challengeDifficulty,
                    onSaveSettings = { interval, difficulty: ChallengeDifficulty ->
                        viewModel.save(interval, difficulty.value, dailyLimitMinutes, onSaved)
                    }
                )
                if (current.saving) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                        color = Color(0xFF38C9C5)
                    )
                }
                current.message?.let { message ->
                    val success = message == "Changes saved successfully."
                    LaunchedEffect(message) {
                        if (success) {
                            delay(3500)
                            viewModel.clearMessage()
                        }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (success) Color(0xFFE5F8F6) else Color(0xFFFCECF5),
                        border = BorderStroke(1.dp,
                            if (success) Color(0xFF38C9C5) else Color(0xFFF3A1C8)),
                        shadowElevation = 5.dp
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (success) Color(0xFF0E8582) else Color(0xFFB3346B),
                                modifier = Modifier.size(26.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (success) "Settings saved!" else "Could not save settings",
                                    fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp, color = Color(0xFF302342)
                                )
                                Text(
                                    if (success) "Your challenge preferences are updated." else message,
                                    fontFamily = JakartaFont, fontSize = 12.sp,
                                    color = Color(0xFF655B76)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
