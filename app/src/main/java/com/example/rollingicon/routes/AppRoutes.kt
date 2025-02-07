package com.example.rollingicon.routes

sealed class AppRoutes(val route: String) {
    data object Splash : AppRoutes("Splash")
    data object Onboarding : AppRoutes("Onboarding")
    data object Home : AppRoutes("Home")
    data object Language : AppRoutes("Language")
    data object Settings : AppRoutes("Settings")
    data object AppPicker : AppRoutes("AppPicker")
    data object ImagePicker : AppRoutes("ImagePicker")
    data object VideoPicker : AppRoutes("VideoPicker")
}