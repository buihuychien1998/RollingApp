package com.buffalo.software.rolling.icon.live.wallpaper.ui.splash

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.buffalo.software.rolling.icon.live.wallpaper.R
import com.buffalo.software.rolling.icon.live.wallpaper.routes.AppRoutes
import com.buffalo.software.rolling.icon.live.wallpaper.theme.AppFont
import com.buffalo.software.rolling.icon.live.wallpaper.theme.clr_96ACC4
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.AppOpenAdController
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.BannerAd
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.InterstitialAdManager
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.banner_splash
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.inter_splash
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.inter_splash_high
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.tracking.FirebaseAnalyticsEvents
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.tracking.FirebaseEventLogger
import com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model.RemoteConfigKeys
import com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model.RemoteConfigViewModel
import com.buffalo.software.rolling.icon.live.wallpaper.utils.LAUNCH_COUNT
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper
import com.buffalo.software.rolling.icon.live.wallpaper.utils.SHOW_AD
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, remoteConfigViewModel: RemoteConfigViewModel = viewModel()) {
    val scale = remember { Animatable(1f) }
    val progress = remember { Animatable(0f) }

    val context = LocalContext.current
    var isAdFinished by remember { mutableStateOf(false) }
    var isConfigLoaded by remember { mutableStateOf(false) }
    var isResumed by remember { mutableStateOf(true) } // Track if app is in foreground

    val lifecycleOwner = LocalLifecycleOwner.current
    val configValues by remoteConfigViewModel.configValues.collectAsState()
    var showAd by remember { mutableStateOf(true) } // ✅ Controls ad visibility

    LaunchedEffect(Unit){
        AppOpenAdController.shouldShowAd = false
    }

    // ✅ Track if Remote Config is loaded
    LaunchedEffect(configValues) {
        if (configValues.isNotEmpty()) {
            isConfigLoaded = true
            showAd = configValues[RemoteConfigKeys.BANNER_SPLASH] == true
        }
    }

    // ✅ Observe lifecycle changes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_PAUSE -> isResumed = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ✅ Fetch Remote Config Before Showing Ads
    LaunchedEffect(isConfigLoaded) {
        if (isConfigLoaded) {
            FirebaseEventLogger.trackScreenView(context, FirebaseAnalyticsEvents.SCREEN_SPLASH_VIEW)

            if (SHOW_AD) {
                when {
                    configValues[RemoteConfigKeys.INTER_SPLASH_HIGH] == true &&
                            configValues[RemoteConfigKeys.INTER_SPLASH] == true -> {
                        InterstitialAdManager.loadAds(context, inter_splash_high, inter_splash)
                    }

                    configValues[RemoteConfigKeys.INTER_SPLASH_HIGH] == true -> {
                        InterstitialAdManager.loadAd(context, inter_splash_high)
                    }

                    configValues[RemoteConfigKeys.INTER_SPLASH] == true -> {
                        InterstitialAdManager.loadAd(context, inter_splash)
                    }

                    else -> {
                        delay(2000) // Wait for splash time
                        openNextScreen(context, navController)
                    }
                }
            } else {
                delay(2000) // No ads, go to next screen
                openNextScreen(context, navController)
            }
        }
    }

    // ✅ Handle Ad Completion & Navigation
    LaunchedEffect(isAdFinished, showAd, isResumed, isConfigLoaded) {
        println("isAdFinished $isAdFinished")
        println("isResumed $isResumed")
        println("isConfigLoaded $isConfigLoaded")
        if (isConfigLoaded && (isAdFinished || !showAd) && isResumed) {
            delay(2000)
            val activity = context as? Activity

            activity?.let {
                when {
                    configValues[RemoteConfigKeys.INTER_SPLASH_HIGH] == true &&
                            configValues[RemoteConfigKeys.INTER_SPLASH] == true -> {
                        InterstitialAdManager.showAdIfAvailable(activity) {
                            openNextScreen(context, navController)
                        }
                    }

                    configValues[RemoteConfigKeys.INTER_SPLASH_HIGH] == true -> {
                        InterstitialAdManager.showAd(activity, inter_splash_high) {
                            openNextScreen(context, navController)
                        }
                    }

                    configValues[RemoteConfigKeys.INTER_SPLASH] == true -> {
                        InterstitialAdManager.showAd(activity, inter_splash) {
                            openNextScreen(context, navController)
                        }
                    }

                    else -> {
                        delay(2000)
                        openNextScreen(context, navController)
                    }
                }
            }
        }
    }

    // ✅ UI Layout
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.bg_rolling_app),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.safeDrawingPadding()
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_splash),
                contentDescription = "Logo",
                modifier = Modifier
                    .scale(scale.value)
                    .size(240.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.text_rolling_icon),
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AppFont.Grandstander
            )
            Spacer(modifier = Modifier.height(48.dp))
        }

        Column(
            Modifier
                .safeDrawingPadding()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.ad_warning),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .safeDrawingPadding()
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 16.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color.White,
                trackColor = clr_96ACC4
            )

            if (SHOW_AD && showAd) {
                BannerAd(banner_splash, false) {
                    isAdFinished = true
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun openNextScreen(
    context: Context,
    navController: NavController
) {
    val launchCount = PreferencesHelper.getLaunchCount(context)
    val isOnboardingDone = PreferencesHelper.isOnboardingDone(context)
    PreferencesHelper.setLFODone(context = context, false)
    println("SplashScreen: launchCount $launchCount")
    val nextRoute = when {
        launchCount < LAUNCH_COUNT -> AppRoutes.Language.route
        !isOnboardingDone -> AppRoutes.Onboarding.route
        else -> AppRoutes.Home.route
    }
    AppOpenAdController.shouldShowAd = true

    navController.navigate(nextRoute) {
        popUpTo(AppRoutes.Splash.route) { inclusive = true }
    }
}
