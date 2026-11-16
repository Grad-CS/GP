
package com.example.minder.features.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.minder.ui.theme.PacificoFont
import com.example.minder.ui.theme.BodoniFont
import com.example.minder.ui.theme.JakartaFont

// Lama - Minder welcome screen colour palette
private val Purple = Color(0xFF9061D8)
private val Teal = Color(0xFF38C9C5)
private val Pink = Color(0xFFF3A1C8)
private val Ink = Color(0xFF302342)
private val Muted = Color(0xFF655B76)
private val Lavender = Color(0xFFF5EEFC)
private val LightTeal = Color(0xFFE5F8F6)
private val PaleTeal = Color(0xFFF1FCFB)

// Lama - UI only. Keep navigation controlled by MainActivity.
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit = {}
) {
    val entrance = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700)
        )
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            WelcomeBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 22.dp,
                        vertical = 18.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                // Logo animation
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            alpha = entrance.value
                            translationY =
                                (1f - entrance.value) * 18.dp.toPx()
                            scaleX = 0.96f + 0.04f * entrance.value
                            scaleY = 0.96f + 0.04f * entrance.value
                        }
                ) {
                    MinderLogo()
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Brand and tagline - centred alignment
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Minder",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = PacificoFont,
                        fontSize = 49.sp,
                        lineHeight = 70.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Purple, Pink)
                            )
                        )
                    )

                    Text(
                        text = "Mind Your Time, Live Your Life",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = BodoniFont,
                        fontSize = 18.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        color = Ink,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Feature cards - 2 x 2 layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureCard(
                            symbol = "◷",
                            title = "Screen time",
                            description = "See where your day goes.",
                            bg = LightTeal,
                            accent = Teal,
                            modifier = Modifier.weight(1f)
                        )

                        FeatureCard(
                            symbol = "✓",
                            title = "Less distraction",
                            description = "Quiet the apps that pull you away.",
                            bg = Color.White,
                            accent = Teal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureCard(
                            symbol = "✦",
                            title = "Mind challenges",
                            description = "Small games that build focus.",
                            bg = Lavender,
                            accent = Purple,
                            modifier = Modifier.weight(1f)
                        )

                        FeatureCard(
                            symbol = "⚖",
                            title = "Digital balance",
                            description = "Healthy habits, one day at a time.",
                            bg = PaleTeal,
                            accent = Teal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Get Started button
                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colorStops = arrayOf(
                                        0f to Teal,
                                        0.6f to Teal,
                                        1f to Purple
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Get Started",
                            fontFamily = BodoniFont,
                            fontSize = 21.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Lama - Equal-height cards and consistent text alignment
@Composable
private fun FeatureCard(
    symbol: String,
    title: String,
    description: String,
    bg: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(180.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.22f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(14.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        // Icon aligned to top-left
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = accent,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        // Reserved title area for consistent description alignment
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 42.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = BodoniFont,
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = Ink,
                textAlign = TextAlign.Start
            )
        }

        // Description starts at the same position in all cards
        Text(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = JakartaFont,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Muted,
            textAlign = TextAlign.Start
        )
    }
}

// Lama - Decorative background
@Composable
private fun WelcomeBackground() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawCircle(
            color = LightTeal.copy(alpha = 0.7f),
            radius = size.width * 0.52f,
            center = Offset(
                size.width * 1.05f,
                -size.width * 0.12f
            )
        )

        drawCircle(
            color = PaleTeal,
            radius = size.width * 0.44f,
            center = Offset(
                -size.width * 0.12f,
                size.height * 0.94f
            )
        )

        drawCircle(
            color = Lavender.copy(alpha = 0.65f),
            radius = size.width * 0.23f,
            center = Offset(
                -size.width * 0.08f,
                size.height * 0.52f
            )
        )

        drawCircle(
            color = Teal.copy(alpha = 0.65f),
            radius = 6.dp.toPx(),
            center = Offset(
                size.width * 0.08f,
                size.height * 0.18f
            )
        )

        drawCircle(
            color = Pink.copy(alpha = 0.7f),
            radius = 5.dp.toPx(),
            center = Offset(
                size.width * 0.91f,
                size.height * 0.31f
            )
        )

        drawCircle(
            color = Teal.copy(alpha = 0.5f),
            radius = 4.dp.toPx(),
            center = Offset(
                size.width * 0.88f,
                size.height * 0.76f
            )
        )
    }
}

// Lama - Minder shield and clock logo
@Composable
private fun MinderLogo() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val sx = size.width / 280f
        val sy = size.height / 280f

        fun point(x: Float, y: Float): Offset {
            return Offset(x * sx, y * sy)
        }

        val shield = Path().apply {
            moveTo(140f * sx, 52f * sy)
            lineTo(206f * sx, 76f * sy)
            lineTo(206f * sx, 140f * sy)

            cubicTo(
                206f * sx, 182f * sy,
                176f * sx, 212f * sy,
                140f * sx, 230f * sy
            )

            cubicTo(
                104f * sx, 212f * sy,
                74f * sx, 182f * sy,
                74f * sx, 140f * sy
            )

            lineTo(74f * sx, 76f * sy)
            close()
        }

        drawCircle(
            color = Teal.copy(alpha = 0.15f),
            radius = 106f * sx,
            center = point(140f, 140f),
            style = Stroke(width = 4f * sx)
        )

        drawPath(
            path = shield,
            brush = Brush.linearGradient(
                colors = listOf(Purple, Teal),
                start = point(80f, 55f),
                end = point(205f, 225f)
            )
        )

        drawCircle(
            color = Color.White,
            radius = 36f * sx,
            center = point(140f, 140f)
        )

        drawCircle(
            color = Purple.copy(alpha = 0.18f),
            radius = 29f * sx,
            center = point(140f, 140f)
        )

        drawCircle(
            color = Color.White,
            radius = 22f * sx,
            center = point(140f, 140f)
        )

        drawLine(
            color = Teal,
            start = point(140f, 127f),
            end = point(140f, 140f),
            strokeWidth = 5f * sx,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Teal,
            start = point(140f, 140f),
            end = point(149f, 148f),
            strokeWidth = 5f * sx,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = Pink,
            radius = 6f * sx,
            center = point(62f, 212f)
        )

        drawCircle(
            color = Teal,
            radius = 5f * sx,
            center = point(232f, 66f)
        )

        drawCircle(
            color = Color(0xFFE9DDF9),
            radius = 4f * sx,
            center = point(40f, 108f)
        )
    }
}
