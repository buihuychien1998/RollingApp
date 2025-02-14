package com.example.rollingicon.ui.splash

import android.app.Activity
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
import androidx.navigation.NavController
import com.example.rollingicon.R
import com.example.rollingicon.routes.AppRoutes
import com.example.rollingicon.theme.AppFont
import com.example.rollingicon.theme.clr_96ACC4
import com.example.rollingicon.ui.ads.BannerAd
import com.example.rollingicon.ui.ads.InterstitialAdManager
import com.example.rollingicon.ui.ads.banner_splash
import com.example.rollingicon.ui.ads.inter_splash
import com.example.rollingicon.ui.ads.inter_splash_high
import com.example.rollingicon.utils.PreferencesHelper
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(1f) }
    val progress = remember { Animatable(0f) }

    val context = LocalContext.current
    var isAdFinished by remember { mutableStateOf(false) }
    var isResumed by remember { mutableStateOf(true) } // Track if app is in foreground
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle changes
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

    LaunchedEffect(Unit) {
        InterstitialAdManager.loadAds(context, inter_splash_high, inter_splash)
    }

    // Start delay only when the ad finishes loading and app is in foreground
    LaunchedEffect(isAdFinished, isResumed) {
        if (isAdFinished && isResumed) {
            delay(2000) // Wait 2000ms after ad loads
            val activity = context as? Activity

            if (activity != null) {
                InterstitialAdManager.showAdIfAvailable(activity) {
                    val isLanguageDone = PreferencesHelper.isLFODone(context)
                    val isOnboardingDone = PreferencesHelper.isOnboardingDone(context)

                    val nextRoute = when {
                        !isLanguageDone -> AppRoutes.Language.route
                        !isOnboardingDone -> AppRoutes.Onboarding.route
                        else -> AppRoutes.Home.route
                    }

                    navController.navigate(nextRoute) {
                        popUpTo(AppRoutes.Splash.route) { inclusive = true }
                    }
                }
            }
        }
    }
    // Animate the logo popping in
//    LaunchedEffect(Unit) {
////        launch {
////            scale.animateTo(
////                targetValue = 1f,
////                animationSpec = tween(durationMillis = 1000, easing = EaseOutBounce)
////            )
////        }
//        launch {
//            progress.animateTo(
//                targetValue = 1f,
//                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
//            )
//        }
//        delay(2000)
//        val isLFO = PreferencesHelper.isLFO(context)
//        navController.navigate(if (isLFO) AppRoutes.Language.route else AppRoutes.Home.route) {
//            popUpTo(
//                AppRoutes.Splash.route
//            ) { inclusive = true }
//        }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize(), // Background color
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.bg_rolling_app),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .safeDrawingPadding()
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_splash), // Replace with your logo
                contentDescription = "Logo",
                modifier = Modifier
                    .scale(scale.value)
                    .size(240.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.app_name),
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
//                progress = progress.value,
                modifier = Modifier
                    .safeDrawingPadding()
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 16.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color.White,
                trackColor = clr_96ACC4
            )
            BannerAd(banner_splash, false){
                isAdFinished = true
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }


}
