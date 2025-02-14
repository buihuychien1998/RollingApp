package com.example.rollingicon.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var currentAdUnitId: String? = null
    var isAdShowing = mutableStateOf(false) // Track Ad Visibility

    private var primaryAd: InterstitialAd? = null
    private var fallbackAd: InterstitialAd? = null
    private var isPrimaryAdLoaded = false
    private var isFallbackAdLoaded = false

    // 🚀 Load Single Ad (Original)
    fun loadAd(context: Context, adUnitId: String) {
        if (isAdLoading || (interstitialAd != null && currentAdUnitId == adUnitId) || !ConsentHelper.canRequestAds()) return

        isAdLoading = true
        currentAdUnitId = adUnitId

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isAdLoading = false
                Log.d("InterstitialAd", "✅ Ad Loaded: $adUnitId")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                isAdLoading = false
                Log.e("InterstitialAd", "❌ Ad Failed to Load: ${error.message}")
            }
        })
    }

    // 🚀 Load Two Ads (New)
    fun loadAds(context: Context, primaryAdUnitId: String, fallbackAdUnitId: String) {
        if (isAdLoading || !ConsentHelper.canRequestAds()) return
        isAdLoading = true

        // Load Primary Ad
        InterstitialAd.load(context, primaryAdUnitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                primaryAd = ad
                isPrimaryAdLoaded = true
                isAdLoading = false
                Log.d("InterstitialAd", "✅ Primary Ad Loaded: $primaryAdUnitId")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                isPrimaryAdLoaded = false
                Log.e("InterstitialAd", "❌ Primary Ad Failed: ${error.message}")

                // Load Fallback Ad if Primary Ad Fails
                InterstitialAd.load(context, fallbackAdUnitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        fallbackAd = ad
                        isFallbackAdLoaded = true
                        isAdLoading = false
                        Log.d("InterstitialAd", "✅ Fallback Ad Loaded: $fallbackAdUnitId")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isFallbackAdLoaded = false
                        isAdLoading = false
                        Log.e("InterstitialAd", "❌ Fallback Ad Failed: ${error.message}")
                    }
                })
            }
        })
    }

    // 🚀 Show Ad with Single ID (Original)
    fun showAd(activity: Activity, adUnitId: String, onAdClosed: () -> Unit) {
        if (interstitialAd != null && currentAdUnitId == adUnitId && ConsentHelper.canRequestAds()) {
            isAdShowing.value = true // Hide UI Before Ad

            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("InterstitialAd", "✅ Ad Closed: $adUnitId")
                    interstitialAd = null
                    isAdShowing.value = false // Show UI After Ad
                    loadAd(activity, adUnitId) // Load next ad
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e("InterstitialAd", "❌ Ad Failed to Show: ${adError.message}")
                    interstitialAd = null
                    isAdShowing.value = false // Show UI After Ad
                    loadAd(activity, adUnitId) // Reload even if failed
                    onAdClosed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAd", "🚀 Ad Not Ready, Proceeding Without Ad")
            onAdClosed()
            loadAd(activity, adUnitId) // Try to load for next time
        }
    }

    // 🚀 Show Preloaded Ads (New)
    fun showAdIfAvailable(activity: Activity, onAdClosed: () -> Unit) {
        when {
            primaryAd != null -> {
                showPreloadedAd(activity, primaryAd!!, onAdClosed)
                primaryAd = null
            }
            fallbackAd != null -> {
                showPreloadedAd(activity, fallbackAd!!, onAdClosed)
                fallbackAd = null
            }
            else -> {
                Log.d("InterstitialAd", "🚀 No Ads Available, Proceeding Without Ad")
                onAdClosed()
            }
        }
    }

    // 🚀 Helper Function to Show Preloaded Ad
    private fun showPreloadedAd(activity: Activity, ad: InterstitialAd, onAdClosed: () -> Unit) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("InterstitialAd", "✅ Ad Closed")
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e("InterstitialAd", "❌ Failed to Show Ad: ${adError.message}")
                onAdClosed()
            }
        }
        ad.show(activity)
    }
}

