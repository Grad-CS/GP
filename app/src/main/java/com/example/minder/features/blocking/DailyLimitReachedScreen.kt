package com.example.minder.features.blocking

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.ManropeFont

private val LimitPurple = Color(0xFF9061D8)
private val LimitTurquoise = Color(0xFF38C9C5)
private val LimitInk = Color(0xFF302342)
private val LimitMuted = Color(0xFF655B76)
private val LimitBorder = Color(0xFFCDEFEC)

/**
 * UI-only daily-limit screen. The host must implement extension persistence,
 * eligibility checks and monitoring updates before reporting an extension active.
 * Do not use this for the three-failed-attempt ChallengeLockedScreen.
 */
@Composable
fun DailyLimitReachedScreen(
    onExitApp: () -> Unit,
    onRequestExtension: (Int) -> Unit,
    extensionAvailable: Boolean = true,
    extensionAvailabilityLoading: Boolean = false,
    extensionActiveMinutes: Int? = null,
    remainingExtensionSeconds: Long? = null,
    isProcessing: Boolean = false,
    errorMessage: String? = null
) {
    var selectedMinutes by remember { mutableIntStateOf(0) }
    var showConfirmation by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White)
                .verticalScroll(rememberScrollState())
                .imePadding().navigationBarsPadding().statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Minder", fontFamily = OutfitFont, fontWeight = FontWeight.Bold,
                fontSize = 28.sp, color = LimitPurple)
            Spacer(Modifier.height(24.dp))
            ShieldClockIllustration()
            Spacer(Modifier.height(20.dp))
            Text("Daily Limit Reached", fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp,
                color = LimitInk, textAlign = TextAlign.Center)
            Spacer(Modifier.height(30.dp))
            if (extensionAvailabilityLoading) {
                CircularProgressIndicator(color = LimitTurquoise)
                Spacer(Modifier.height(12.dp))
                Text("Checking extension availability...", fontFamily = JakartaFont, color = LimitMuted)
            } else if (extensionActiveMinutes != null) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFFE5F8F6), RoundedCornerShape(24.dp))
                        .border(1.dp, LimitBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp), horizontalAlignment = Alignment.Start
                ) {
                    Text("Extension active", fontFamily = OutfitFont, fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold, color = LimitInk)
                    if (remainingExtensionSeconds != null) {
                        val seconds = remainingExtensionSeconds.coerceAtLeast(0)
                        Text("${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                            fontFamily = ManropeFont, fontSize = 32.sp, color = LimitPurple)
                    }
                    Text("$extensionActiveMinutes extra minutes selected.",
                        fontFamily = JakartaFont, fontSize = 14.sp, color = LimitMuted)
                }
            } else if (!extensionAvailable) {
                Box(
                    Modifier.fillMaxWidth().background(Color(0xFFF5EEFC), RoundedCornerShape(24.dp))
                        .padding(22.dp)
                ) {
                    Text("No more extensions are available today.",
                        fontFamily = JakartaFont, fontSize = 15.sp, color = LimitInk)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("Need a Little More Time?", fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = LimitInk)
                    Spacer(Modifier.height(5.dp))
                    Text("Choose how much extra time you'd like for this app.",
                        fontFamily = JakartaFont, fontSize = 14.sp, color = LimitMuted)
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        listOf(10, 15, 20).forEach { minutes ->
                            val selected = selectedMinutes == minutes
                            Column(
                                modifier = Modifier.weight(1f).height(83.dp)
                                    .background(if (selected) LimitTurquoise else Color(0xFFF1FCFB), RoundedCornerShape(19.dp))
                                    .border(1.5.dp, LimitTurquoise, RoundedCornerShape(19.dp))
                                    .clickable(enabled = !isProcessing) { selectedMinutes = minutes },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("$minutes", fontFamily = ManropeFont, fontSize = 25.sp,
                                    fontWeight = FontWeight.SemiBold, color = LimitInk)
                                Text("min", fontFamily = JakartaFont, fontSize = 13.sp, color = LimitInk)
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = { showConfirmation = true },
                        enabled = selectedMinutes != 0 && !isProcessing,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = LimitTurquoise,
                            contentColor = LimitInk)
                    ) {
                        Text(if (isProcessing) "Please wait..." else "Extend Time",
                            fontFamily = OutfitFont, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onExitApp,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, LimitTurquoise),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LimitInk)
            ) {
                Text("Exit App", fontFamily = OutfitFont, fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold)
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage, fontFamily = JakartaFont, color = Color(0xFFA8456F),
                    fontSize = 13.sp, textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth())
            }
        }
        if (showConfirmation) {
            AlertDialog(
                onDismissRequest = { if (!isProcessing) showConfirmation = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Confirm Extension", fontFamily = OutfitFont, color = LimitInk) },
                text = { Text("Add $selectedMinutes extra minutes?", fontFamily = JakartaFont, color = LimitMuted) },
                confirmButton = {
                    TextButton(enabled = !isProcessing, onClick = {
                        showConfirmation = false
                        onRequestExtension(selectedMinutes)
                    }) { Text("Confirm Extension", fontFamily = OutfitFont, color = LimitPurple) }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmation = false }) {
                        Text("Cancel", fontFamily = JakartaFont, color = LimitMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun ShieldClockIllustration() {
    Canvas(Modifier.size(204.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(Color(0xFFF1FCFB), radius = size.minDimension * .49f)
        drawCircle(Color(0xFFE5F8F6), radius = size.minDimension * .38f)
        val s = size.minDimension / 240f
        val shield = Path().apply {
            moveTo(120*s, 40*s); lineTo(176*s, 60*s); lineTo(176*s, 102*s)
            cubicTo(176*s, 144*s, 150*s, 170*s, 120*s, 184*s)
            cubicTo(90*s, 170*s, 64*s, 144*s, 64*s, 102*s)
            lineTo(64*s, 60*s); close()
        }
        drawPath(shield, Color.White)
        drawPath(shield, LimitTurquoise, style = Stroke(width = 5*s))
        drawCircle(LimitPurple, radius = 32*s, center = Offset(120*s,110*s),
            style = Stroke(width = 5*s))
        drawLine(LimitPurple, Offset(120*s,90*s), Offset(120*s,110*s),
            strokeWidth = 5*s, cap = StrokeCap.Round)
        drawLine(LimitPurple, Offset(120*s,110*s), Offset(133*s,118*s),
            strokeWidth = 5*s, cap = StrokeCap.Round)
        drawCircle(LimitTurquoise, radius = 20*s, center = Offset(158*s,156*s))
        drawLine(Color.White, Offset(152*s,148*s), Offset(152*s,164*s),
            strokeWidth = 5*s, cap = StrokeCap.Round)
        drawLine(Color.White, Offset(164*s,148*s), Offset(164*s,164*s),
            strokeWidth = 5*s, cap = StrokeCap.Round)
    }
}
