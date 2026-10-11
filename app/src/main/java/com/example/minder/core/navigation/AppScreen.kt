package com.example.minder.core.navigation

sealed class AppScreen {

    data object Splash : AppScreen()

    data object Welcome : AppScreen()

    data object Onboarding : AppScreen()

    data object Questionnaire : AppScreen()

    data object Home : AppScreen()

    data object AppSelection : AppScreen()

    object UsageLimit : AppScreen()

    object ChallengeSetup : AppScreen()

    data object Permissions : AppScreen()

    data object Statistics : AppScreen()

    data object Settings : AppScreen()
}