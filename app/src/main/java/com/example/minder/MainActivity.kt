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
import androidx.lifecycle.lifecycleScope
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.data.repository.RestrictedAppRepositoryImpl
import com.example.minder.data.repository.UsageSessionRepositoryImpl
import com.example.minder.domain.usecase.usage.SaveUsageSessionsUseCase
import com.example.minder.services.usage.UsagePermissionManager
import com.example.minder.services.usage.UsageStatsService
import com.example.minder.ui.theme.MinderTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Lama - Manages and checks the Usage Access permission
    private lateinit var usagePermissionManager: UsagePermissionManager

    // Lama - Provides access to the local Room database
    private lateinit var database: MinderDatabase

    // Lama - Reads Android application usage information
    private lateinit var usageStatsService: UsageStatsService

    // Lama - Connects usage data with the repositories and saves usage sessions
    private lateinit var saveUsageSessionsUseCase: SaveUsageSessionsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Lama - Initializes the Usage Access permission manager
        usagePermissionManager =
            UsagePermissionManager(this)

        // Lama - Initializes the Minder Room database
        database =
            MinderDatabase.getDatabase(applicationContext)

        // Lama - Initializes the Android usage statistics service
        usageStatsService =
            UsageStatsService(this)

        // Lama - Initializes the restricted application repository
        val restrictedAppRepository =
            RestrictedAppRepositoryImpl(
                database.restrictedAppDao()
            )

        // Lama - Initializes the usage session repository
        val usageSessionRepository =
            UsageSessionRepositoryImpl(
                database.usageSessionDao()
            )

        // Lama - Initializes the use case that saves application usage sessions
        saveUsageSessionsUseCase =
            SaveUsageSessionsUseCase(
                restrictedAppRepository = restrictedAppRepository,
                usageSessionRepository = usageSessionRepository
            )

        setContent {
            MinderTheme {
                UsageTestScreen()
            }
        }
    }

    @Composable
    fun UsageTestScreen() {

        // Lama - Stores the current Usage Access permission state
        val hasPermission = usagePermissionManager.hasUsageAccess()

        // Lama - Stores usage information displayed on the screen
        var usageResult by remember {
            mutableStateOf(
                "No usage data loaded yet"
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Minder - Usage Tracking"
                )

                Text(
                    text =
                        if (hasPermission) {
                            "Usage Access: Granted"
                        } else {
                            "Usage Access: Not Granted"
                        }
                )

                if (!hasPermission) {

                    // Lama - Opens Android settings to enable Usage Access
                    Button(
                        onClick = {
                            usagePermissionManager
                                .openUsageAccessSettings()
                        }
                    ) {
                        Text("Enable Usage Access")
                    }

                } else {

                    Button(
                        onClick = {

                            // Lama - Runs database operations in a coroutine
                            lifecycleScope.launch {

                                // Lama - Gets the existing local user
                                var user =
                                    database
                                        .userDao()
                                        .getUser()

                                // Lama - Creates a local user if one does not exist
                                if (user == null) {

                                    val newUserId =
                                        database
                                            .userDao()
                                            .insertUser(
                                                UserEntity(
                                                    createdAt =
                                                        System.currentTimeMillis()
                                                )
                                            )

                                    user =
                                        database
                                            .userDao()
                                            .getUserById(
                                                newUserId.toInt()
                                            )
                                }

                                // Lama - Reads today's application usage sessions
                                val sessions =
                                    usageStatsService
                                        .getTodayUsageSessions()

                                // Lama - Saves sessions for applications selected by the user
                                if (user != null) {

                                    saveUsageSessionsUseCase(
                                        userId = user.userId,
                                        sessions = sessions
                                    )
                                }

                                // Lama - Reads total application usage for display
                                val apps =
                                    usageStatsService
                                        .getTodayUsage()

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
                        }
                    ) {
                        Text("Read Today's Usage")
                    }
                }

                Text(
                    text = usageResult
                )
            }
        }
    }

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