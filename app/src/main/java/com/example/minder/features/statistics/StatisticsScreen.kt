package com.example.minder.features.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.domain.model.*
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.ManropeFont
import com.example.minder.ui.theme.OutfitFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Lama - Statistics styling aligned with Minder's HTML/CSS dashboard.
// This file changes UI only. All six real statistics still come from StatisticsViewModel.
private val StatsBackground = Color.White
private val StatsText = Color(0xFF302342)
private val StatsSecondary = Color(0xFF655B76)
private val StatsTeal = Color(0xFF38C9C5)
private val StatsDarkTeal = Color(0xFF1B837F)
private val StatsPurple = Color(0xFF9061D8)
private val StatsPink = Color(0xFFF3A1C8)
private val StatsBlue = Color(0xFF78B8F5)
private val StatsBorder = Color(0xFFD3F1EE)
private val StatsLightTeal = Color(0xFFE5F8F6)
private val StatsPaleTeal = Color(0xFFF1FCFB)
private val StatsLavender = Color(0xFFF5EEFC)
private val StatsLavenderBorder = Color(0xFFE9DDF9)
private val StatsSoftPink = Color(0xFFFCECF5)
private val StatsPinkBorder = Color(0xFFF8D6E8)
private val StatsSoftBlue = Color(0xFFEDF6FE)
private val StatsBlueBorder = Color(0xFFD5E8FB)
private val StatsError = Color(0xFFA8456F)

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel
) {
    val dailyState by viewModel.dailyUsageState.collectAsState()
    val weeklyState by viewModel.weeklyUsageState.collectAsState()
    val comparisonState by viewModel.usageComparisonState.collectAsState()
    val savedTimeState by viewModel.savedTimeState.collectAsState()
    val averageState by viewModel.averageSessionDurationState.collectAsState()
    val peakState by viewModel.peakUsageTimesState.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StatsBackground)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Text(
                    "Statistics",
                    fontFamily = OutfitFont,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatsText
                )
                Text(
                    "Understand your screen time and discover your digital habits.",
                    fontFamily = JakartaFont,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = StatsSecondary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(5.dp))

                StatsDataCard(
                    title = "Daily Usage",
                    state = dailyState,
                    background = StatsLavender,
                    borderColor = StatsLavenderBorder
                ) { data ->
                    StatValue(formatDuration(data.totalUsageSeconds.toLong()), "Total today")
                    Spacer(Modifier.height(14.dp))

                    if (data.apps.isEmpty()) {
                        EmptyStatsMessage("No app usage recorded today")
                    } else {
                        Text(
                            "Usage by app",
                            fontFamily = OutfitFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatsText
                        )
                        Spacer(Modifier.height(7.dp))
                        val maximum = data.apps.maxOfOrNull {
                            it.totalUsageSeconds
                        }?.toLong() ?: 1L
                        data.apps.sortedByDescending { it.totalUsageSeconds }
                            .forEach { app ->
                                UsageProgressRow(
                                    name = app.appName,
                                    seconds = app.totalUsageSeconds.toLong(),
                                    maximum = maximum,
                                    progressColor = StatsPurple
                                )
                            }
                    }
                }

                StatsDataCard(
                    title = "Weekly Usage",
                    state = weeklyState,
                    background = StatsPaleTeal
                ) { data ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        StatValue(
                            value = formatDuration(data.totalUsageSeconds.toLong()),
                            label = "Weekly total",
                            modifier = Modifier.weight(1f)
                        )
                        StatValue(
                            value = formatDuration(data.averageDailyUsageSeconds.toLong()),
                            label = "Daily average",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${formatDate(data.weekStartDate)} – ${formatDate(data.weekEndDate)}",
                        fontFamily = JakartaFont,
                        fontSize = 12.sp,
                        color = StatsSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    if (data.apps.isEmpty()) {
                        EmptyStatsMessage("No weekly app usage available")
                    } else {
                        Text(
                            "App usage this week",
                            fontFamily = OutfitFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatsText
                        )
                        Spacer(Modifier.height(7.dp))
                        val maximum = data.apps.maxOfOrNull {
                            it.totalUsageSeconds
                        }?.toLong() ?: 1L
                        data.apps.sortedByDescending { it.totalUsageSeconds }
                            .forEach { app ->
                                UsageProgressRow(
                                    name = app.appName,
                                    seconds = app.totalUsageSeconds.toLong(),
                                    maximum = maximum,
                                    progressColor = StatsTeal
                                )
                            }
                    }
                }

                StatsDataCard(
                    title = "Usage Comparison",
                    state = comparisonState,
                    background = StatsSoftBlue,
                    borderColor = StatsBlueBorder
                ) { data ->
                    val trendColor = when (data.trend) {
                        UsageTrend.DECREASED -> StatsDarkTeal
                        UsageTrend.INCREASED -> StatsError
                        UsageTrend.UNCHANGED -> StatsSecondary
                    }
                    val trendLabel = when (data.trend) {
                        UsageTrend.DECREASED -> "Usage decreased"
                        UsageTrend.INCREASED -> "Usage increased"
                        UsageTrend.UNCHANGED -> "No change"
                    }

                    Text(
                        trendLabel,
                        fontFamily = OutfitFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = trendColor
                    )
                    Spacer(Modifier.height(10.dp))

                    val current = data.currentWeekUsageSeconds.toLong()
                    val previous = data.previousWeekUsageSeconds.toLong()
                    val maximum = maxOf(current, previous, 1L)

                    ComparisonBar("This week", current, maximum, StatsTeal)
                    Spacer(Modifier.height(10.dp))
                    ComparisonBar("Previous week", previous, maximum, StatsBlue)
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = StatsBlueBorder)
                    Spacer(Modifier.height(10.dp))

                    val delta = data.usageChangeSeconds.toLong()
                    val sign = if (delta > 0) "+" else if (delta < 0) "−" else ""
                    Text(
                        "$sign${formatDuration(kotlin.math.abs(delta))} " +
                                "(${String.format(Locale.US, "%+.1f", data.usageChangePercentage)}%)",
                        fontFamily = ManropeFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = trendColor
                    )
                    Text(
                        "Compared with the previous week",
                        fontFamily = JakartaFont,
                        fontSize = 12.sp,
                        color = StatsSecondary
                    )
                }

                StatsDataCard(
                    title = "Saved Time",
                    state = savedTimeState,
                    background = StatsSoftPink,
                    borderColor = StatsPinkBorder
                ) { data ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        StatValue(
                            value = formatDuration(data.todaySavedTimeSeconds.toLong()),
                            label = "Saved today",
                            modifier = Modifier.weight(1f)
                        )
                        StatValue(
                            value = formatDuration(data.totalSavedTimeSeconds.toLong()),
                            label = "Total saved",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                StatsDataCard(
                    title = "Average Session Duration",
                    state = averageState,
                    background = StatsSoftBlue,
                    borderColor = StatsBlueBorder
                ) { data ->
                    StatValue(
                        value = formatDuration(data.averageDurationSeconds.toLong()),
                        label = "Average session"
                    )
                    Spacer(Modifier.height(11.dp))
                    Text(
                        "${data.totalSessions} sessions recorded",
                        fontFamily = JakartaFont,
                        fontSize = 13.sp,
                        color = StatsSecondary
                    )
                }

                StatsDataCard(
                    title = "Peak Usage Times",
                    state = peakState,
                    background = StatsPaleTeal
                ) { data ->
                    if (data.peakHour == null) {
                        EmptyStatsMessage("No usage sessions recorded")
                    } else {
                        StatValue(
                            value = formatHour(data.peakHour),
                            label = "Peak hour"
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatDuration(data.peakUsageSeconds) + " used during peak hour",
                            fontFamily = JakartaFont,
                            fontSize = 13.sp,
                            color = StatsSecondary
                        )
                        Spacer(Modifier.height(18.dp))

                        HourlyUsageChart(data.hourlyStats)
                        Spacer(Modifier.height(9.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM")
                                .forEach { label ->
                                    Text(
                                        label,
                                        fontFamily = JakartaFont,
                                        color = StatsSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            ChartLegend("Peak", StatsTeal)
                            ChartLegend("Other hours", StatsPurple)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun <T> StatsDataCard(
    title: String,
    state: StatisticsDataState<T>,
    background: Color,
    borderColor: Color = StatsBorder,
    content: @Composable ColumnScope.(T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .padding(18.dp)
    ) {
        Text(
            title,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = StatsText
        )
        Spacer(Modifier.height(15.dp))

        when (state) {
            is StatisticsDataState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = StatsTeal,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
            }

            is StatisticsDataState.Success -> content(state.data)

            is StatisticsDataState.Error -> Text(
                state.message,
                fontFamily = JakartaFont,
                fontSize = 13.sp,
                color = StatsError
            )
        }
    }
}

@Composable
private fun StatValue(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            value,
            fontFamily = ManropeFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 25.sp,
            color = StatsText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontFamily = JakartaFont,
            fontSize = 12.sp,
            color = StatsSecondary
        )
    }
}

@Composable
private fun UsageProgressRow(
    name: String,
    seconds: Long,
    maximum: Long,
    progressColor: Color
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                fontFamily = JakartaFont,
                color = StatsText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                formatDuration(seconds),
                fontFamily = ManropeFont,
                fontWeight = FontWeight.SemiBold,
                color = StatsSecondary,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = {
                if (maximum > 0) (seconds.toFloat() / maximum.toFloat()).coerceIn(0f, 1f)
                else 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(9.dp)),
            color = progressColor,
            trackColor = StatsLightTeal,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun ComparisonBar(
    name: String,
    seconds: Long,
    maximum: Long,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            name,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = StatsText
        )
        Text(
            formatDuration(seconds),
            fontFamily = ManropeFont,
            fontSize = 13.sp,
            color = StatsText
        )
    }
    Spacer(Modifier.height(6.dp))
    LinearProgressIndicator(
        progress = { (seconds.toFloat() / maximum.toFloat()).coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(7.dp)),
        color = color,
        trackColor = Color.White,
        strokeCap = StrokeCap.Round
    )
}

@Composable
private fun EmptyStatsMessage(message: String) {
    Text(
        message,
        fontFamily = JakartaFont,
        fontSize = 13.sp,
        color = StatsSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.65f))
            .padding(15.dp)
    )
}

@Composable
private fun ChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            fontFamily = JakartaFont,
            fontSize = 11.sp,
            color = StatsSecondary
        )
    }
}

@Composable
private fun HourlyUsageChart(hourlyStats: List<HourlyUsageStats>) {
    val values = remember(hourlyStats) {
        (0..23).map { hour ->
            hourlyStats.firstOrNull { it.hourOfDay == hour }
                ?.totalUsageSeconds ?: 0L
        }
    }
    val maximum = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val count = 24
        val spacing = 3.dp.toPx()
        val width = ((size.width - spacing * (count - 1)) / count)
            .coerceAtLeast(0f)

        values.forEachIndexed { index, value ->
            if (value > 0L) {
                val barHeight = (value.toFloat() / maximum.toFloat() * size.height)
                    .coerceAtLeast(3.dp.toPx())
                drawRoundRect(
                    color = if (value == maximum) StatsTeal else StatsPurple,
                    topLeft = Offset(index * (width + spacing), size.height - barHeight),
                    size = Size(width, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        safe > 0 -> "${safe}s"
        else -> "0m"
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM d", Locale.ENGLISH).format(Date(timestamp))

private fun formatHour(hour: Int): String {
    val normalized = hour.coerceIn(0, 23)
    val hour12 = if (normalized % 12 == 0) 12 else normalized % 12
    return "$hour12 ${if (normalized < 12) "AM" else "PM"}"
}
