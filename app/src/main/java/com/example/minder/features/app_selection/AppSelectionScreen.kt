package com.example.minder.features.app_selection

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val PrimaryDark = Color(0xFF001B3D)
private val PrimaryBlue = Color(0xFF2F8FFF)
private val CardBackground = Color(0xFF082247)
private val BoxBackground = Color(0xFF031630)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A9FB8)
private val BorderColor = Color(0xFF143866)
private val ErrorRed = Color(0xFFEF5350)

data class SelectableApp(
    val id: String,
    val name: String,
    val packageName: String,
    val isSelected: Boolean = false,
    val dailyLimitMinutes: Int? = null // Null يعني أنه لم يحدد وقت بعد
) {
    fun getFormattedLimit(): String {
        return when {
            dailyLimitMinutes == null -> "Set limit"
            dailyLimitMinutes < 60 -> "${dailyLimitMinutes}m/day"
            dailyLimitMinutes % 60 == 0 -> "${dailyLimitMinutes / 60}h/day"
            else -> "${dailyLimitMinutes / 60}h ${dailyLimitMinutes % 60}m/day"
        }
    }
}

// جلب جميع التطبيقات المنزلة في الجهاز
fun getInstalledSystemApps(context: Context): List<SelectableApp> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(mainIntent, 0)
    }

    val apps = mutableListOf<SelectableApp>()

    for (info in resolveInfos) {
        val packageName = info.activityInfo.packageName
        if (packageName != context.packageName) {
            val appName = info.loadLabel(pm).toString()
            apps.add(
                SelectableApp(
                    id = packageName,
                    name = appName,
                    packageName = packageName,
                    isSelected = false,
                    dailyLimitMinutes = null
                )
            )
        }
    }

    return apps.distinctBy { it.packageName }.sortedBy { it.name }
}

@Composable
fun AppSelectionScreen(
    onBackClick: () -> Unit = {},
    onDoneClick: (List<SelectableApp>) -> Unit = {}
) {
    val context = LocalContext.current
    var allApps by remember { mutableStateOf<List<SelectableApp>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForLimit by remember { mutableStateOf<SelectableApp?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        allApps = getInstalledSystemApps(context)
    }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PrimaryDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    Text(
                        text = "4/5",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Apps",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose apps and set a daily limit for each selected app.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PrimaryBlue
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredApps, key = { it.id }) { app ->
                            val appIcon = remember(app.packageName) { getAppIconBitmap(context, app.packageName) }

                            AppRowItem(
                                appName = app.name,
                                appIcon = appIcon,
                                isSelected = app.isSelected,
                                formattedLimit = app.getFormattedLimit(),
                                hasLimitSet = app.dailyLimitMinutes != null,
                                onCheckedChange = { checked ->
                                    allApps = allApps.map {
                                        if (it.id == app.id) {
                                            if (checked && it.dailyLimitMinutes == null) {
                                                selectedAppForLimit = it
                                            }
                                            it.copy(isSelected = checked, dailyLimitMinutes = if (!checked) null else it.dailyLimitMinutes)
                                        } else it
                                    }
                                },
                                onLimitClick = {
                                    selectedAppForLimit = app
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp),
                                thickness = 1.dp,
                                color = BorderColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val selectedApps = allApps.filter { it.isSelected }
                        val hasUnsetLimit = selectedApps.any { it.dailyLimitMinutes == null }

                        if (hasUnsetLimit) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Please set a daily limit for all selected apps before proceeding."
                                )
                            }
                        } else if (selectedApps.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Please select at least one app."
                                )
                            }
                        } else {
                            onDoneClick(selectedApps)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Done",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    selectedAppForLimit?.let { app ->
        DailyLimitDialog(
            appName = app.name,
            currentMinutes = app.dailyLimitMinutes,
            onDismiss = { selectedAppForLimit = null },
            onLimitSet = { newMinutes ->
                allApps = allApps.map {
                    if (it.id == app.id) {
                        it.copy(dailyLimitMinutes = newMinutes, isSelected = true)
                    } else it
                }
                selectedAppForLimit = null
            }
        )
    }
}

@Composable
private fun AppRowItem(
    appName: String,
    appIcon: ImageBitmap?,
    isSelected: Boolean,
    formattedLimit: String,
    hasLimitSet: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLimitClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .toggleable(
                value = isSelected,
                role = Role.Checkbox,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryBlue,
                    uncheckedColor = TextSecondary,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF021226)),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = appName,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        val dotSize = size.width * 0.38f
                        val gap = size.width * 0.24f

                        drawRoundRect(
                            color = TextSecondary,
                            topLeft = Offset(0f, 0f),
                            size = Size(dotSize, dotSize),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = TextSecondary,
                            topLeft = Offset(dotSize + gap, 0f),
                            size = Size(dotSize, dotSize),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = TextSecondary,
                            topLeft = Offset(0f, dotSize + gap),
                            size = Size(dotSize, dotSize),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = TextSecondary,
                            topLeft = Offset(dotSize + gap, dotSize + gap),
                            size = Size(dotSize, dotSize),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = appName,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onLimitClick() }
        ) {
            if (isSelected) {
                Text(
                    text = formattedLimit,
                    color = if (hasLimitSet) PrimaryBlue else ErrorRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyLimitDialog(
    appName: String,
    currentMinutes: Int?,
    onDismiss: () -> Unit,
    onLimitSet: (Int) -> Unit
) {
    val initialMinutes = currentMinutes ?: 45
    var isCustomSelected by remember { mutableStateOf(currentMinutes != null && currentMinutes !in listOf(15, 30, 60, 120)) }
    var selectedMinutes by remember { mutableStateOf(initialMinutes) }
    var customInputText by remember { mutableStateOf(initialMinutes.toString()) }

    val presetOptions = listOf(
        "15 min" to 15,
        "30 min" to 30,
        "1 hour" to 60,
        "2 hours" to 120
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Daily Limit for $appName",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                presetOptions.forEach { (label, minutes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .selectable(
                                selected = (!isCustomSelected && selectedMinutes == minutes),
                                onClick = {
                                    isCustomSelected = false
                                    selectedMinutes = minutes
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (!isCustomSelected && selectedMinutes == minutes),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryBlue,
                                unselectedColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = label, color = TextPrimary, fontSize = 15.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .selectable(
                            selected = isCustomSelected,
                            onClick = { isCustomSelected = true },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isCustomSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = PrimaryBlue,
                            unselectedColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Custom", color = TextPrimary, fontSize = 15.sp)
                }

                if (isCustomSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BoxBackground)
                            .border(1.dp, PrimaryBlue, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = customInputText,
                                onValueChange = { customInputText = it.filter { char -> char.isDigit() } },
                                modifier = Modifier.width(72.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "minutes / day",
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isCustomSelected) {
                        val parsed = customInputText.toIntOrNull()
                        onLimitSet(if (parsed != null && parsed > 0) parsed else 45)
                    } else {
                        onLimitSet(selectedMinutes)
                    }
                }
            ) {
                Text("Save", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}