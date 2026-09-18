package com.example.minder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minder.services.usage.UsagePermissionManager
import com.example.minder.services.usage.UsageStatsService
import com.example.minder.ui.theme.MinderTheme

class MainActivity : ComponentActivity() {
    // Lama - Manages and checks the Usage Access permission
    private lateinit var usagePermissionManager: UsagePermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        // Lama - Initializes the Usage Access permission manager
        usagePermissionManager = UsagePermissionManager(this)

        setContent {
            MinderTheme {
                // Lama - Creates the screen for testing app usage tracking
                UsageTestScreen()
            }
        }
    }
    // Lama - Creates the screen for testing app usage tracking
    @Composable
    fun UsageTestScreen() {
        // Lama - Stores the usage data that will be displayed to the user
        var hasPermission by remember {
            mutableStateOf(usagePermissionManager.hasUsageAccess())
        }
        // Lama - Stores the usage data that will be displayed to the user
        var usageResult by remember {
            mutableStateOf("No usage data loaded yet")
        }

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            // Lama - Organizes the usage tracking information vertically
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Minder - Usage Tracking Test"
                )
                // Lama - Displays whether Usage Access permission is granted
                Text(
                    text = if (hasPermission) {
                        "Usage Access: Granted"
                    } else {
                        "Usage Access: Not Granted"
                    }
                )

                if (!hasPermission) {
                    // Lama - Opens Android settings to enable Usage Access
                    Button(
                        onClick = {
                            usagePermissionManager.openUsageAccessSettings()
                        }
                    ) {
                        Text("Enable Usage Access")
                    }

                } else {
                    // Lama - Reads today's app usage when the user clicks the button
                    Button(
                        onClick = {

                            // Lama - Creates the service used to retrieve app usage statistics
                            val usageStatsService =
                                UsageStatsService(this@MainActivity)
                            // Lama - Retrieves today's usage data for applications
                            val apps =
                                usageStatsService.getTodayUsage()

                            // Lama - Converts the usage data into readable minutes
                            usageResult =
                                if (apps.isEmpty()) {

                                    "No usage data found"

                                } else {

                                    apps.joinToString("\n") { app ->

                                        val minutes =
                                            app.usageTimeMillis / 60000

                                        "${app.packageName} = $minutes min"
                                    }
                                }
                        }
                    ) {
                        Text("Read Today's Usage")
                    }
                }

                // Lama - Displays the retrieved application usage results
                Text(
                    text = usageResult
                )
            }
        }
    }

    // Lama - Refreshes the screen after returning from Usage Access settings
    override fun onResume() {
        super.onResume()

        if (::usagePermissionManager.isInitialized) {

            setContent {
                MinderTheme {
                    UsageTestScreen()
                }
            }
        }
    }
}