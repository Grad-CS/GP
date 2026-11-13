package com.example.minder.features.blocking

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.domain.usecase.usage.CheckDailyLimitUseCase
import kotlinx.coroutines.delay
import java.util.Calendar
import androidx.compose.foundation.layout.Row

private val PrimaryDark = Color(0xFF071B33)
private val PrimaryBlue = Color(0xFF3289F5)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFB8C7D9)
private val CircleBackground = Color(0xFF0B2747)

@Composable
fun BlockingScreen(
    appId: Int,
    interventionId: Int,
    initialRemainingSeconds: Long,
    checkDailyLimitUseCase: CheckDailyLimitUseCase,

    //Ragahd-: Indicates whether today's extension has already been used.
    extensionAlreadyUsedToday: Boolean?,

    //Ragahd-: Handles the selected extension duration.
    onRequestExtension: (Int) -> Unit,

    //Ragahd-: Exits the blocked app without granting an extension.
    onExitApp: () -> Unit,

    onTimeFinished: () -> Unit
) {
    var remainingSeconds by remember {
        mutableLongStateOf(initialRemainingSeconds)
    }

    var limitExceeded by remember {
        mutableStateOf(false)
    }
    //Ragahd-: Tracks the selected extension duration.
    var selectedExtensionMinutes by remember {
        mutableStateOf(10)
    }

    LaunchedEffect(appId) {
        val calendar = Calendar.getInstance()

        calendar.set(
            Calendar.HOUR_OF_DAY,
            0
        )
        calendar.set(
            Calendar.MINUTE,
            0
        )
        calendar.set(
            Calendar.SECOND,
            0
        )
        calendar.set(
            Calendar.MILLISECOND,
            0
        )

        val today = calendar.timeInMillis

        limitExceeded = checkDailyLimitUseCase(
            appId = appId,
            date = today
        )
    }

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds <= 0) {
            onTimeFinished()
            return@LaunchedEffect
        }

        delay(1000)
        remainingSeconds--
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    val timeText = String.format(
        "%02d:%02d:%02d",
        hours,
        minutes,
        seconds
    )

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

            Spacer(modifier = Modifier.height(55.dp))

            Text(
                text = "Minder",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(62.dp))

            Text(
                text = "Time’s up!",
                color = TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "You’ve reached your daily limit for this app.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You can use this app again tomorrow.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(42.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(224.dp)
                    .background(
                        CircleBackground,
                        RoundedCornerShape(112.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "⏱",
                        color = PrimaryBlue,
                        fontSize = 34.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = timeText,
                        color = TextPrimary,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Daily usage limit",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(38.dp))

            Text(
                text = "Your daily limit has ended.",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your daily usage limit will reset tomorrow.",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

//Ragahd-: Shows extension options only after checking today's request.
            if (extensionAlreadyUsedToday == false) {

                Text(
                    text = "Need more time?",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(10, 15, 20).forEach { minutes ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selectedExtensionMinutes == minutes)
                                        PrimaryBlue
                                    else
                                        CircleBackground,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedExtensionMinutes = minutes
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$minutes min",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue, RoundedCornerShape(12.dp))
                        .clickable {
                            onRequestExtension(selectedExtensionMinutes)
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Extend Time",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

            } else if (extensionAlreadyUsedToday == true) {

                Text(
                    text = "You've already used your daily extension.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

            } else {

                Text(
                    text = "Checking extension availability...",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CircleBackground, RoundedCornerShape(12.dp))
                    .clickable {
                        onExitApp()
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Exit App",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Stay focused. One step at a time.",
                color = Color(0xFF7F95AD),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(35.dp))
        }
    }
}