package com.example.rollingicon.ui.ads

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAd(bannerId: String, onAdFinished: (() -> Unit)? = null) { // Make it optional
    val context = LocalContext.current
    var adWidth by remember { mutableIntStateOf(0) }
    var isAdLoading by remember { mutableStateOf(true) }
    var isAdFailed by remember { mutableStateOf(false) }
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                adWidth = layoutCoordinates.size.width // Get dynamic width
            }
    ) {
        // Show shimmer while loading
        if (isAdLoading) {
            ShimmerEffect()
        }

        if (!isAdFailed) {
            if (adWidth > 0) { // Ensure width is available before creating AdView
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
                            loadAd(AdRequest.Builder().build())

                            this.adListener = object : AdListener() {
                                override fun onAdClicked() {
                                    Log.d("BannerAd", "Ad clicked")
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
                        }
                    }, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Automatically trigger navigation when ad is loaded or failed (if onAdFinished is provided)
    LaunchedEffect(isAdLoaded, isAdFailed) {
        if ((isAdLoaded || isAdFailed) && onAdFinished != null) {
            onAdFinished() // Navigate to the next screen
        }
    }
}
