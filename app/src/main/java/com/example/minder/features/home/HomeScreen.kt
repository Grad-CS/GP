package com.example.minder.features.home

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.features.app_selection.SelectableApp
import com.example.minder.features.app_selection.getAppIconBitmap

private val PrimaryDark = Color(0xFF001B3D)
private val CardBackground = Color(0xFF082247)
private val BoxInnerBg = Color(0xFF031630)
private val AccentGreen = Color(0xFF2EC4B6)
private val CircleTrack = Color(0xFF13325B)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A9FB8)
private val BorderColor = Color(0xFF143866)
private val OrangeStreak = Color(0xFFFF8C42)

// ألوان شعار Minder الرسمية
private val LogoLightBlue = Color(0xFF63B3FF)
private val LogoMidBlue = Color(0xFF2082F6)
private val LogoDarkBlue = Color(0xFF0A499C)
private val LogoDeepDarkBlue = Color(0xFF031630)

@Composable
fun HomeScreen(
    selectedApps: List<SelectableApp> = emptyList(),
    appUsageMap: Map<String, Int> = emptyMap(),
    todayUsageMinutes: Int = 0,     // القراءة الحقيقية الفعلية
    timeSavedMinutes: Int = 0,      // الوقت الموفر الفعلي
    streakDays: Int = 0,            // الأيام المتتالية الفعلية
    onSettingsClick: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val sortedAppsByUsage = remember(selectedApps, appUsageMap) {
        selectedApps.sortedByDescending { app ->
            appUsageMap[app.packageName] ?: 0
        }
    }

    val totalLimitMinutes = remember(selectedApps) {
        selectedApps.mapNotNull { it.dailyLimitMinutes }.sum()
    }

    val usageProgress = remember(todayUsageMinutes, totalLimitMinutes) {
        if (totalLimitMinutes > 0) {
            (todayUsageMinutes.toFloat() / totalLimitMinutes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = usageProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header: Minder Drawn Logo & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // رسم شعار Minder المحتفي بالبداية (32x32 dp)
                    Box(modifier = Modifier.size(32.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val corner = w * 0.22f

                            val outerPath = Path().apply {
                                addRoundRect(
                                    RoundRect(
                                        rect = Rect(0f, 0f, w, h),
                                        cornerRadius = CornerRadius(corner, corner)
                                    )
                                )
                            }

                            val topLeftPath = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(w * 0.52f, 0f)
                                lineTo(w * 0.12f, h)
                                lineTo(0f, h)
                                close()
                            }

                            val topRightPath = Path().apply {
                                moveTo(w * 0.48f, 0f)
                                lineTo(w, 0f)
                                lineTo(w, h * 0.55f)
                                lineTo(w * 0.35f, h * 0.25f)
                                close()
                            }

                            val bottomRightPath = Path().apply {
                                moveTo(w, h * 0.48f)
                                lineTo(w, h)
                                lineTo(w * 0.48f, h)
                                lineTo(w * 0.88f, 0f)
                                close()
                            }

                            val bottomLeftPath = Path().apply {
                                moveTo(0f, h * 0.52f)
                                lineTo(w * 0.65f, h * 0.75f)
                                lineTo(w * 0.52f, h)
                                lineTo(0f, h)
                                close()
                            }

                            val innerWhitePath = Path().apply {
                                moveTo(w * 0.48f, h * 0.12f)
                                cubicTo(w * 0.65f, h * 0.28f, w * 0.88f, h * 0.48f, w * 0.88f, h * 0.48f)
                                cubicTo(w * 0.72f, h * 0.65f, w * 0.52f, h * 0.88f, w * 0.52f, h * 0.88f)
                                cubicTo(w * 0.35f, h * 0.72f, w * 0.12f, h * 0.52f, w * 0.12f, h * 0.52f)
                                close()
                            }

                            drawPath(path = outerPath, color = LogoMidBlue)
                            drawPath(path = topLeftPath, color = LogoLightBlue)
                            drawPath(path = topRightPath, color = LogoMidBlue)
                            drawPath(path = bottomRightPath, color = LogoDarkBlue)
                            drawPath(path = bottomLeftPath, color = LogoLightBlue)

                            drawPath(path = innerWhitePath, color = Color.White)

                            val centerCircleRadius = w * 0.22f
                            val centerPoint = Offset(w * 0.5f, h * 0.5f)
                            drawCircle(
                                color = LogoDeepDarkBlue,
                                radius = centerCircleRadius,
                                center = centerPoint
                            )

                            val barWidth = w * 0.055f
                            val barHeight = h * 0.22f
                            val barGap = w * 0.04f

                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(centerPoint.x - barGap - barWidth, centerPoint.y - (barHeight / 2)),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(2f, 2f)
                            )

                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(centerPoint.x + barGap, centerPoint.y - (barHeight / 2)),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Minder",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Greeting
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Good morning,",
                    color = TextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Keep going!",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Progress Ring with Real Data
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 16.dp.toPx()

                    drawArc(
                        color = CircleTrack,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    if (totalLimitMinutes > 0) {
                        drawArc(
                            color = AccentGreen,
                            startAngle = 135f,
                            sweepAngle = 270f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Today",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMinutesToHoursMinutes(todayUsageMinutes),
                        color = TextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (totalLimitMinutes > 0) "of ${formatMinutesToHoursMinutes(totalLimitMinutes)} limit" else "No limits set",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Real Time Saved & Streak
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Time saved today",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${timeSavedMinutes}m",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = BorderColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(OrangeStreak.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = OrangeStreak,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Streak",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$streakDays days",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Real Selected Apps Row
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Your selected apps",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (sortedAppsByUsage.isEmpty()) {
                    Text(
                        text = "No apps selected yet",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                } else {
                    val displayApps = sortedAppsByUsage.take(4)
                    val remainingAppsCount = (sortedAppsByUsage.size - 4).coerceAtLeast(0)

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(displayApps) { _, app ->
                            val appIcon = remember(app.packageName) {
                                getAppIconBitmap(context, app.packageName)
                            }

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardBackground)
                                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (appIcon != null) {
                                    Image(
                                        bitmap = appIcon,
                                        contentDescription = app.name,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(BoxInnerBg)
                                    )
                                }
                            }
                        }

                        if (remainingAppsCount > 0) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(CardBackground)
                                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+$remainingAppsCount",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    onTabSelected(index)
                }
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        NavigationTab("Home", Icons.Default.Home),
        NavigationTab("Statistics", Icons.Default.Info),
        NavigationTab("Progress", Icons.Default.DateRange),
        NavigationTab("Settings", Icons.Default.Settings)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onTabSelected(index) }
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) Color(0xFF2F8FFF) else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color(0xFF2F8FFF) else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private data class NavigationTab(val label: String, val icon: ImageVector)

private fun formatMinutesToHoursMinutes(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0m"
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}