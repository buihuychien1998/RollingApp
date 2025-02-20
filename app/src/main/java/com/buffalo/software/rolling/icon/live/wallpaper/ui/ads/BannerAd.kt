package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAd(
    bannerId: String,
    showAppOpenResume: Boolean = true,
    onAdFinished: (() -> Unit)? = null
) { // Make it optional
    val context = LocalContext.current
    var adWidth by remember { mutableIntStateOf(0) }
    var isAdLoading by remember { mutableStateOf(true) }
    var isAdFailed by remember { mutableStateOf(false) }
    var isAdLoaded by remember { mutableStateOf(false) }
    var adView by remember { mutableStateOf<AdView?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("BannerAd", "🔥 App Resumed -> Reloading Ad")
                if (!AppOpenAdController.shouldShowAd) {
                    AppOpenAdController.shouldShowAd = showAppOpenResume
                    isAdLoading = true   // ✅ Reset loading state
                    isAdLoaded = false   // ✅ Ensure reloading works
                    isAdFailed = false
                    adView?.destroy()
                    adView?.loadAd(AdRequest.Builder().build())
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView?.destroy()
        }
    }



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                adWidth = layoutCoordinates.size.width // Get dynamic width
            }
    ) {
        // Show shimmer while loading
        if (isAdLoading) {
            BannerShimmerEffect()
        }

        if (!isAdFailed && adWidth > 0) {
            AndroidView(
                factory = { ctx ->
                    val displayMetrics = ctx.resources.displayMetrics
                    val availableWidthInDp = (adWidth / displayMetrics.density).toInt()

                    val adaptiveAdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        ctx, availableWidthInDp
                    )
                    AdView(context).apply {
                        setAdSize(adaptiveAdSize)
                        adUnitId = bannerId  // Replace with your AdMob banner ID

                        this.adListener = object : AdListener() {
                            override fun onAdClicked() {
                                Log.d("BannerAd", "Ad clicked")
                                AppOpenAdController.shouldShowAd = false
                            }

                            override fun onAdClosed() {
                                Log.d("BannerAd", "Ad closed")
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                isAdLoading = false
                                isAdFailed = true // Hide ad on failure
                                Log.e("BannerAd", "Ad failed to load: ${adError.message}")
                            }

                            override fun onAdImpression() {
                                Log.d("BannerAd", "Ad impression recorded")
                            }

                            override fun onAdLoaded() {
                                isAdLoading = false
                                isAdLoaded = true // Ad successfully loaded
                                Log.d("BannerAd", "Ad loaded successfully")
                            }

                            override fun onAdOpened() {
                                Log.d("BannerAd", "Ad opened")
                            }
                        }
                        adView = this
                        adView?.loadAd(AdRequest.Builder().build())
                    }
                }, update = { adView = it }, modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ✅ Automatically trigger navigation when ad is loaded or failed
    LaunchedEffect(isAdLoaded, isAdFailed) {
        if ((isAdLoaded || isAdFailed) && onAdFinished != null) {
            onAdFinished() // Navigate to the next screen
        }
    }
}


