package com.example.rollingicon.routes

sealed class AppRoutes(val route: String) {
    data object Home : AppRoutes("home")
    data object Language : AppRoutes("language")
    data object Settings : AppRoutes("settings")
    data object AppPicker : AppRoutes("appPicker")
    data object ImagePicker : AppRoutes("imagePicker")
    data object VideoPicker : AppRoutes("videoPicker")
}