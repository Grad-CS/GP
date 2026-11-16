
package com.example.minder.features.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.ui.theme.PacificoFont
import kotlinx.coroutines.delay

private val SplashPurple = Color(0xFF9061D8)
private val SplashTeal = Color(0xFF38C9C5)
private val SplashPink = Color(0xFFF3A1C8)

@Composable
fun SplashScreen(
    onSplashTimeout: () -> Unit = {}
) {
    // Keep the existing startup callback.
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        // Background decorations
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                color = Color(0xFFE5F8F6),
                radius = size.width * 0.52f,
                center = Offset(
                    size.width * 1.05f,
                    -size.width * 0.12f
                )
            )

            drawCircle(
                color = Color(0xFFF5EEFC),
                radius = size.width * 0.45f,
                center = Offset(
                    -size.width * 0.12f,
                    size.height * 0.94f
                )
            )

            drawCircle(
                color = SplashTeal.copy(alpha = 0.65f),
                radius = 6.dp.toPx(),
                center = Offset(
                    size.width * 0.12f,
                    size.height * 0.22f
                )
            )

            drawCircle(
                color = SplashPink.copy(alpha = 0.7f),
                radius = 5.dp.toPx(),
                center = Offset(
                    size.width * 0.88f,
                    size.height * 0.72f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Same shield-and-clock logo as WelcomeScreen
            SplashMinderLogo(
                modifier = Modifier.size(190.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Project name
            Text(
                text = "Minder",
                modifier = Modifier.fillMaxWidth(),
                fontFamily = PacificoFont,
                fontSize = 52.sp,
                lineHeight = 76.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            SplashPurple,
                            SplashPink
                        )
                    )
                )
            )
        }
    }
}

@Composable
private fun SplashMinderLogo(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
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

        // Decorative circle
        drawCircle(
            color = SplashTeal.copy(alpha = 0.15f),
            radius = 106f * sx,
            center = point(140f, 140f),
            style = Stroke(width = 4f * sx)
        )

        // Gradient shield
        drawPath(
            path = shield,
            brush = Brush.linearGradient(
                colors = listOf(
                    SplashPurple,
                    SplashTeal
                ),
                start = point(80f, 55f),
                end = point(205f, 225f)
            )
        )

        // Clock
        drawCircle(
            color = Color.White,
            radius = 36f * sx,
            center = point(140f, 140f)
        )

        drawCircle(
            color = SplashPurple.copy(alpha = 0.18f),
            radius = 29f * sx,
            center = point(140f, 140f)
        )

        drawCircle(
            color = Color.White,
            radius = 22f * sx,
            center = point(140f, 140f)
        )

        drawLine(
            color = SplashTeal,
            start = point(140f, 127f),
            end = point(140f, 140f),
            strokeWidth = 5f * sx,
            cap = StrokeCap.Round
        )

        drawLine(
            color = SplashTeal,
            start = point(140f, 140f),
            end = point(149f, 148f),
            strokeWidth = 5f * sx,
            cap = StrokeCap.Round
        )

        // Decorative dots
        drawCircle(
            color = SplashPink,
            radius = 6f * sx,
            center = point(62f, 212f)
        )

        drawCircle(
            color = SplashTeal,
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
