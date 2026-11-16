package com.example.minder.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.minder.features.app_selection.SelectableApp
import com.example.minder.features.app_selection.getInstalledSystemApps
import com.example.minder.features.app_selection.getAppIconBitmap
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont

// Lama - Home-only app picker; onboarding AppSelectionScreen remains unchanged.
@Composable
fun AddAppsScreen(
    existingPackages: Set<String>,
    onBack: () -> Unit,
    onAddApps: (List<SelectableApp>) -> Unit
) {
    val context = LocalContext.current
    val apps = remember(context, existingPackages) {
        getInstalledSystemApps(context).filterNot { it.packageName in existingPackages }
    }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    val purple = Color(0xFF9061D8)
    val teal = Color(0xFF38C9C5)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(containerColor = Color.White, bottomBar = {
            Surface(shadowElevation = 6.dp, color = Color.White) {
                Button(
                    onClick = { onAddApps(apps.filter { it.packageName in selected }) },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(18.dp).height(54.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = teal)
                ) {
                    Text("Add Selected Apps (${selected.size})", fontFamily = OutfitFont, color = Color(0xFF302342))
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).statusBarsPadding().padding(horizontal = 18.dp)) {
                TextButton(onClick = onBack) { Text("← Back", color = purple, fontFamily = OutfitFont) }
                Text("Add Apps", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 29.sp, color = Color(0xFF302342))
                Text("Select installed apps. Set their daily limits later on Home.", fontFamily = JakartaFont, color = Color(0xFF655B76))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = query, onValueChange = { query = it },
                    placeholder = { Text("Search apps...", fontFamily = JakartaFont) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(12.dp))
                val filtered = apps.filter { it.name.contains(query.trim(), ignoreCase = true) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filtered, key = { it.packageName }) { app ->
                        Row(Modifier.fillMaxWidth().clickable {
                            selected = if (app.packageName in selected) selected - app.packageName else selected + app.packageName
                        }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            val bitmap = remember(app.packageName) { getAppIconBitmap(context, app.packageName) }
                            if (bitmap != null) Image(bitmap, null, Modifier.size(42.dp))
                            else Text(app.name.take(1), modifier = Modifier.width(42.dp), color = purple)
                            Spacer(Modifier.width(12.dp))
                            Text(app.name, modifier = Modifier.weight(1f), fontFamily = OutfitFont, color = Color(0xFF302342))
                            Checkbox(checked = app.packageName in selected,
                                onCheckedChange = { checked -> selected = if (checked) selected + app.packageName else selected - app.packageName },
                                colors = CheckboxDefaults.colors(checkedColor = teal))
                        }
                        HorizontalDivider(color = Color(0xFFE5F8F6))
                    }
                    if (filtered.isEmpty()) item { Text("No available apps found", modifier = Modifier.padding(20.dp), fontFamily = JakartaFont) }
                }
            }
        }
    }
}
