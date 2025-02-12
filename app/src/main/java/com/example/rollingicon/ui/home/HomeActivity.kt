package com.example.rollingicon.ui.home

import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rollingicon.routes.AppRoutes
import com.example.rollingicon.ui.app_picker.AppPickerScreen
import com.example.rollingicon.ui.image_picker.ImagePickerScreen
import com.example.rollingicon.ui.language.LanguageScreen
import com.example.rollingicon.ui.onboarding.OnboardingScreen
import com.example.rollingicon.ui.settings.SettingsScreen
import com.example.rollingicon.ui.share_view_model.SharedViewModel
import com.example.rollingicon.ui.splash.SplashScreen
import com.example.rollingicon.ui.video_picker.VideoPickerScreen
import com.example.rollingicon.utils.PreferencesHelper
import com.example.rollingicon.utils.TWEEN_DURATION
import com.example.rollingicon.utils.languages
import java.util.Locale


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set the Compose content for this activity
        setContent {
            val navController = rememberNavController()
            val sharedViewModel: SharedViewModel =
                viewModel(LocalContext.current as ComponentActivity)


            // Check the saved language preference or fallback to default
            val currentLanguage = PreferencesHelper.getSelectedLanguage(this)
            changeLanguage(currentLanguage)

            NavHost(navController = navController,
                startDestination = AppRoutes.Splash.route,
                enterTransition = {
                    EnterTransition.None
                },
                exitTransition = {
                    ExitTransition.None
                },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TWEEN_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TWEEN_DURATION)) }
                ) {
                composable(
                    route = AppRoutes.Splash.route,
                    exitTransition = {
                        fadeOut(animationSpec = tween(100))
                    }
                ) { SplashScreen(navController) }

                composable(AppRoutes.Onboarding.route) {
                    OnboardingScreen(navController)
                }
                composable(AppRoutes.Home.route) {
                    HomeScreen(navController, sharedViewModel)
                }
                composable(AppRoutes.Language.route) {
                    val isLFO = remember {
                        PreferencesHelper.isLFODone(this@HomeActivity)
                    }

                    // Get the current language from shared preferences
                    val currentLanguageCode = PreferencesHelper.getSelectedLanguage(this@HomeActivity)
                    // Set the selected language based on saved preference or default to the first one
                    val selectedLanguage = remember {
                        mutableStateOf(
                            languages.firstOrNull { it.code == currentLanguageCode } ?: languages.first()
                        )
                    }
                    // Pass 'showBackButton' based on the current navigation stack
                    LanguageScreen(
                        languages = languages,
                        selectedLanguage = selectedLanguage.value,
                        onLanguageSelected = { selectedLanguage.value = it },
                        onConfirm = {
                            // Handle confirm action, e.g., save the selected language to SharedPreferences
                            PreferencesHelper.saveSelectedLanguage(this@HomeActivity, selectedLanguage.value)
                            PreferencesHelper.setLFODone(this@HomeActivity, true)

                            // Update app language
                            changeLanguage(selectedLanguage.value.code)

                            // Navigate to Home if it's the first open, or pop back if opened from settings
                            if (isLFO) {
                                // If coming from settings, pop the current screen
                                navController.popBackStack()
                            } else {
                                // If it's the first time, navigate to Home screen
                                navController.navigate(AppRoutes.Onboarding.route) {
                                    popUpTo(AppRoutes.Language.route) { inclusive = true }
                                }
                            }
                        },
                        showBackButton = isLFO,
                        // Show back button if we are navigating from another screen
                        onBackPressed = {
                            // Handle back press action
                            navController.popBackStack()
                        }
                    )
                }
                composable(AppRoutes.Settings.route) {
                    SettingsScreen(navController)
                }
                composable(AppRoutes.AppPicker.route) {
                    AppPickerScreen(navController)
                }
                composable(AppRoutes.ImagePicker.route) {
                    ImagePickerScreen(navController, sharedViewModel = sharedViewModel)
                }
                composable(AppRoutes.VideoPicker.route) {
                    VideoPickerScreen(navController, sharedViewModel = sharedViewModel)
                }
            }
        }
    }

    fun changeLanguage(language: String) {
        // Get the current system language
        val currentLanguage = Locale.getDefault().language

        // Only change language if it's different
        if (language != currentLanguage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13 (Tiramisu) and above
                val localeManager = getSystemService(LocaleManager::class.java)
                localeManager?.applicationLocales = LocaleList.forLanguageTags(language)
            } else {
                // For lower versions of Android
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
            }
        }
    }

}


