package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

object NativeAdManager {
    private var nativeAd: NativeAd? = null
    private var fallbackNativeAd: NativeAd? = null

    fun preloadNativeAd(
        context: Context,
        primaryAdUnitId: String,
        fallbackAdUnitId: String? = null, // ✅ Optional fallback ID
        onAdLoaded: (NativeAd?) -> Unit
    ) {
        if (!ConsentHelper.canRequestAds()) {
            onAdLoaded(null)
            return
        }

        val adLoader = AdLoader.Builder(context, primaryAdUnitId)
            .forNativeAd { ad ->
//                destroyAds()
                nativeAd = ad
                onAdLoaded(ad)
                Log.d("NativeAdManager", "✅ Primary Native Ad Loaded: $primaryAdUnitId")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("NativeAdManager", "❌ Primary Ad Failed: $error")
                    // Try loading fallback if available
                    if (fallbackAdUnitId != null) {
                        loadFallbackNativeAd(context, fallbackAdUnitId, onAdLoaded)
                    } else {
                        onAdLoaded(null)
                    }
                }

                override fun onAdClicked() {
                    Log.d("NativeAdManager", "🔥 Native Ad Clicked!")
                    AppOpenAdController.isAdClicked = true
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun loadFallbackNativeAd(context: Context, fallbackAdUnitId: String, onAdLoaded: (NativeAd?) -> Unit) {
        val fallbackLoader = AdLoader.Builder(context, fallbackAdUnitId)
            .forNativeAd { ad ->
                fallbackNativeAd = ad
                onAdLoaded(ad)
                Log.d("NativeAdManager", "✅ Fallback Native Ad Loaded: $fallbackAdUnitId")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("NativeAdManager", "❌ Fallback Ad Failed: ${error.message}")
                    onAdLoaded(null)
                }
            })
            .build()

        fallbackLoader.loadAd(AdRequest.Builder().build())
    }

    fun getPreloadedAd(): NativeAd? = nativeAd ?: fallbackNativeAd

    fun reloadNativeAd(
        context: Context,
        primaryAdUnitId: String,
        fallbackAdUnitId: String? = null, // ✅ Optional fallback
        onAdReloaded: (NativeAd?) -> Unit
    ) {
//        destroyAds() // ✅ Ensure old ads are removed before reloading
        preloadNativeAd(context, primaryAdUnitId, fallbackAdUnitId, onAdReloaded)
    }

    fun destroyAds() {
        nativeAd?.destroy()
        fallbackNativeAd?.destroy()
        nativeAd = null
        fallbackNativeAd = null
        Log.d("NativeAdManager", "🗑️ Ads Destroyed")
    }
}
