package com.example.minder.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.minder.ui.theme.OutfitFont

// Lama - One shared bottom bar for all main tabs, always left-to-right.
@Composable
fun MinderBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
            val tabs = listOf("Home", "Statistics", "Progress", "Settings")
            tabs.forEachIndexed { index, label ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = {
                        if (index == 3) onSettingsClick() else onTabSelected(index)
                    },
                    icon = {
                        when (index) {
                            0 -> Icon(Icons.Default.Home, contentDescription = null)
                            1 -> Icon(Icons.Default.Info, contentDescription = null)
                            2 -> Icon(Icons.Default.CheckCircle, contentDescription = null)
                            else -> Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    },
                    label = { Text(label, fontFamily = OutfitFont) }
                )
            }
        }
    }
}
