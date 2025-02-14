package com.example.rollingicon.ui.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

object NativeAdManager {
    private var nativeAd: NativeAd? = null

    fun preloadNativeAd(context: Context, adUnitId: String, onAdLoaded: (NativeAd?) -> Unit) {
        if (!ConsentHelper.canRequestAds()) {
            onAdLoaded(null)
            return
        }

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                destroyAd()
                nativeAd = ad
                onAdLoaded(ad)
                Log.d("NativeAdManager", "Native Ad Preloaded")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("NativeAdManager", "Failed to load native ad: ${error.message}")
                    onAdLoaded(null)

                }

                override fun onAdClicked() {
                    Log.d("NativeAdManager", "🔥 Native Ad Clicked!") // 🚀 Log Click Event Here
                    AppOpenAdController.shouldShowAd = false
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun getPreloadedAd(): NativeAd? = nativeAd

    fun reloadNativeAd(context: Context, adUnitId: String, onAdReloaded: (NativeAd?) -> Unit) {
        destroyAd() // ✅ Ensure old ad is removed before reloading
        preloadNativeAd(context, adUnitId, onAdReloaded)
    }

    fun destroyAd() {
        nativeAd?.destroy()
        nativeAd = null
        Log.d("NativeAdManager", "Native Ad Destroyed")
    }
}