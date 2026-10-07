package com.example.minder.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateHandler {

    private val _currentScreen =
        MutableStateFlow<AppScreen>(AppScreen.Onboarding)

    val currentScreen: StateFlow<AppScreen> =
        _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }
}