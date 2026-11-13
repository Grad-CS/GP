
package com.example.minder.features.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.domain.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val StatsBackground = Color(0xFF001B3D)
private val StatsCard = Color(0xFF082247)
private val StatsInner = Color(0xFF031630)
private val StatsAccent = Color(0xFF2EC4B6)
private val StatsText = Color.White
private val StatsSecondary = Color(0xFF8A9FB8)
private val StatsBorder = Color(0xFF143866)
private val StatsBlue = Color(0xFF2F8FFF)
private val StatsOrange = Color(0xFFFF8C42)

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onBackClick: () -> Unit = {}
) {
    val dailyState by viewModel.dailyUsageState.collectAsState()
    val weeklyState by viewModel.weeklyUsageState.collectAsState()
    val comparisonState by viewModel.usageComparisonState.collectAsState()
    val savedTimeState by viewModel.savedTimeState.collectAsState()
    val averageState by viewModel.averageSessionDurationState.collectAsState()
    val peakState by viewModel.peakUsageTimesState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = StatsText
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Statistics",
                    color = StatsText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Understand your digital habits",
                    color = StatsSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            StatsSectionTitle("Daily Usage")

            StatsDataCard(
                title = "Total screen time today",
                state = dailyState
            ) { data ->
                Text(
                    text = formatDuration(data.totalUsageSeconds.toLong()),
                    color = StatsAccent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (data.apps.isEmpty()) {
                    EmptyStatsMessage("No app usage recorded today")
                } else {
                    data.apps
                        .sortedByDescending { it.totalUsageSeconds }
                        .forEach { app ->
                            UsageProgressRow(
                                name = app.appName,
                                seconds = app.totalUsageSeconds.toLong(),
                                maximum = data.apps.maxOfOrNull {
                                    it.totalUsageSeconds
                                }?.toLong() ?: 1L
                            )
                        }
                }
            }

            StatsSectionTitle("Weekly Usage")

            StatsDataCard(
                title = "This week's screen time",
                state = weeklyState
            ) { data ->
                Text(
                    text = formatDuration(data.totalUsageSeconds.toLong()),
                    color = StatsAccent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Daily average", color = StatsSecondary, fontSize = 12.sp)
                        Text(
                            formatDuration(data.averageDailyUsageSeconds.toLong()),
                            color = StatsText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Week period", color = StatsSecondary, fontSize = 12.sp)
                        Text(
                            "${formatDate(data.weekStartDate)} - ${formatDate(data.weekEndDate)}",
                            color = StatsText,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "App usage",
                    color = StatsText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (data.apps.isEmpty()) {
                    EmptyStatsMessage("No weekly app usage available")
                } else {
                    data.apps
                        .sortedByDescending { it.totalUsageSeconds }
                        .forEach { app ->
                            UsageProgressRow(
                                name = app.appName,
                                seconds = app.totalUsageSeconds.toLong(),
                                maximum = data.apps.maxOfOrNull {
                                    it.totalUsageSeconds
                                }?.toLong() ?: 1L
                            )
                        }
                }
            }

            StatsSectionTitle("Usage Comparison")

            StatsDataCard(
                title = "Current week vs previous week",
                state = comparisonState
            ) { data ->
                val trendColor = when (data.trend) {
                    UsageTrend.DECREASED -> StatsAccent
                    UsageTrend.INCREASED -> StatsOrange
                    UsageTrend.UNCHANGED -> StatsSecondary
                }

                val trendLabel = when (data.trend) {
                    UsageTrend.DECREASED -> "Usage decreased"
                    UsageTrend.INCREASED -> "Usage increased"
                    UsageTrend.UNCHANGED -> "No change"
                }

                Text(
                    text = trendLabel,
                    color = trendColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ComparisonValue(
                    label = "This week",
                    value = formatDuration(data.currentWeekUsageSeconds.toLong())
                )

                ComparisonValue(
                    label = "Previous week",
                    value = formatDuration(data.previousWeekUsageSeconds.toLong())
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = StatsBorder
                )

                Text(
                    text = "${if (data.usageChangeSeconds > 0) "+" else ""}" +
                            formatDuration(data.usageChangeSeconds.toLong()) +
                            " (${String.format(Locale.US, "%.1f", data.usageChangePercentage)}%)",
                    color = trendColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            StatsSectionTitle("Saved Time")

            StatsDataCard(
                title = "Your time saved",
                state = savedTimeState
            ) { data ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SavedTimeValue(
                        label = "Today",
                        seconds = data.todaySavedTimeSeconds.toLong()
                    )
                    SavedTimeValue(
                        label = "Total",
                        seconds = data.totalSavedTimeSeconds.toLong()
                    )
                }
            }

            StatsSectionTitle("Average Session Duration")

            StatsDataCard(
                title = "Your average app session",
                state = averageState
            ) { data ->
                Text(
                    text = formatDuration(data.averageDurationSeconds.toLong()),
                    color = StatsAccent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${data.totalSessions} sessions recorded",
                    color = StatsSecondary,
                    fontSize = 13.sp
                )
            }

            StatsSectionTitle("Peak Usage Times")

            StatsDataCard(
                title = "Hourly usage over the last 7 days",
                state = peakState
            ) { data ->
                if (data.peakHour == null) {
                    EmptyStatsMessage("No usage sessions recorded")
                } else {
                    Text(
                        text = "Peak hour: ${formatHour(data.peakHour)}",
                        color = StatsAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = formatDuration(data.peakUsageSeconds),
                        color = StatsSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    HourlyUsageChart(data.hourlyStats)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM").forEach {
                            Text(
                                text = it,
                                color = StatsSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSectionTitle(title: String) {
    Text(
        text = title,
        color = StatsText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun <T> StatsDataCard(
    title: String,
    state: StatisticsDataState<T>,
    content: @Composable (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StatsCard, RoundedCornerShape(20.dp))
            .border(1.dp, StatsBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Text(
            text = title,
            color = StatsSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        when (state) {
            is StatisticsDataState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = StatsAccent,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
            }

            is StatisticsDataState.Success -> content(state.data)

            is StatisticsDataState.Error -> {
                Text(
                    text = state.message,
                    color = StatsOrange,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun UsageProgressRow(
    name: String,
    seconds: Long,
    maximum: Long
) {
    Column(modifier = Modifier.padding(vertical = 7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                color = StatsText,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatDuration(seconds),
                color = StatsSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        LinearProgressIndicator(
            progress = {
                if (maximum > 0) {
                    (seconds.toFloat() / maximum.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = StatsBlue,
            trackColor = StatsInner,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun ComparisonValue(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = StatsSecondary, fontSize = 13.sp)
        Text(
            value,
            color = StatsText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SavedTimeValue(
    label: String,
    seconds: Long
) {
    Column {
        Text(label, color = StatsSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            formatDuration(seconds),
            color = StatsAccent,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyStatsMessage(message: String) {
    Text(
        text = message,
        color = StatsSecondary,
        fontSize = 13.sp
    )
}

@Composable
private fun HourlyUsageChart(
    hourlyStats: List<HourlyUsageStats>
) {
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
        val barCount = 24
        val spacing = 5.dp.toPx()
        val barWidth = (size.width - spacing * (barCount - 1)) / barCount

        values.forEachIndexed { index, value ->
            val barHeight = (value.toFloat() / maximum.toFloat()) *
                    size.height

            val left = index * (barWidth + spacing)

            drawRoundRect(
                color = if (value == maximum) StatsAccent else StatsBlue,
                topLeft = androidx.compose.ui.geometry.Offset(
                    left,
                    size.height - barHeight
                ),
                size = androidx.compose.ui.geometry.Size(
                    barWidth,
                    barHeight.coerceAtLeast(3.dp.toPx())
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    4.dp.toPx(),
                    4.dp.toPx()
                )
            )
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM d", Locale.ENGLISH).format(Date(timestamp))
}

private fun formatHour(hour: Int): String {
    val normalizedHour = hour.coerceIn(0, 23)
    val period = if (normalizedHour < 12) "AM" else "PM"
    val displayHour = when (val value = normalizedHour % 12) {
        0 -> 12
        else -> value
    }
    return "$displayHour $period"
}
