package com.example.minder.features.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minder.services.usage.UsagePermissionManager
//Ragahd-: Import lifecycle-aware permission refresh support
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
//Ragahd-: Import accessibility service state detection
import android.content.ComponentName
import com.example.minder.services.monitoring.AppAccessibilityService


private val PrimaryDark = Color(0xFF001B3D)
private val PrimaryBlue = Color(0xFF2F8FFF)
private val CardBackground = Color(0xFF082247)
private val IconBoxBackground = Color(0xFF1652A0)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A9FB8)
private val BorderColor = Color(0xFF143866)
private val ErrorColor = Color(0xFFFF5252)



data class PermissionItemData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: (Context) -> Unit
)

@Composable
fun PermissionsScreen(
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val usagePermissionManager = remember { UsagePermissionManager(context) }

    // حالة لإظهار رسالة التنبيه
    var showErrorAlert by remember { mutableStateOf(false) }

    //Ragahd-: Track the current Usage Access permission state
    var hasUsageAccess by remember {
        mutableStateOf(usagePermissionManager.hasUsageAccess())
    }

  //Ragahd-: Track the current Accessibility Service permission state
    var hasAccessibilityAccess by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context))
    }

   //Ragahd-: Track the current overlay permission state
    var hasOverlayAccess by remember {
        mutableStateOf(hasOverlayPermission(context))
    }


//Ragahd-: Refresh permission state whenever the app returns to the foreground
    //Ragahd-: Refresh required permission states when returning to the app
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = usagePermissionManager.hasUsageAccess()
                hasAccessibilityAccess = isAccessibilityServiceEnabled(context)
                hasOverlayAccess = hasOverlayPermission(context)

                if (hasUsageAccess && hasAccessibilityAccess) {
                    showErrorAlert = false
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionsList = remember {
        listOf(
            PermissionItemData(
                title = "Usage Access",
                description = "Track your app usage",
                icon = Icons.Default.Settings,
                action = { ctx ->
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    ctx.startActivity(intent)
                }
            ),
            PermissionItemData(
                title = "Accessibility Service",
                description = "Block apps during challenges",
                icon = Icons.Default.Lock,
                action = { ctx ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    ctx.startActivity(intent)
                }
            ),
            PermissionItemData(
                title = "Display Over Other Apps",
                description = "Show challenge screen",
                icon = Icons.Default.Info,
                action = { ctx ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${ctx.packageName}")
                        )
                        ctx.startActivity(intent)
                    }
                }
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Allow necessary\npermissions",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "These permissions help the app work\nand help you stay on track.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                permissionsList.forEach { item ->
                    PermissionCard(
                        title = item.title,
                        description = item.description,
                        icon = item.icon,
                        onClick = { item.action(context) }
                    )
                }
            }
            //Ragahd-: Show the current Usage Access permission status
            Text(
                text = if (hasUsageAccess) {
                    "Usage Access: Granted"
                } else {
                    "Usage Access: Not granted"
                },
                color = if (hasUsageAccess) Color(0xFF4CAF50) else ErrorColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            // عرض رسالة الخطأ إذا حاول المتابعة بدون منح الصلاحيات الإلزامية
            if (showErrorAlert) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "الرجاء منح الصلاحيات الإلزامية (مثل Usage Access) للمتابعة.",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    //Ragahd-: Require Usage Access and Accessibility before continuing
                    onClick = {
                        hasUsageAccess = usagePermissionManager.hasUsageAccess()
                        hasAccessibilityAccess = isAccessibilityServiceEnabled(context)
                        hasOverlayAccess = hasOverlayPermission(context)

                        if (hasUsageAccess && hasAccessibilityAccess) {
                            showErrorAlert = false
                            onNextClick()
                        } else {
                            showErrorAlert = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Grant Permissions",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Learn more",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

//Ragahd-: Check whether the Minder accessibility service is enabled
private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val expectedService = ComponentName(
        context,
        AppAccessibilityService::class.java
    ).flattenToString()

    return enabledServices.split(":").any { service ->
        service.equals(expectedService, ignoreCase = true)
    }
}

//Ragahd-: Check whether overlay permission is granted
private fun hasOverlayPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            Settings.canDrawOverlays(context)
}
@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .border(1.dp, BorderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(IconBoxBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}