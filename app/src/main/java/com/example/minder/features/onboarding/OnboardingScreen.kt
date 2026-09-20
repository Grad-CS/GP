package com.example.minder.features.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrimaryDark = Color(0xFF001B3D)
private val PrimaryBlue = Color(0xFF2F8FFF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB8C7D9)
private val Green = Color(0xFF55D6B0)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
            .padding(horizontal = 24.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(58.dp))

            Text(
                text = "Minder",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(82.dp))

            Text(
                text = "Take control\nof your screen time",
                color = TextPrimary,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Set limits, face challenges,\nreclaim your focus.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(42.dp))

            OnboardingIllustration(
                modifier = Modifier.size(
                    width = 230.dp,
                    height = 220.dp
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            RowIndicators()

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(27.dp)
                    )
                    .clickable {
                        onGetStarted()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Get Started",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RowIndicators() {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 4.dp)
                .background(
                    PrimaryBlue,
                    RoundedCornerShape(4.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    Color(0xFF52708F),
                    RoundedCornerShape(4.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    Color(0xFF52708F),
                    RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
private fun OnboardingIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {

        val centerX = size.width / 2f

        val phoneWidth = size.width * 0.34f
        val phoneHeight = size.height * 0.72f

        val phoneLeft = centerX - phoneWidth / 2f
        val phoneTop = size.height * 0.14f

        drawRoundRect(
            color = Color(0xFF143A63),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(18f, 18f)
        )

        drawRoundRect(
            color = Color(0xFF08284C),
            topLeft = Offset(
                phoneLeft + 6f,
                phoneTop + 6f
            ),
            size = Size(
                phoneWidth - 12f,
                phoneHeight - 12f
            ),
            cornerRadius = CornerRadius(14f, 14f)
        )

        drawCircle(
            color = Color(0xFF234C73),
            radius = 5f,
            center = Offset(
                centerX,
                phoneTop + 18f
            )
        )

        val leaf1 = Path().apply {
            moveTo(
                centerX - 72f,
                size.height * 0.67f
            )
            cubicTo(
                centerX - 110f,
                size.height * 0.57f,
                centerX - 105f,
                size.height * 0.46f,
                centerX - 67f,
                size.height * 0.52f
            )
            cubicTo(
                centerX - 48f,
                size.height * 0.57f,
                centerX - 53f,
                size.height * 0.65f,
                centerX - 72f,
                size.height * 0.67f
            )
            close()
        }

        drawPath(
            path = leaf1,
            color = Green
        )

        val leaf2 = Path().apply {
            moveTo(
                centerX + 66f,
                size.height * 0.57f
            )
            cubicTo(
                centerX + 93f,
                size.height * 0.44f,
                centerX + 111f,
                size.height * 0.50f,
                centerX + 96f,
                size.height * 0.62f
            )
            cubicTo(
                centerX + 86f,
                size.height * 0.69f,
                centerX + 72f,
                size.height * 0.63f,
                centerX + 66f,
                size.height * 0.57f
            )
            close()
        }

        drawPath(
            path = leaf2,
            color = Color(0xFF75CFAE)
        )

        val leaf3 = Path().apply {
            moveTo(
                centerX - 57f,
                size.height * 0.78f
            )
            cubicTo(
                centerX - 85f,
                size.height * 0.69f,
                centerX - 82f,
                size.height * 0.61f,
                centerX - 52f,
                size.height * 0.65f
            )
            cubicTo(
                centerX - 36f,
                size.height * 0.69f,
                centerX - 40f,
                size.height * 0.76f,
                centerX - 57f,
                size.height * 0.78f
            )
            close()
        }

        drawPath(
            path = leaf3,
            color = Color(0xFF4DBB9B)
        )

        drawLine(
            color = Color(0xFF55D6B0),
            start = Offset(
                centerX - 67f,
                size.height * 0.69f
            ),
            end = Offset(
                centerX - 38f,
                size.height * 0.45f
            ),
            strokeWidth = 3f
        )

        drawLine(
            color = Color(0xFF75CFAE),
            start = Offset(
                centerX + 68f,
                size.height * 0.60f
            ),
            end = Offset(
                centerX + 42f,
                size.height * 0.43f
            ),
            strokeWidth = 3f
        )

        drawCircle(
            color = PrimaryBlue,
            radius = 8f,
            center = Offset(
                centerX + 74f,
                size.height * 0.30f
            )
        )

        drawCircle(
            color = Color(0xFF5BA6FF),
            radius = 5f,
            center = Offset(
                centerX - 85f,
                size.height * 0.36f
            )
        )

        drawArc(
            color = PrimaryBlue,
            startAngle = -70f,
            sweepAngle = 125f,
            useCenter = false,
            topLeft = Offset(
                phoneLeft - 18f,
                phoneTop + 30f
            ),
            size = Size(
                phoneWidth + 36f,
                phoneHeight - 40f
            ),
            style = Stroke(width = 3f)
        )
    }
}