package com.buffalo.software.rolling.icon.live.wallpaper.ui.home

import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.buffalo.software.rolling.icon.live.wallpaper.routes.AppRoutes
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.AppOpenAdController
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.AppOpenAdManager
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.ConsentHelper
import com.buffalo.software.rolling.icon.live.wallpaper.ui.app_picker.AppPickerScreen
import com.buffalo.software.rolling.icon.live.wallpaper.ui.image_picker.ImagePickerScreen
import com.buffalo.software.rolling.icon.live.wallpaper.ui.language.LanguageScreen
import com.buffalo.software.rolling.icon.live.wallpaper.ui.onboarding.OnboardingScreen
import com.buffalo.software.rolling.icon.live.wallpaper.ui.settings.SettingsScreen
import com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model.SharedViewModel
import com.buffalo.software.rolling.icon.live.wallpaper.ui.splash.SplashScreen
import com.buffalo.software.rolling.icon.live.wallpaper.ui.video_picker.VideoPickerScreen
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper.isOnboardingDone
import com.buffalo.software.rolling.icon.live.wallpaper.utils.TWEEN_DURATION
import com.buffalo.software.rolling.icon.live.wallpaper.utils.languages
import java.util.Locale


class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize ConsentHelper
        ConsentHelper.initializeConsent(this) { isConsentGiven ->
            if (isConsentGiven) {
                // Load Ads (Only after user consent)
                AppOpenAdManager(application)
            }
        }

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
//                    EnterTransition.None
                    fadeIn(animationSpec = tween(100))
                },
                exitTransition = {
//                    ExitTransition.None
                    fadeOut(animationSpec = tween(100))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(TWEEN_DURATION)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(TWEEN_DURATION)
                    )
                }
            ) {
                composable(
                    route = AppRoutes.Splash.route,
                    exitTransition = {
                        fadeOut(animationSpec = tween(100))
                    }
                ) {
                    AppOpenAdController.shouldShowAd = false
                    SplashScreen(navController)
                }

                composable(AppRoutes.Onboarding.route) {
                    AppOpenAdController.shouldShowAd = true
                    OnboardingScreen(navController)
                }
                composable(AppRoutes.Home.route) {
                    AppOpenAdController.shouldShowAd = true
                    HomeScreen(navController, sharedViewModel)
                }
                composable(AppRoutes.Language.route) {
                    AppOpenAdController.shouldShowAd = true
                    val fromSetting = remember {
                        PreferencesHelper.fromSetting(this@HomeActivity)
                    }

                    // Get the current language from shared preferences
                    val currentLanguageCode =
                        PreferencesHelper.getSelectedLanguage(this@HomeActivity)
                    // Set the selected language based on saved preference or default to the first one
                    val selectedLanguage = remember {
                        mutableStateOf(
                            languages.firstOrNull { it.code == currentLanguageCode }
                                ?: languages.first()
                        )
                    }
                    // Pass 'showBackButton' based on the current navigation stack
                    LanguageScreen(
                        languages = languages,
                        selectedLanguage = selectedLanguage.value,
                        onLanguageSelected = { selectedLanguage.value = it },
                        onConfirm = {
                            // Handle confirm action, e.g., save the selected language to SharedPreferences
                            PreferencesHelper.saveSelectedLanguage(
                                this@HomeActivity,
                                selectedLanguage.value
                            )
                            PreferencesHelper.setLFODone(this@HomeActivity, true)
                            // Update app language
                            changeLanguage(selectedLanguage.value.code)

                            // Navigate to Home if it's the first open, or pop back if opened from settings
                            if (fromSetting) {
                                navController.popBackStack()
                            } else {
                                PreferencesHelper.increaseLaunchCount(this@HomeActivity)
                                navController.navigate(if(isOnboardingDone(this@HomeActivity)) AppRoutes.Home.route else AppRoutes.Onboarding.route) {
                                    popUpTo(AppRoutes.Language.route) { inclusive = true }
                                }
                            }
                        },
                        showBackButton = fromSetting,
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


