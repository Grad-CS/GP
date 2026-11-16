package com.example.minder.features.blocking

import androidx.compose.runtime.Composable
import com.example.minder.domain.usecase.usage.CheckDailyLimitUseCase

/**
 * Lama - Compatibility wrapper: keep the existing BlockingScreen call contract.
 * Daily limit eligibility is already checked by the monitor before opening
 * BlockingActivity. The activity separately checks today's extension record.
 */
@Composable
fun BlockingScreen(
    appId: Int,
    interventionId: Int,
    initialRemainingSeconds: Long,
    checkDailyLimitUseCase: CheckDailyLimitUseCase,
    extensionAlreadyUsedToday: Boolean?,
    onRequestExtension: (Int) -> Unit,
    onExitApp: () -> Unit,
    onTimeFinished: () -> Unit,
    isRequestingExtension: Boolean = false,
    extensionError: String? = null
) {
    DailyLimitReachedScreen(
        onExitApp = onExitApp,
        onRequestExtension = onRequestExtension,
        extensionAvailable = extensionAlreadyUsedToday == false,
        extensionAvailabilityLoading = extensionAlreadyUsedToday == null,
        isProcessing = isRequestingExtension,
        errorMessage = extensionError
    )
}
