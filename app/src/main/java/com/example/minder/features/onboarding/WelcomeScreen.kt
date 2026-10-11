package com.example.minder.features.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF031938)
private val PrimaryBlue = Color(0xFF2F82FF)
private val PhoneBorderBlue = Color(0xFF1E6FD9)
private val PhoneScreenBlue = Color(0xFF1350A6)
private val PauseCircleBg = Color(0xFFEBF3FF)
private val PauseIconBlue = Color(0xFF0B418C)
private val SparkBlue = Color(0xFF4293FF)
private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF9EACB8)

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // العنوان العلوي
        Text(
            text = "Minder",
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.CenterHorizontally)
        )

        // النص الترحيبي والصورة التوضيحية
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Take control\nof your screen time",
                color = TextWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Set limits, face challenges,\nreclaim your focus.",
                color = TextMuted,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // الشكل الرسومي المميز للجوالين وشارة التوقف
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp, 220.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. أشرطة الشرارات المضيئة الجانبية (Spark Lines)
                    drawLine(SparkBlue, Offset(w * 0.12f, h * 0.35f), Offset(w * 0.22f, h * 0.42f), strokeWidth = 8f, cap = StrokeCap.Round)
                    drawLine(SparkBlue, Offset(w * 0.08f, h * 0.52f), Offset(w * 0.18f, h * 0.53f), strokeWidth = 8f, cap = StrokeCap.Round)

                    drawLine(SparkBlue, Offset(w * 0.88f, h * 0.32f), Offset(w * 0.78f, h * 0.42f), strokeWidth = 8f, cap = StrokeCap.Round)
                    drawLine(SparkBlue, Offset(w * 0.92f, h * 0.48f), Offset(w * 0.82f, h * 0.52f), strokeWidth = 8f, cap = StrokeCap.Round)

                    // 2. الجوال الرأسي الخلفي (Vertical Phone)
                    val vPhoneW = w * 0.48f
                    val vPhoneH = h * 0.72f
                    val vPhoneLeft = (w - vPhoneW) / 2 - 15f
                    val vPhoneTop = h * 0.05f

                    drawRoundRect(
                        color = PhoneBorderBlue,
                        topLeft = Offset(vPhoneLeft, vPhoneTop),
                        size = Size(vPhoneW, vPhoneH),
                        cornerRadius = CornerRadius(36f, 36f)
                    )
                    drawRoundRect(
                        color = PhoneScreenBlue,
                        topLeft = Offset(vPhoneLeft + 8f, vPhoneTop + 8f),
                        size = Size(vPhoneW - 16f, vPhoneH - 16f),
                        cornerRadius = CornerRadius(28f, 28f)
                    )
                    drawRoundRect(
                        color = PhoneBorderBlue,
                        topLeft = Offset(vPhoneLeft + (vPhoneW / 2) - 24f, vPhoneTop + 14f),
                        size = Size(48f, 10f),
                        cornerRadius = CornerRadius(5f, 5f)
                    )

                    // 3. الجوال الأفقي الأمامي (Horizontal Phone)
                    val hPhoneW = w * 0.72f
                    val hPhoneH = h * 0.42f
                    val hPhoneLeft = (w - hPhoneW) / 2 + 15f
                    val hPhoneTop = h * 0.48f

                    drawRoundRect(
                        color = PhoneBorderBlue,
                        topLeft = Offset(hPhoneLeft, hPhoneTop),
                        size = Size(hPhoneW, hPhoneH),
                        cornerRadius = CornerRadius(32f, 32f)
                    )
                    drawRoundRect(
                        color = PhoneScreenBlue,
                        topLeft = Offset(hPhoneLeft + 8f, hPhoneTop + 8f),
                        size = Size(hPhoneW - 16f, hPhoneH - 16f),
                        cornerRadius = CornerRadius(24f, 24f)
                    )
                    drawRoundRect(
                        color = PhoneBorderBlue,
                        topLeft = Offset(hPhoneLeft + hPhoneW - 22f, hPhoneTop + (hPhoneH / 2) - 18f),
                        size = Size(8f, 36f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    // 4. الدائرة المركزية البيضاء المضبوطة لعلامة التوقف المؤقت
                    val circleRadius = w * 0.19f
                    val circleCenter = Offset(w * 0.46f, h * 0.50f)

                    drawCircle(
                        color = PauseCircleBg,
                        radius = circleRadius,
                        center = circleCenter
                    )

                    // أعمدة رمز التوقف الداكنة والمحددة بدقة
                    val barW = 16f
                    val barH = 58f
                    val barGap = 10f

                    // الخط الأيسر
                    drawRoundRect(
                        color = PauseIconBlue,
                        topLeft = Offset(circleCenter.x - barGap - barW, circleCenter.y - (barH / 2)),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // الخط الأيمن
                    drawRoundRect(
                        color = PauseIconBlue,
                        topLeft = Offset(circleCenter.x + barGap, circleCenter.y - (barH / 2)),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
        }

        // زر Get Started السفلي
        Button(
            onClick = onGetStartedClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Get Started",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}