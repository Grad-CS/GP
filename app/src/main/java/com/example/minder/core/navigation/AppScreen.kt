package com.example.minder.core.navigation

sealed class AppScreen {

    data object Onboarding : AppScreen()

    data object Questionnaire : AppScreen()

    data object Home : AppScreen()

    data object AppSelection : AppScreen()

    data object Permissions : AppScreen()

    data object Setup : AppScreen()

    data object Statistics : AppScreen()

    data object Settings : AppScreen()
}