
package com.example.minder.features.app_selection

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.ui.theme.PacificoFont
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.ManropeFont
import com.example.minder.ui.theme.BodoniFont
import kotlinx.coroutines.launch

// Lama - Minder design system
private val Purple = Color(0xFF9061D8)
private val Teal = Color(0xFF38C9C5)
private val DarkTeal = Color(0xFF0E8582)
private val Pink = Color(0xFFF3A1C8)
private val Ink = Color(0xFF302342)
private val Muted = Color(0xFF655B76)
private val LightTeal = Color(0xFFE5F8F6)
private val LightPurple = Color(0xFFF5EEFC)
private val PaleTeal = Color(0xFFF1FCFB)
private val ErrorRed = Color(0xFFB3346B)

data class SelectableApp(
    val id: String,
    val name: String,
    val packageName: String,
    val isSelected: Boolean = false,
    val dailyLimitMinutes: Int? = null
) {
    fun getFormattedLimit(): String {
        val total = dailyLimitMinutes ?: return "Set limit"
        val hours = total / 60
        val minutes = total % 60

        return when {
            hours == 0 -> "$minutes min/day"
            minutes == 0 -> "$hours h/day"
            else -> "$hours h $minutes min/day"
        }
    }
}

// Keep real Android installed-app discovery.
fun getInstalledSystemApps(
    context: Context
): List<SelectableApp> {
    val pm = context.packageManager

    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfos =
        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {
            pm.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, 0)
        }

    return resolveInfos.mapNotNull { info ->
        val packageName = info.activityInfo.packageName

        if (packageName == context.packageName) {
            null
        } else {
            SelectableApp(
                id = packageName,
                name = info.loadLabel(pm).toString(),
                packageName = packageName
            )
        }
    }
        .distinctBy { it.packageName }
        .sortedBy { it.name.lowercase() }
}

@Composable
fun AppSelectionScreen(
    initialSelectedApps: List<SelectableApp> = emptyList(),
    onBackClick: () -> Unit = {},
    onDoneClick: (List<SelectableApp>) -> Unit = {}
) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var allApps by remember {
        mutableStateOf<List<SelectableApp>>(emptyList())
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf("Suggested")
    }

    val suggestions = listOf(
        "TikTok",
        "Instagram",
        "YouTube",
        "Snapchat"
    )

    // Restore selected apps and their saved daily limits.
    LaunchedEffect(context, initialSelectedApps) {
        val installedApps = getInstalledSystemApps(context)

        allApps = installedApps.map { app ->
            val saved = initialSelectedApps.firstOrNull {
                it.packageName == app.packageName
            }

            if (saved != null) {
                app.copy(
                    isSelected = true,
                    dailyLimitMinutes = saved.dailyLimitMinutes
                )
            } else {
                app
            }
        }
    }

    val selectedCount = allApps.count { it.isSelected }

    val filteredApps = allApps.filter { app ->
        val matchesSearch = app.name.contains(
            searchQuery.trim(),
            ignoreCase = true
        )

        val matchesFilter = when (selectedFilter) {
            "Selected" -> app.isSelected

            "Social Media" -> isSocialMediaApp(app)

            "Suggested" -> {
                isSuggestedApp(app)
            }

            else -> true
        }

        matchesSearch && matchesFilter
    }

    fun updateApp(
        packageName: String,
        transform: (SelectableApp) -> SelectableApp
    ) {
        allApps = allApps.map { app ->
            if (app.packageName == packageName) {
                transform(app)
            } else {
                app
            }
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Scaffold(
            containerColor = Color.White,
            snackbarHost = {
                SnackbarHost(snackbar)
            },
            bottomBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 12.dp
                            )
                    ) {
                        Text(
                            text = "$selectedCount apps selected",
                            fontFamily = OutfitFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Muted
                        )

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val selected = allApps.filter {
                                    it.isSelected
                                }

                                when {
                                    selected.isEmpty() -> {
                                        scope.launch {
                                            snackbar.showSnackbar(
                                                "Please select at least one app."
                                            )
                                        }
                                    }

                                    selected.any {
                                        it.dailyLimitMinutes == null ||
                                                it.dailyLimitMinutes !in 1..1440
                                    } -> {
                                        scope.launch {
                                            snackbar.showSnackbar(
                                                "Set a valid daily limit for every selected app."
                                            )
                                        }
                                    }

                                    else -> {
                                        onDoneClick(selected)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkTeal
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                DarkTeal,
                                                Teal,
                                                Purple
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Continue",
                                    fontFamily = BodoniFont,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "Minder",
                        fontFamily = PacificoFont,
                        fontSize = 31.sp,
                        color = Purple
                    )

                    Spacer(Modifier.weight(1f))

                    Spacer(Modifier.width(48.dp))
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Apps & Daily Limits",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 29.sp,
                    lineHeight = 35.sp,
                    color = Ink
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Choose the apps you want to restrict " +
                            "and set a daily usage limit for each one.",
                    fontFamily = JakartaFont,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Muted
                )

                Spacer(Modifier.height(18.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.isNotBlank()) {
                            selectedFilter = "All Apps"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Search for an app...",
                            fontFamily = JakartaFont
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Teal
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal,
                        unfocusedBorderColor = LightTeal,
                        focusedContainerColor = PaleTeal,
                        unfocusedContainerColor = PaleTeal,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        cursorColor = Purple
                    )
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Quick search",
                    fontFamily = OutfitFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Muted
                )

                Spacer(Modifier.height(8.dp))

                // Quick search suggestions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(
                                    LightPurple,
                                    RoundedCornerShape(50)
                                )
                                .clickable {
                                    searchQuery = suggestion
                                    selectedFilter = "All Apps"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = suggestion,
                                fontFamily = OutfitFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Purple,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Filters
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf(
                        "Suggested",
                        "Social Media",
                        "All Apps",
                        "Selected"
                    ).forEach { filter ->
                        val active = selectedFilter == filter

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .background(
                                    if (active) Teal else PaleTeal,
                                    RoundedCornerShape(50)
                                )
                                .border(
                                    1.dp,
                                    if (active) Teal else LightTeal,
                                    RoundedCornerShape(50)
                                )
                                .clickable {
                                    selectedFilter = filter
                                    searchQuery = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                fontFamily = OutfitFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (active) {
                                    Color.White
                                } else {
                                    DarkTeal
                                },
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Apps and their limits on the same page.
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = filteredApps,
                        key = { it.packageName }
                    ) { app ->

                        val icon = remember(app.packageName) {
                            getAppIconBitmap(
                                context,
                                app.packageName
                            )
                        }

                        AppLimitCard(
                            app = app,
                            appIcon = icon,
                            onSelectedChange = { checked ->
                                updateApp(app.packageName) {
                                    it.copy(
                                        isSelected = checked,
                                        dailyLimitMinutes =
                                            if (checked) {
                                                it.dailyLimitMinutes
                                            } else {
                                                null
                                            }
                                    )
                                }
                            },
                            onLimitChange = { minutes ->
                                updateApp(app.packageName) {
                                    it.copy(
                                        isSelected = true,
                                        dailyLimitMinutes = minutes
                                    )
                                }
                            }
                        )
                    }

                    if (filteredApps.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No apps found",
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = "Try another name or choose All Apps.",
                                    fontFamily = JakartaFont,
                                    fontSize = 13.sp,
                                    color = Muted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppLimitCard(
    app: SelectableApp,
    appIcon: ImageBitmap?,
    onSelectedChange: (Boolean) -> Unit,
    onLimitChange: (Int) -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (app.isSelected) PaleTeal else Color.White,
                shape
            )
            .border(
                1.5.dp,
                if (app.isSelected) Teal else LightTeal,
                shape
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            LightPurple,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.name.take(1),
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                    text = if (app.isSelected) {
                        app.getFormattedLimit()
                    } else {
                        "Tap to select"
                    },
                    fontFamily = JakartaFont,
                    fontSize = 12.sp,
                    color = if (app.isSelected) DarkTeal else Muted
                )
            }

            Checkbox(
                checked = app.isSelected,
                onCheckedChange = onSelectedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Teal,
                    uncheckedColor = Teal,
                    checkmarkColor = Color.White
                )
            )
        }

        if (app.isSelected) {
            Spacer(Modifier.height(14.dp))

            HorizontalDivider(color = LightTeal)

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Daily Usage Limit",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Ink
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "Set hours and minutes for this app.",
                fontFamily = JakartaFont,
                fontSize = 12.sp,
                color = Muted
            )

            Spacer(Modifier.height(12.dp))

            val total = app.dailyLimitMinutes ?: 0
            val hours = total / 60
            val minutes = total % 60

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        LightTeal,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeSelector(
                    value = hours,
                    label = "Hours",
                    range = 0..24,
                    modifier = Modifier.weight(1f),
                    onValueChange = { newHours ->
                        val newTotal = newHours * 60 + minutes

                        if (newTotal in 1..1440) {
                            onLimitChange(newTotal)
                        }
                    }
                )

                Text(
                    text = ":",
                    fontFamily = ManropeFont,
                    fontSize = 28.sp,
                    color = Purple
                )

                TimeSelector(
                    value = minutes,
                    label = "Minutes",
                    range = 0..59,
                    modifier = Modifier.weight(1f),
                    onValueChange = { newMinutes ->
                        val newTotal = hours * 60 + newMinutes

                        if (newTotal in 1..1440) {
                            onLimitChange(newTotal)
                        }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (app.dailyLimitMinutes != null) {
                    "Daily limit: ${app.getFormattedLimit()}"
                } else {
                    "Choose a daily usage limit"
                },
                fontFamily = JakartaFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = DarkTeal
            )
        }
    }
}

@Composable
private fun TimeSelector(
    value: Int,
    label: String,
    range: IntRange,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                if (value < range.last) {
                    onValueChange(value + 1)
                }
            },
            enabled = value < range.last
        ) {
            Text(
                text = "▲",
                fontSize = 19.sp,
                color = DarkTeal
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(14.dp)
                )
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString().padStart(2, '0'),
                fontFamily = ManropeFont,
                fontSize = 31.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )
        }

        IconButton(
            onClick = {
                if (value > range.first) {
                    onValueChange(value - 1)
                }
            },
            enabled = value > range.first
        ) {
            Text(
                text = "▼",
                fontSize = 19.sp,
                color = DarkTeal
            )
        }

        Text(
            text = label,
            fontFamily = JakartaFont,
            fontSize = 12.sp,
            color = Muted
        )
    }
}

// Suggestions are matched against installed apps only.
private fun isSuggestedApp(app: SelectableApp): Boolean {
    val name = app.name.lowercase()
    val pkg = app.packageName.lowercase()

    return listOf(
        "tiktok",
        "instagram",
        "youtube",
        "snapchat",
        "facebook",
        "twitter"
    ).any { keyword ->
        name.contains(keyword) || pkg.contains(keyword)
    }
}

private fun isSocialMediaApp(app: SelectableApp): Boolean {
    val name = app.name.lowercase()
    val pkg = app.packageName.lowercase()

    return listOf(
        "instagram",
        "tiktok",
        "snapchat",
        "facebook",
        "twitter",
        "reddit",
        "pinterest",
        "threads",
        "youtube"
    ).any { keyword ->
        name.contains(keyword) || pkg.contains(keyword)
    }
}
