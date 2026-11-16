
package com.example.minder.features.permissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import com.example.minder.services.monitoring.AppAccessibilityService
import com.example.minder.services.usage.UsagePermissionManager

import com.example.minder.ui.theme.PacificoFont
import com.example.minder.ui.theme.OutfitFont
import com.example.minder.ui.theme.JakartaFont
import com.example.minder.ui.theme.BodoniFont


// Lama - Minder UI colours
private val Purple = Color(0xFF9061D8)
private val Teal = Color(0xFF38C9C5)
private val Pink = Color(0xFFF3A1C8)

private val Ink = Color(0xFF302342)
private val Muted = Color(0xFF655B76)

private val LightTeal = Color(0xFFE5F8F6)
private val DarkTeal = Color(0xFF0E7773)

private val ErrorColor = Color(0xFFC23D56)


// Lama - Permissions screen with two permissions only.
@Composable
fun PermissionsScreen(
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val usagePermissionManager = remember(context) {
        UsagePermissionManager(context)
    }

    // Usage Access permission state
    var hasUsageAccess by remember {
        mutableStateOf(
            usagePermissionManager.hasUsageAccess()
        )
    }

    // Accessibility permission state
    var hasAccessibilityAccess by remember {
        mutableStateOf(
            isAccessibilityServiceEnabled(context)
        )
    }

    var showErrorAlert by remember {
        mutableStateOf(false)
    }

    // Lama - Refresh permission states.
    fun refreshPermissions() {

        hasUsageAccess =
            usagePermissionManager.hasUsageAccess()

        hasAccessibilityAccess =
            isAccessibilityServiceEnabled(context)

        if (hasUsageAccess && hasAccessibilityAccess) {
            showErrorAlert = false
        }
    }

    // Ragahd - Refresh permissions when returning from Settings.
    DisposableEffect(lifecycleOwner, context) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            PermissionsBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Minder logo
                PermissionsLogo()

                Spacer(Modifier.height(4.dp))

                // Minder brand
                Text(
                    text = "Minder",
                    fontFamily = PacificoFont,
                    fontSize = 42.sp,
                    lineHeight = 64.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(Purple, Pink)
                        )
                    )
                )

                Spacer(Modifier.height(4.dp))

                // Page title
                Text(
                    text = "Permissions",
                    fontFamily = OutfitFont,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Two permissions to help you " +
                            "stay focused and build healthier habits.",
                    fontFamily = JakartaFont,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // Permission 1: Usage Access
                MinderPermissionCard(
                    title = "Usage Access",
                    description =
                        "See how long you spend in each app.",
                    icon = Icons.Default.Settings,
                    granted = hasUsageAccess,
                    buttonText = "Allow Usage Access",
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_USAGE_ACCESS_SETTINGS
                            )
                        )
                    }
                )

                Spacer(Modifier.height(14.dp))

                // Permission 2: Accessibility
                MinderPermissionCard(
                    title = "Accessibility",
                    description =
                        "Know which app is open so Minder " +
                                "can show challenges and block apps.",
                    icon = Icons.Default.Lock,
                    granted = hasAccessibilityAccess,
                    buttonText = "Enable Accessibility",
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                            )
                        )
                    }
                )

                Spacer(Modifier.height(20.dp))

                // Privacy information
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFF9F8FE),
                            RoundedCornerShape(18.dp)
                        )
                        .border(
                            1.dp,
                            Color(0xFFD7C4F0),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = "Your privacy matters. " +
                                "Minder uses these permissions " +
                                "to monitor usage and support " +
                                "focus challenges.",
                        modifier = Modifier.weight(1f),
                        fontFamily = JakartaFont,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Muted
                    )
                }

                // Error message
                if (showErrorAlert) {

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Please enable Usage Access " +
                                "and Accessibility to continue.",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = JakartaFont,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = ErrorColor,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Continue button
                Button(
                    onClick = {

                        refreshPermissions()

                        if (
                            hasUsageAccess &&
                            hasAccessibilityAccess
                        ) {
                            showErrorAlert = false
                            onNextClick()
                        } else {
                            showErrorAlert = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal
                    )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
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
                            fontSize = 20.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}


// Lama - Reusable permission card.
@Composable
private fun MinderPermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    granted: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {

    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (granted) {
                    Color(0xFFF1FCFB)
                } else {
                    Color.White
                },
                shape
            )
            .border(
                width = 1.5.dp,
                color = if (granted) {
                    Teal
                } else {
                    Color(0xFFA5E5E2)
                },
                shape = shape
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        LightTeal,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkTeal,
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontFamily = OutfitFont,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                textAlign = TextAlign.Start
            )
        }

        Text(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = JakartaFont,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = Muted,
            textAlign = TextAlign.Start
        )

        if (granted) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(50)
                    )
                    .border(
                        1.5.dp,
                        Teal,
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "✓  Granted",
                    fontFamily = JakartaFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTeal
                )
            }

        } else {

            OutlinedButton(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, Teal),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = DarkTeal
                )
            ) {

                Text(
                    text = buttonText,
                    fontFamily = BodoniFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


// Lama - Background matching WelcomeScreen.
@Composable
private fun PermissionsBackground() {

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        drawCircle(
            color = LightTeal.copy(alpha = 0.75f),
            radius = size.width * 0.52f,
            center = Offset(
                size.width * 1.05f,
                -size.width * 0.12f
            )
        )

        drawCircle(
            color = Color(0xFFF5EEFC),
            radius = size.width * 0.44f,
            center = Offset(
                -size.width * 0.12f,
                size.height * 0.94f
            )
        )

        drawCircle(
            color = Teal.copy(alpha = 0.55f),
            radius = 5.dp.toPx(),
            center = Offset(
                size.width * 0.08f,
                size.height * 0.18f
            )
        )

        drawCircle(
            color = Pink.copy(alpha = 0.7f),
            radius = 5.dp.toPx(),
            center = Offset(
                size.width * 0.91f,
                size.height * 0.31f
            )
        )
    }
}


// Lama - Minder shield and clock logo.
@Composable
private fun PermissionsLogo() {

    Canvas(
        modifier = Modifier.size(105.dp)
    ) {

        val sx = size.width / 120f
        val sy = size.height / 120f

        fun p(x: Float, y: Float): Offset {
            return Offset(x * sx, y * sy)
        }

        val shield = Path().apply {

            moveTo(60f * sx, 14f * sy)
            lineTo(88f * sx, 24f * sy)
            lineTo(88f * sx, 56f * sy)

            cubicTo(
                88f * sx, 76f * sy,
                76f * sx, 90f * sy,
                60f * sx, 98f * sy
            )

            cubicTo(
                44f * sx, 90f * sy,
                32f * sx, 76f * sy,
                32f * sx, 56f * sy
            )

            lineTo(32f * sx, 24f * sy)
            close()
        }

        drawPath(
            path = shield,
            color = Color(0xFFF5EEFC)
        )

        drawPath(
            path = shield,
            brush = Brush.linearGradient(
                colors = listOf(Purple, Teal),
                start = p(30f, 20f),
                end = p(90f, 95f)
            ),
            style = Stroke(
                width = 5f * sx,
                join = StrokeJoin.Round
            )
        )

        drawCircle(
            color = Purple,
            radius = 14f * sx,
            center = p(60f, 53f),
            style = Stroke(width = 4f * sx)
        )

        drawLine(
            color = Teal,
            start = p(60f, 45f),
            end = p(60f, 54f),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Teal,
            start = p(60f, 54f),
            end = p(66f, 59f),
            strokeWidth = 4f * sx,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = Pink,
            radius = 3.5f * sx,
            center = p(20f, 38f)
        )

        drawCircle(
            color = Teal,
            radius = 3f * sx,
            center = p(102f, 44f)
        )
    }
}


// Ragahd - Check the real Accessibility Service state.
private fun isAccessibilityServiceEnabled(
    context: Context
): Boolean {

    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val expectedService = ComponentName(
        context,
        AppAccessibilityService::class.java
    ).flattenToString()

    return enabledServices.split(":").any { service ->
        service.equals(
            expectedService,
            ignoreCase = true
        )
    }
}
