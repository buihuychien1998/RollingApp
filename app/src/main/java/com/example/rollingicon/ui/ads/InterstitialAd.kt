package com.example.rollingicon.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
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

    // Load Ad with Dynamic adUnitId
    fun loadAd(context: Context, adUnitId: String) {
        if (isAdLoading || (interstitialAd != null && currentAdUnitId == adUnitId) || !ConsentHelper.canRequestAds()) return // Avoid redundant loads

        isAdLoading = true
        currentAdUnitId = adUnitId

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isAdLoading = false
                Log.d("InterstitialAd", "Ad Loaded: $adUnitId")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                isAdLoading = false
                Log.e("InterstitialAd", "Ad Failed to Load: ${error.message}")
            }
        })
    }

    // Show Ad with Dynamic adUnitId
    fun showAd(activity: Activity, adUnitId: String, onAdClosed: () -> Unit) {
        if (interstitialAd != null && currentAdUnitId == adUnitId && ConsentHelper.canRequestAds()) {
            isAdShowing.value = true // Hide UI Before Ad

            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("InterstitialAd", "Ad Closed: $adUnitId")
                    interstitialAd = null
                    isAdShowing.value = false // Show UI After Ad
                    loadAd(activity, adUnitId) // Load next ad
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e("InterstitialAd", "Ad Failed to Show: ${adError.message}")
                    interstitialAd = null
                    isAdShowing.value = false // Show UI After Ad
                    loadAd(activity, adUnitId) // Reload even if failed
                    onAdClosed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAd", "Ad Not Ready, Proceeding Without Ad")
            onAdClosed()
            loadAd(activity, adUnitId) // Try to load for next time
        }
    }
}
