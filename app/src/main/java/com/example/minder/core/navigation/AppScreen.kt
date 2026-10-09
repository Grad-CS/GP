package com.example.minder.core.navigation

sealed class AppScreen {

    data object Splash : AppScreen()

    data object Welcome : AppScreen()

    data object Onboarding : AppScreen()

    data object Questionnaire : AppScreen()

    data object Home : AppScreen()

    data object AppSelection : AppScreen()

    object UsageLimit : AppScreen()       // 3/6

    object ChallengeSetup : AppScreen()   // 4/6

    data object Permissions : AppScreen()

    data object Statistics : AppScreen()

    data object Settings : AppScreen()
}