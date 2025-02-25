package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.buffalo.software.rolling.icon.live.wallpaper.utils.SHOW_AD
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(private val application: Application) :
    Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var lastAdShownTime: Long = 0 // 🕒 Thời điểm hiển thị quảng cáo gần nhất
    private val adCooldownMillis = 25_000L // ⏳ Giãn cách 25 giây
//    private val adCooldownMillis = 0L // ⏳ Giãn cách 25 giây
    private var previousActivityName: String? = null  // 🔥 Store last opened activity name

    init {
        application.registerActivityLifecycleCallbacks(this)
        loadAd()
    }

    private fun loadAd() {
        if (!SHOW_AD || !AppOpenAdController.enableConfig || !ConsentHelper.canRequestAds()) {
            Log.d("AppOpenAdManager", "🚫 Ad Loading Skipped - Conditions not met")
            return
        }

        Log.d("AppOpenAdManager", "🔄 Loading new Ad...")
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(application, appopen_resume, adRequest, object :
            AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                Log.d("AppOpenAdManager", "✅ Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e("AppOpenAdManager", "❌ Ad Failed to Load: ${error.message}")
                appOpenAd = null
            }
        })
    }


    fun showAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit) {
        val currentTime = System.currentTimeMillis()

        // Prevent app open ad if interstitial ad was shown recently
        if (currentTime - InterstitialAdManager.lastInterstitialTime < InterstitialAdManager.interstitialCooldownMillis) {
            Log.d("AppOpenAdManager", "🚫 Skipping App Open Ad due to recent interstitial ad")
            onAdDismissed()
            return
        }

        if (!SHOW_AD || currentTime - lastAdShownTime < adCooldownMillis) {
            onAdDismissed()
            return
        }

        if (InterstitialAdManager.isAdShowing.value || !AppOpenAdController.enableConfig || !AppOpenAdController.shouldShowAd || AppOpenAdController.isAdClicked || AppOpenAdController.disableByClickAction || isShowingAd || appOpenAd == null || !ConsentHelper.canRequestAds()) {
            onAdDismissed()
            return
        }

        isShowingAd = true
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                appOpenAd = null
                lastAdShownTime = System.currentTimeMillis() // 🕒 Cập nhật thời gian hiển thị
                loadAd() // Reload Ad after showing
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isShowingAd = false
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                lastAdShownTime = System.currentTimeMillis() // 🕒 Lưu thời gian hiển thị
            }
        }
        appOpenAd?.show(activity)
    }

    private fun isAdActivityOnTop(activity: Activity): Boolean {
        val activityName = activity.javaClass.simpleName
        return activityName.contains("AdActivity")
    }

    override fun onActivityResumed(activity: Activity) {
        if (isAdActivityOnTop(activity)) {
            Log.d("AppOpenAdManager", "🚫 AdActivity is on top, skipping App Open Ad")
            return
        }

        val activityName = activity.javaClass.simpleName
        Log.d("AppOpenAdManager", "🆕 Current Activity: $activityName | Previous Activity: $previousActivityName")

        // 🛑 Skip App Open Ad if the last screen was an ad screen
        if (previousActivityName?.contains("AdActivity") == true) {
            Log.d("AppOpenAdManager", "🚫 Skipping App Open Ad - Previous activity was an ad")
            previousActivityName = activityName // ✅ Update for next check
            return
        }

        previousActivityName = activityName // ✅ Update last known activity


        if (AppOpenAdController.shouldShowAd) {
            showAdIfAvailable(activity) {
                AppOpenAdController.disableByClickAction = false
                Log.d("AppOpenAdManager", "✅ App Open Ad completed or not available")
            }
        } else {
            Log.d("AppOpenAdManager", "🚫 App Open Ad disabled for this screen")
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}