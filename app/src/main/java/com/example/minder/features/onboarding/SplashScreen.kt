package com.example.minder.features.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val DarkBackground = Color(0xFF001B3D)
private val WaveDarkBlue = Color(0xFF0D3268)
private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8A9FB8)

// ألوان الشعار المقتبسة من الصورة
private val LogoLightBlue = Color(0xFF63B3FF)
private val LogoMidBlue = Color(0xFF2082F6)
private val LogoDarkBlue = Color(0xFF0A499C)
private val LogoDeepDarkBlue = Color(0xFF031630)

@Composable
fun SplashScreen(
    onSplashTimeout: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // رسم الأمواج الخلفية
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val wavePath = Path().apply {
                moveTo(0f, height * 0.78f)
                cubicTo(
                    width * 0.25f, height * 0.85f,
                    width * 0.65f, height * 0.65f,
                    width, height * 0.72f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(path = wavePath, color = WaveDarkBlue)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // رسم الشعار في المنتصف
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val corner = w * 0.22f

                    // 1. المربع الخارجي المنحني كقاطعة قص للشعار
                    val outerPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(0f, 0f, w, h),
                                cornerRadius = CornerRadius(corner, corner)
                            )
                        )
                    }

                    // 2. الجزء العلوي الأيسر (فاتح)
                    val topLeftPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w * 0.52f, 0f)
                        lineTo(w * 0.12f, h)
                        lineTo(0f, h)
                        close()
                    }

                    // 3. الجزء الأيمن العلوي (أزرق متوسط)
                    val topRightPath = Path().apply {
                        moveTo(w * 0.48f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.55f)
                        lineTo(w * 0.35f, h * 0.25f)
                        close()
                    }

                    // 4. الجزء السفلي الأيمن (أزرق غامق)
                    val bottomRightPath = Path().apply {
                        moveTo(w, h * 0.48f)
                        lineTo(w, h)
                        lineTo(w * 0.48f, h)
                        lineTo(w * 0.88f, 0f)
                        close()
                    }

                    // 5. الجزء السفلي الأيسر (أزرق سماوي)
                    val bottomLeftPath = Path().apply {
                        moveTo(0f, h * 0.52f)
                        lineTo(w * 0.65f, h * 0.75f)
                        lineTo(w * 0.52f, h)
                        lineTo(0f, h)
                        close()
                    }

                    // 6. المعين/العين المائلة البيضاء الداخلية
                    val innerWhitePath = Path().apply {
                        moveTo(w * 0.48f, h * 0.12f)
                        cubicTo(w * 0.65f, h * 0.28f, w * 0.88f, h * 0.48f, w * 0.88f, h * 0.48f)
                        cubicTo(w * 0.72f, h * 0.65f, w * 0.52f, h * 0.88f, w * 0.52f, h * 0.88f)
                        cubicTo(w * 0.35f, h * 0.72f, w * 0.12f, h * 0.52f, w * 0.12f, h * 0.52f)
                        close()
                    }

                    // رسم طبقات الشعار مع القص
                    drawPath(path = outerPath, color = LogoMidBlue)
                    drawPath(path = topLeftPath, color = LogoLightBlue)
                    drawPath(path = topRightPath, color = LogoMidBlue)
                    drawPath(path = bottomRightPath, color = LogoDarkBlue)
                    drawPath(path = bottomLeftPath, color = LogoLightBlue)

                    // رسم الجزء الأبيض الداخلي
                    drawPath(path = innerWhitePath, color = Color.White)

                    // 7. الدائرة الشرقية الداكنة التوقف المؤقت
                    val centerCircleRadius = w * 0.22f
                    val centerPoint = Offset(w * 0.5f, h * 0.5f)
                    drawCircle(
                        color = LogoDeepDarkBlue,
                        radius = centerCircleRadius,
                        center = centerPoint
                    )

                    // 8. خطوط الـ Pause البيضاء
                    val barWidth = w * 0.055f
                    val barHeight = h * 0.22f
                    val barGap = w * 0.04f

                    // الخط الأيسر
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(centerPoint.x - barGap - barWidth, centerPoint.y - (barHeight / 2)),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    // الخط الأيمن
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(centerPoint.x + barGap, centerPoint.y - (barHeight / 2)),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // اسم التطبيق والشعار النصي
            Text(
                text = "MINDER",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Break the Habit",
                color = TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}