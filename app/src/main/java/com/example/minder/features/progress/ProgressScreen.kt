package com.example.minder.features.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.TextStyle
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.ManropeFont
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.domain.model.*
import java.util.Locale

// Lama - UI only. All numbers come from Ragahd's existing ProgressViewModel.
private val Ink = Color(0xFF302342)
private val Muted = Color(0xFF655B76)
private val Purple = Color(0xFF9061D8)
private val Teal = Color(0xFF38C9C5)
private val DarkTeal = Color(0xFF1B837F)
private val Pink = Color(0xFFF3A1C8)
private val Blue = Color(0xFF78B8F5)
private val LavenderBg = Color(0xFFF5EEFC)
private val TealBg = Color(0xFFF1FCFB)
private val PinkBg = Color(0xFFFCECF5)

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onBackClick: () -> Unit = {} // Kept for compatibility; bottom navigation replaces Back.
) {
    val interventions by viewModel.interventionStatisticsState.collectAsState()
    val success by viewModel.challengeSuccessRateState.collectAsState()
    val attempts by viewModel.attemptsStatisticsState.collectAsState()

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalTextStyle provides TextStyle(fontFamily = JakartaFont, color = Ink, textAlign = TextAlign.Start)
    ) {
        Column(
            Modifier.fillMaxSize().background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Progress", fontSize = 29.sp, fontWeight = FontWeight.Bold, fontFamily = OutfitFont, color = Ink)
            Text("Track your achievements and build healthier digital habits.",
                fontSize = 13.sp, color = Muted)
            Spacer(Modifier.height(3.dp))

            when (val s = interventions) {
                is ProgressDataState.Loading -> StateCard("Intervention Statistics", TealBg) { Loading() }
                is ProgressDataState.Error -> StateCard("Intervention Statistics", TealBg) { ErrorText(s.message) }
                is ProgressDataState.Success -> InterventionSection(s.data)
            }
            when (val s = success) {
                is ProgressDataState.Loading -> StateCard("Challenge Success Rate", LavenderBg) { Loading() }
                is ProgressDataState.Error -> StateCard("Challenge Success Rate", LavenderBg) { ErrorText(s.message) }
                is ProgressDataState.Success -> SuccessSection(s.data)
            }
            when (val s = attempts) {
                is ProgressDataState.Loading -> StateCard("Attempts & Challenge Types", PinkBg) { Loading() }
                is ProgressDataState.Error -> StateCard("Attempts & Challenge Types", PinkBg) { ErrorText(s.message) }
                is ProgressDataState.Success -> AttemptsSection(s.data)
            }
        }
    }
}

@Composable
private fun StateCard(
    title: String,
    bg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, when(bg) {
            LavenderBg -> Color(0xFFE9DDF9)
            PinkBg -> Color(0xFFF8D6E8)
            else -> Color(0xFFD3F1EE)
        })
    ) {
        Column(
            Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = OutfitFont, color = Ink)
                content()
            }
        )
    }
}

@Composable private fun Loading() {
    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Teal)
    }
}

@Composable private fun ErrorText(message: String) {
    Text(message, color = Color(0xFFA8456F), fontSize = 13.sp)
}

@Composable
private fun Metric(value: String, label: String, modifier: Modifier = Modifier, valueColor: Color = Ink) {
    Column(
        modifier.background(Color.White, RoundedCornerShape(14.dp)).padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, fontSize = 21.sp, fontWeight = FontWeight.Bold, fontFamily = ManropeFont, color = valueColor)
        Text(label, fontSize = 11.sp, color = Muted, lineHeight = 15.sp)
    }
}

@Composable
private fun InterventionSection(data: InterventionStatistics) {
    StateCard("Intervention Statistics", TealBg) {
        if (data.totalInterventions > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Donut(
                    values = listOf(
                        data.unlockedInterventions.toFloat(),
                        data.pendingInterventions.toFloat(),
                        data.lockedInterventions.toFloat()
                    ),
                    colors = listOf(Teal, Blue, Pink),
                    centerText = data.totalInterventions.toString(),
                    modifier = Modifier.size(132.dp)
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Legend("Unlocked", Teal, data.unlockedInterventions.toString())
                    Legend("Pending", Blue, data.pendingInterventions.toString())
                    Legend("Locked", Pink, data.lockedInterventions.toString())
                }
            }
        } else {
            Text("No intervention records yet.", fontSize = 13.sp, color = Muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Metric(data.totalInterventions.toString(), "Total", Modifier.weight(1f))
            Metric(data.unlockedInterventions.toString(), "Unlocked", Modifier.weight(1f), DarkTeal)
            Metric(data.lockedInterventions.toString(), "Locked", Modifier.weight(1f), Color(0xFFA8456F))
        }
        Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(13.dp)) {
            Text("Average resolution time", color = Muted, fontSize = 12.sp)
            Text(formatDuration(data.averageResolutionTimeSeconds), color = Ink, fontFamily = ManropeFont, fontWeight = FontWeight.Bold, fontSize = 23.sp)
        }
    }
}

@Composable
private fun SuccessSection(data: ChallengeSuccessRate) {
    StateCard("Challenge Success Rate", LavenderBg) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Donut(
                values = listOf(
                    data.successRatePercentage.toFloat().coerceIn(0f, 100f),
                    (100.0 - data.successRatePercentage).toFloat().coerceIn(0f, 100f)
                ),
                colors = listOf(Teal, Color(0xFFDDF5F2)),
                centerText = "${data.successRatePercentage.num()}%",
                modifier = Modifier.size(156.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Metric(data.totalCompletedInterventions.toString(), "Completed", Modifier.weight(1f))
            Metric(data.successfulInterventions.toString(), "Successful", Modifier.weight(1f), DarkTeal)
            Metric(data.failedInterventions.toString(), "Failed", Modifier.weight(1f), Color(0xFFA8456F))
        }
        Text(
            "Success rate is calculated from completed interventions.",
            fontSize = 12.sp, color = Muted
        )
    }
}

@Composable
private fun AttemptsSection(data: AttemptsAndChallengeTypesStatistics) {
    StateCard("Attempts & Challenge Types", PinkBg) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Metric(data.correctAttempts.toString(), "Correct", Modifier.weight(1f), DarkTeal)
            Metric(data.incorrectAttempts.toString(), "Incorrect", Modifier.weight(1f), Color(0xFFA8456F))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Metric(data.totalAttempts.toString(), "Total attempts", Modifier.weight(1f))
            Metric(data.averageAttemptsPerIntervention.num(), "Avg per intervention", Modifier.weight(1f), Purple)
        }
        Text("By Challenge Type", fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Ink)
        ChallengeType.entries.forEach { type ->
            val stats = data.attemptsByChallengeType[type]
            val correct = stats?.correctAttempts ?: 0
            val wrong = stats?.incorrectAttempts ?: 0
            val total = stats?.totalAttempts ?: 0
            val label = when(type) {
                ChallengeType.MATH -> "Math"
                ChallengeType.PUZZLE -> "Puzzle"
                ChallengeType.LOGIC -> "Logic"
            }
            Column(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Purple)
                    Text("${(stats?.successRatePercentage ?: 0.0).num()}%",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkTeal)
                }
                SegmentedBar(correct.toFloat(), wrong.toFloat())
                Text("$total attempts  •  $correct correct  •  $wrong incorrect",
                    fontSize = 11.sp, color = Muted)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Legend("Correct", Teal)
            Legend("Incorrect", Pink)
        }
    }
}

@Composable
private fun Legend(label: String, color: Color, value: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(3.dp)))
        Text(if (value == null) label else "$label: $value", fontSize = 12.sp, color = Ink)
    }
}

@Composable
private fun SegmentedBar(correct: Float, wrong: Float) {
    val total = correct + wrong
    Row(
        Modifier.fillMaxWidth().height(13.dp).background(Color(0xFFE5F8F6), RoundedCornerShape(8.dp))
    ) {
        if (total > 0) {
            if (correct > 0) Box(Modifier.fillMaxHeight().weight(correct).background(Teal))
            if (wrong > 0) Box(Modifier.fillMaxHeight().weight(wrong).background(Pink))
        }
    }
}

@Composable
private fun Donut(values: List<Float>, colors: List<Color>, centerText: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 13.dp.toPx()
            val total = values.sum().coerceAtLeast(0f)
            var start = -90f
            if (total <= 0f) {
                drawArc(Color(0xFFDDF5F2), 0f, 360f, false, style = Stroke(stroke))
            } else {
                values.forEachIndexed { index, amount ->
                    val sweep = amount.coerceAtLeast(0f) / total * 360f
                    if (sweep > 0f) drawArc(
                        color = colors[index % colors.size],
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt)
                    )
                    start += sweep
                }
            }
        }
        Text(centerText, color = Ink, fontSize = 23.sp, fontFamily = ManropeFont, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

private fun Double.num(): String = String.format(Locale.US, "%.1f", this)
private fun formatDuration(seconds: Double): String {
    val s = seconds.toLong().coerceAtLeast(0)
    return when {
        s >= 3600 -> "${s / 3600}h ${(s % 3600) / 60}m"
        s >= 60 -> "${s / 60}m ${s % 60}s"
        else -> "${s}s"
    }
}
