package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoadingAd = false

    fun loadRewardedAd(context: Context, adUnitId: String, onAdLoaded: (() -> Unit)? = null) {
        if (isLoadingAd || rewardedAd != null || !ConsentHelper.canRequestAds()) return

        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoadingAd = false
                onAdLoaded?.invoke()
                Log.d("RewardAdManager", "Rewarded Ad Loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                isLoadingAd = false
                Log.e("RewardAdManager", "Failed to Load Rewarded Ad: ${error.message}")
            }
        })
    }

    fun showRewardedAd(activity: Activity, onUserEarnedReward: (RewardItem) -> Unit, onAdDismissed: (() -> Unit)? = null) {
        if (rewardedAd == null) {
            Log.e("RewardAdManager", "Rewarded Ad not available")
            onAdDismissed?.invoke()
            return
        }

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd(activity, "your_rewarded_ad_unit_id") // Reload ad after showing
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                Log.e("RewardAdManager", "Failed to Show Rewarded Ad: ${adError.message}")
                onAdDismissed?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("RewardAdManager", "Rewarded Ad Shown")
            }
        }

        rewardedAd?.show(activity) { rewardItem ->
            onUserEarnedReward(rewardItem)
        }
    }
}