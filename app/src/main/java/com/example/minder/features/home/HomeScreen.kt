package com.example.minder.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.core.navigation.MinderBottomBar
import com.example.minder.features.app_selection.SelectableApp
import com.example.minder.features.app_selection.getAppIconBitmap
import com.example.minder.ui.theme.PacificoFont
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.ManropeFont
import com.example.minder.ui.theme.BodoniFont
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// Lama - Minder web-inspired design
private val Purple = Color(0xFF9061D8)
private val Teal = Color(0xFF38C9C5)
private val DarkTeal = Color(0xFF1B837F)
private val Pink = Color(0xFFF3A1C8)
private val Ink = Color(0xFF302342)
private val Muted = Color(0xFF655B76)
private val LightTeal = Color(0xFFE5F8F6)
private val Lavender = Color(0xFFF5EEFC)
private val SoftPink = Color(0xFFFCECF5)
private val SoftBlue = Color(0xFFEDF6FE)
private val PaleTeal = Color(0xFFF1FCFB)
private val BorderTeal = Color(0xFFD3F1EE)

@Composable
fun HomeScreen(
    selectedApps: List<SelectableApp> = emptyList(),
    appUsageMap: Map<String, Int> = emptyMap(),
    todayUsageMinutes: Int? = null,
    challengesCompleted: Int? = null,
    onSettingsClick: () -> Unit = {},
    onAddAppClick: () -> Unit = {},
    onEditAppClick: (String, Int) -> Unit = { _, _ -> },
    onDeleteAppClick: (String) -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val totalLimit = selectedApps.sumOf {
        it.dailyLimitMinutes ?: 0
    }

    val usedTotal = if (selectedApps.isNotEmpty() &&
        selectedApps.all { appUsageMap.containsKey(it.packageName) }
    ) {
        selectedApps.sumOf {
            appUsageMap[it.packageName] ?: 0
        }
    } else null

    val usageProgress = if (usedTotal != null && totalLimit > 0) {
        "${(usedTotal * 100 / totalLimit)}%"
    } else "—"

    var editingApp by remember {
        mutableStateOf<SelectableApp?>(null)
    }

    var deletingApp by remember {
        mutableStateOf<SelectableApp?>(null)
    }

    // Lama - Force left-to-right alignment throughout the Home screen.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Purple
                        )
                    }

                    Spacer(Modifier.width(9.dp))

                    Text(
                        "Minder",
                        fontFamily = PacificoFont,
                        fontSize = 27.sp,
                        color = Purple,
                        modifier = Modifier.weight(1f)
                    )

                }

                Spacer(Modifier.height(18.dp))

                Text(
                    "Your Digital Wellbeing",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 29.sp,
                    lineHeight = 35.sp,
                    color = Ink
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Small steps today, better habits tomorrow.",
                    fontFamily = JakartaFont,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Muted
                )

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewCard(
                        title = "Today's Screen Time",
                        value = todayUsageMinutes?.let(::formatTime) ?: "—",
                        icon = Icons.Default.DateRange,
                        background = Lavender,
                        modifier = Modifier.weight(1f)
                    )

                    OverviewCard(
                        title = "Challenges Completed",
                        value = challengesCompleted?.toString() ?: "—",
                        icon = Icons.Default.CheckCircle,
                        background = SoftPink,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewCard(
                        title = "Restricted Apps",
                        value = selectedApps.size.toString(),
                        icon = Icons.Default.Lock,
                        background = SoftBlue,
                        modifier = Modifier.weight(1f)
                    )

                    OverviewCard(
                        title = "Usage Progress",
                        value = usageProgress,
                        icon = Icons.Default.Info,
                        background = LightTeal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(34.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Restricted Apps",
                            fontFamily = OutfitFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Text(
                            "Manage your apps and daily usage limits.",
                            fontFamily = JakartaFont,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = Muted
                        )
                    }

                    Button(
                        onClick = onAddAppClick,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(
                            horizontal = 14.dp,
                            vertical = 9.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Teal,
                            contentColor = Ink
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            "Add Apps",
                            fontFamily = BodoniFont,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (selectedApps.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaleTeal)
                            .padding(25.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = DarkTeal,
                            modifier = Modifier.size(30.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "No restricted apps",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            "Add apps to start managing your daily limits.",
                            fontFamily = JakartaFont,
                            fontSize = 12.sp,
                            color = Muted
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = onAddAppClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Teal
                            )
                        ) {
                            Text("Add Apps", color = Ink)
                        }
                    }
                } else {
                    selectedApps.forEach { app ->
                        val usage = appUsageMap[app.packageName]

                        AppUsageCard(
                            app = app,
                            usedMinutes = usage,
                            onEdit = {
                                editingApp = app
                            },
                            onDelete = {
                                deletingApp = app
                            }
                        )

                        Spacer(Modifier.height(14.dp))
                    }
                }

                Spacer(Modifier.height(26.dp))
            }

            MinderBottomBar(
                selectedTab = 0,
                onTabSelected = onTabSelected,
                onSettingsClick = onSettingsClick
            )
        }

        // Lama - Web-style time editor: steppers and quick presets.
        editingApp?.let { app ->
            var selectedMinutes by remember(app.packageName) {
                mutableIntStateOf(app.dailyLimitMinutes ?: 60)
            }
            val quickLimits = listOf(15, 30, 60, 90, 120)
            AlertDialog(
                onDismissRequest = { editingApp = null },
                title = {
                    Text("Edit Daily Limit", fontFamily = OutfitFont,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("${app.name} · Current limit: ${formatTime(app.dailyLimitMinutes ?: 0)}",
                            fontFamily = JakartaFont, fontSize = 13.sp, color = Muted)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TimeStepper(
                                title = "Hours", value = selectedMinutes / 60,
                                onMinus = { selectedMinutes = (selectedMinutes - 60).coerceAtLeast(0) },
                                onPlus = { selectedMinutes = (selectedMinutes + 60).coerceAtMost(1440) },
                                modifier = Modifier.weight(1f)
                            )
                            TimeStepper(
                                title = "Minutes", value = selectedMinutes % 60,
                                onMinus = {
                                    selectedMinutes = (selectedMinutes / 60) * 60 +
                                            ((selectedMinutes % 60 + 55) % 60)
                                },
                                onPlus = {
                                    selectedMinutes = (selectedMinutes / 60) * 60 +
                                            ((selectedMinutes % 60 + 5) % 60)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                quickLimits.take(3).forEach { minutes ->
                                    LimitChip(minutes, selectedMinutes == minutes,
                                        { selectedMinutes = minutes }, Modifier.weight(1f))
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                quickLimits.drop(3).forEach { minutes ->
                                    LimitChip(minutes, selectedMinutes == minutes,
                                        { selectedMinutes = minutes }, Modifier.weight(1f))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onEditAppClick(app.packageName, selectedMinutes)
                            editingApp = null
                        },
                        enabled = selectedMinutes in 1..1440,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Ink),
                        shape = RoundedCornerShape(50.dp)
                    ) { Text("Save Changes", fontFamily = BodoniFont) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { editingApp = null },
                        shape = RoundedCornerShape(50.dp)
                    ) { Text("Cancel", fontFamily = BodoniFont, color = Ink) }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        deletingApp?.let { app ->
            AlertDialog(
                onDismissRequest = { deletingApp = null },
                title = {
                    Text(
                        "Remove Restricted App?",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        "Remove ${app.name} from your restricted apps? " +
                                "Its daily limit will no longer be enforced.",
                        fontFamily = JakartaFont,
                        color = Color(0xFFE9DDF9)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteAppClick(app.packageName)
                        deletingApp = null
                    }) {
                        Text("Remove", color = Pink, fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingApp = null }) {
                        Text("Cancel", color = Teal, fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold)
                    }
                },
                containerColor = Ink,
                shape = RoundedCornerShape(24.dp)
            )
        }
    } // End left-to-right layout
}


@Composable
private fun OverviewCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 143.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = value,
            fontFamily = ManropeFont,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink
        )

        Text(
            text = title,
            fontFamily = OutfitFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Muted,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun AppUsageCard(
    app: SelectableApp,
    usedMinutes: Int?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val icon = remember(app.packageName) {
        getAppIconBitmap(context, app.packageName)
    }

    val limit = (app.dailyLimitMinutes ?: 0).coerceAtLeast(0)
    val used = usedMinutes?.coerceAtLeast(0)
    val remaining = used?.let { (limit - it).coerceAtLeast(0) }

    val percentage = if (used != null && limit > 0) {
        used.toFloat() / limit
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                BorderTeal,
                RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Lavender),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = app.name,
                        modifier = Modifier.size(42.dp)
                    )
                } else {
                    Text(
                        app.name.take(1),
                        fontFamily = OutfitFont,
                        color = Purple,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    fontFamily = OutfitFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Daily limit ${formatTime(limit)}",
                    fontFamily = JakartaFont,
                    fontSize = 13.sp,
                    color = Muted
                )
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit ${app.name}",
                    tint = Purple,
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ${app.name}",
                    tint = Color(0xFFB3346B),
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(Modifier.height(17.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UsageMetric(
                "Daily Limit",
                formatTime(limit),
                Modifier.weight(1f)
            )

            UsageMetric(
                "Used Today",
                used?.let(::formatTime) ?: "—",
                Modifier.weight(1f)
            )

            UsageMetric(
                "Remaining",
                remaining?.let(::formatTime) ?: "—",
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(13.dp))

        LinearProgressIndicator(
            progress = { percentage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(9.dp)),
            color = if (percentage > 1f) Pink else Teal,
            trackColor = LightTeal
        )

        Spacer(Modifier.height(7.dp))

        Text(
            text = if (used == null || limit == 0) {
                "No usage data yet"
            } else {
                "${(percentage * 100).toInt()}% of limit used"
            },
            fontFamily = JakartaFont,
            fontSize = 12.sp,
            color = Muted
        )
    }
}

@Composable
private fun UsageMetric(
    label: String,
    value: String,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = JakartaFont,
            fontSize = 12.sp,
            color = Muted,
            maxLines = 1
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = value,
            fontFamily = ManropeFont,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            maxLines = 1
        )
    }
}

private fun formatTime(minutes: Int): String {
    val safe = minutes.coerceAtLeast(0)
    val hours = safe / 60
    val mins = safe % 60

    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}
// Lama - Matches the Hours/Minutes controls in the HTML prototype.
@Composable
private fun TimeStepper(
    title: String, value: Int, onMinus: () -> Unit,
    onPlus: () -> Unit, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(PaleTeal).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontFamily = JakartaFont, fontSize = 12.sp, color = Muted)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            StepperButton("−", onMinus)
            Text(value.toString(), modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontFamily = ManropeFont, fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold, color = Ink)
            StepperButton("+", onPlus)
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(32.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(1.dp, BorderTeal, RoundedCornerShape(50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = ManropeFont, fontSize = 18.sp, color = Ink)
    }
}

@Composable
private fun LimitChip(
    minutes: Int, selected: Boolean, onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (minutes) {
        60 -> "1h"
        90 -> "1h 30min"
        120 -> "2h"
        else -> "$minutes min"
    }
    Box(
        modifier = modifier.clip(RoundedCornerShape(50))
            .background(if (selected) LightTeal else PaleTeal)
            .border(1.dp, if (selected) Teal else BorderTeal, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = JakartaFont, fontSize = 11.sp,
            color = if (selected) DarkTeal else Ink, maxLines = 1)
    }
}
