package com.example.minder.features.blocking

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.repository.DailyUsageRepositoryImpl
import com.example.minder.data.repository.ExtensionRequestRepositoryImpl
import com.example.minder.data.repository.RestrictedAppRepositoryImpl
import com.example.minder.domain.usecase.usage.CheckDailyLimitUseCase
import com.example.minder.domain.usecase.usage.RequestDailyExtensionUseCase
import com.example.minder.services.monitoring.AppAccessibilityService
import com.example.minder.ui.theme.MinderTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Calendar

class BlockingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_APP_ID = "app_id"
        const val EXTRA_INTERVENTION_ID = "intervention_id"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appId = intent.getIntExtra(EXTRA_APP_ID, -1)
        if (appId == -1) {
            Log.e("MinderBlocking", "Invalid appId")
            finish()
            return
        }

        val database = MinderDatabase.getDatabase(applicationContext)
        val restrictedAppRepository = RestrictedAppRepositoryImpl(database.restrictedAppDao())
        val dailyUsageRepository = DailyUsageRepositoryImpl(database.dailyUsageDao())
        val checkDailyLimitUseCase = CheckDailyLimitUseCase(
            restrictedAppRepository = restrictedAppRepository,
            dailyUsageRepository = dailyUsageRepository
        )
        val extensionRepository = ExtensionRequestRepositoryImpl(database.extensionRequestDao())
        val requestDailyExtensionUseCase = RequestDailyExtensionUseCase(
            extensionRequestRepository = extensionRepository
        )

        val extensionAlreadyUsedToday = mutableStateOf<Boolean?>(null)
        val isRequestingExtension = mutableStateOf(false)
        val extensionError = mutableStateOf<String?>(null)

        lifecycleScope.launch {
            try {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val dayStart = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val nextDayStart = calendar.timeInMillis
                extensionAlreadyUsedToday.value = extensionRepository.getRequestForAppOnDate(
                    appId = appId,
                    dayStart = dayStart,
                    nextDayStart = nextDayStart
                ) != null
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e("MinderBlocking", "Failed to check extension availability", exception)
                extensionError.value = "Unable to check extension availability. Please try again later."
                // Remain unavailable when eligibility cannot be verified.
                extensionAlreadyUsedToday.value = true
            }
        }

        setContent {
            MinderTheme {
                BlockingScreen(
                    appId = appId,
                    interventionId = -1,
                    initialRemainingSeconds = 0L,
                    checkDailyLimitUseCase = checkDailyLimitUseCase,
                    extensionAlreadyUsedToday = extensionAlreadyUsedToday.value,
                    isRequestingExtension = isRequestingExtension.value,
                    extensionError = extensionError.value,
                    onRequestExtension = { selectedMinutes ->
                        if (!isRequestingExtension.value && extensionAlreadyUsedToday.value == false) {
                            isRequestingExtension.value = true
                            extensionError.value = null
                            lifecycleScope.launch {
                                try {
                                    val extensionId = requestDailyExtensionUseCase(
                                        appId = appId,
                                        selectedMinutes = selectedMinutes
                                    )
                                    if (extensionId != null) {
                                        // Keep the existing completion behaviour. The monitor
                                        // must independently recognise the saved extension.
                                        finish()
                                    } else {
                                        extensionAlreadyUsedToday.value = true
                                        extensionError.value = "An extension could not be granted."
                                    }
                                } catch (exception: CancellationException) {
                                    throw exception
                                } catch (exception: Exception) {
                                    Log.e("MinderBlocking", "Failed to request daily extension", exception)
                                    extensionError.value = "Unable to extend time. Please try again."
                                } finally {
                                    isRequestingExtension.value = false
                                }
                            }
                        }
                    },
                    onExitApp = {
                        AppAccessibilityService.requestExitToHome()
                        finish()
                    },
                    onTimeFinished = { /* Daily-limit screen has no countdown. */ }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        AppAccessibilityService.notifyBlockingClosed()
    }
}
