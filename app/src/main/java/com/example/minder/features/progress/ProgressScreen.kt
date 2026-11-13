
package com.example.minder.features.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.domain.model.AttemptsAndChallengeTypesStatistics
import com.example.minder.domain.model.ChallengeSuccessRate
import com.example.minder.domain.model.ChallengeType
import com.example.minder.domain.model.ChallengeTypeStatistics
import com.example.minder.domain.model.InterventionStatistics

//Ragahd-: Minder colors shared with the Statistics screen.
private val ProgressPrimaryDark = Color(0xFF001B3D)
private val ProgressCardBackground = Color(0xFF082247)
private val ProgressBoxInnerBg = Color(0xFF031630)
private val ProgressAccentGreen = Color(0xFF2EC4B6)
private val ProgressTextSecondary = Color(0xFF8A9FB8)
private val ProgressBorderColor = Color(0xFF143866)

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onBackClick: () -> Unit = {}
) {
    val interventionState by viewModel.interventionStatisticsState.collectAsState()
    val successRateState by viewModel.challengeSuccessRateState.collectAsState()
    val attemptsState by viewModel.attemptsStatisticsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProgressPrimaryDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Progress",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = interventionState) {
                is ProgressDataState.Loading -> ProgressLoadingCard("Intervention Statistics")
                is ProgressDataState.Error -> ProgressErrorCard(
                    title = "Intervention Statistics",
                    message = state.message
                )
                is ProgressDataState.Success -> InterventionStatisticsCard(state.data)
            }

            when (val state = successRateState) {
                is ProgressDataState.Loading -> ProgressLoadingCard("Challenge Success Rate")
                is ProgressDataState.Error -> ProgressErrorCard(
                    title = "Challenge Success Rate",
                    message = state.message
                )
                is ProgressDataState.Success -> ChallengeSuccessRateCard(state.data)
            }

            when (val state = attemptsState) {
                is ProgressDataState.Loading -> ProgressLoadingCard("Attempts & Challenge Types")
                is ProgressDataState.Error -> ProgressErrorCard(
                    title = "Attempts & Challenge Types",
                    message = state.message
                )
                is ProgressDataState.Success -> AttemptsStatisticsCard(state.data)
            }
        }
    }
}

@Composable
private fun ProgressLoadingCard(title: String) {
    ProgressCard(title = title) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ProgressAccentGreen)
        }
    }
}

@Composable
private fun ProgressErrorCard(title: String, message: String) {
    ProgressCard(title = title) {
        Text(
            text = message,
            color = Color(0xFFFF8A80),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun InterventionStatisticsCard(data: InterventionStatistics) {
    ProgressCard(title = "Intervention Statistics") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = data.totalInterventions.toString()
            )
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Unlocked",
                value = data.unlockedInterventions.toString(),
                valueColor = ProgressAccentGreen
            )
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Locked",
                value = data.lockedInterventions.toString(),
                valueColor = Color(0xFFFF8A80)
            )
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Pending",
                value = data.pendingInterventions.toString(),
                valueColor = ProgressTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Average Resolution Time",
            color = ProgressTextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = formatProgressDuration(data.averageResolutionTimeSeconds),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChallengeSuccessRateCard(data: ChallengeSuccessRate) {
    ProgressCard(title = "Challenge Success Rate") {
        Text(
            text = "${data.successRatePercentage.coerceIn(0.0, 100.0).formatProgressNumber()}%",
            color = ProgressAccentGreen,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        ProgressBar(
            percentage = data.successRatePercentage
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Successful",
                value = data.successfulInterventions.toString(),
                valueColor = ProgressAccentGreen
            )
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Failed",
                value = data.failedInterventions.toString(),
                valueColor = Color(0xFFFF8A80)
            )
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Completed",
                value = data.totalCompletedInterventions.toString()
            )
        }
    }
}

@Composable
private fun AttemptsStatisticsCard(data: AttemptsAndChallengeTypesStatistics) {
    ProgressCard(title = "Attempts & Challenge Types") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = data.totalAttempts.toString()
            )
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Correct",
                value = data.correctAttempts.toString(),
                valueColor = ProgressAccentGreen
            )
            ProgressMetric(
                modifier = Modifier.weight(1f),
                label = "Incorrect",
                value = data.incorrectAttempts.toString(),
                valueColor = Color(0xFFFF8A80)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Average Attempts per Intervention",
            color = ProgressTextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = data.averageAttemptsPerIntervention.formatProgressNumber(),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "By Challenge Type",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        ChallengeType.entries.forEach { type ->
            val stats = data.attemptsByChallengeType[type]
                ?: ChallengeTypeStatistics(
                    totalAttempts = 0,
                    correctAttempts = 0,
                    incorrectAttempts = 0,
                    successRatePercentage = 0.0
                )

            ChallengeTypeItem(
                type = type,
                stats = stats
            )

            if (type != ChallengeType.entries.last()) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ChallengeTypeItem(
    type: ChallengeType,
    stats: ChallengeTypeStatistics
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                ProgressBoxInnerBg,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (type) {
                    ChallengeType.MATH -> "Math"
                    ChallengeType.PUZZLE -> "Puzzle"
                    ChallengeType.LOGIC -> "Logic"
                },
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            Text(
                text = "${stats.successRatePercentage.coerceIn(0.0, 100.0).formatProgressNumber()}%",
                color = ProgressAccentGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProgressBar(percentage = stats.successRatePercentage)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Attempts: ${stats.totalAttempts}",
                color = ProgressTextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "Correct: ${stats.correctAttempts}",
                color = ProgressAccentGreen,
                fontSize = 12.sp
            )
            Text(
                text = "Incorrect: ${stats.incorrectAttempts}",
                color = Color(0xFFFF8A80),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ProgressBar(percentage: Double) {
    val progress = (percentage / 100.0).toFloat().coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .background(
                ProgressBorderColor,
                RoundedCornerShape(50)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(9.dp)
                .background(
                    ProgressAccentGreen,
                    RoundedCornerShape(50)
                )
        )
    }
}

//Ragahd-: Reusable card container for Progress statistics.
@Composable
private fun ProgressCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProgressCardBackground
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ProgressBorderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}

@Composable
private fun ProgressMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Column(
        modifier = modifier
            .background(
                ProgressBoxInnerBg,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = ProgressTextSecondary,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

private fun formatProgressDuration(seconds: Double): String {
    val totalSeconds = seconds.toLong().coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val remainingSeconds = totalSeconds % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${remainingSeconds}s"
        else -> "${remainingSeconds}s"
    }
}

private fun Double.formatProgressNumber(): String {
    return String.format(java.util.Locale.US, "%.1f", this)
}
