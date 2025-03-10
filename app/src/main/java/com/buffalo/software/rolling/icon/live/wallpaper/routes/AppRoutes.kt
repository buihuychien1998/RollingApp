package com.buffalo.software.rolling.icon.live.wallpaper.routes

sealed class AppRoutes(val route: String) {
    data object Splash : AppRoutes("Splash")
    data object Onboarding : AppRoutes("Onboarding")
    data object Feature : AppRoutes("Feature")
    data object Home : AppRoutes("Home")
    data object Language : AppRoutes("Language")
    data object Settings : AppRoutes("Settings")
    data object AppPicker : AppRoutes("AppPicker")
    data object ImagePicker : AppRoutes("ImagePicker")
    data object VideoPicker : AppRoutes("VideoPicker")
    data object BackgroundSelection : AppRoutes("BackgroundSelection")
    data object BackgroundDetail : AppRoutes("BackgroundDetail")
}