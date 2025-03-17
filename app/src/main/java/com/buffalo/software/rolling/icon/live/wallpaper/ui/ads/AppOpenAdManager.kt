package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.tracking.setupAppOpenAd
import com.buffalo.software.rolling.icon.live.wallpaper.utils.SHOW_AD
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(private val application: Application) :
    Application.ActivityLifecycleCallbacks {

    companion object {
        @Volatile
        private var instance: AppOpenAdManager? = null

        fun getInstance(application: Application): AppOpenAdManager {
            return instance ?: synchronized(this) {
                instance ?: AppOpenAdManager(application).also { instance = it }
            }
        }

        private var appOpenAd: AppOpenAd? = null // ✅ Singleton instance
    }
    private var isShowingAd = false
    private var lastAdShownTime: Long = 0 // 🕒 Thời điểm hiển thị quảng cáo gần nhất
//    private val adCooldownMillis = 25_000L // ⏳ Giãn cách 25 giây
    private val adCooldownMillis = 500L // ⏳ Giãn cách 25 giây
    private var previousActivityName: String? = null  // 🔥 Store last opened activity name
    private var isResumedOnce = false

    init {
        application.registerActivityLifecycleCallbacks(this)
        loadAd()
    }

    private fun loadAd() {
        if (!SHOW_AD || !AppOpenAdController.enableConfig || !ConsentHelper.canRequestAds() || appOpenAd != null) {
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
        if (currentTime - InterstitialAdManager.lastInterstitialTime < InterstitialAdManager.interstitialCooldownMillis || InterstitialAdManager.isAdShowing.value) {
            Log.d("AppOpenAdManager", "🚫 Skipping App Open Ad due to recent interstitial ad")
            onAdDismissed()
            return
        }

        if (!SHOW_AD || currentTime - lastAdShownTime < adCooldownMillis) {
            onAdDismissed()
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (appOpenAd == null) {
            onAdDismissed()
            loadAd()
            return
        }

        if (!AppOpenAdController.enableConfig
            || !AppOpenAdController.shouldShowAd || AppOpenAdController.isAdClicked
            || AppOpenAdController.disableByClickAction || isShowingAd
            || !ConsentHelper.canRequestAds()) {
            onAdDismissed()
            return
        }
        Log.d("AppOpenAdManager", "🚫 showAdIfAvailable")

        isShowingAd = true
        setupAppOpenAd(activity, appOpenAd!!)
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                appOpenAd = null
                lastAdShownTime = System.currentTimeMillis() // 🕒 Cập nhật thời gian hiển thị
                onAdDismissed()
                loadAd() // Reload Ad after showing
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d("AppOpenAdManager", adError.message)
                isShowingAd = false
                appOpenAd = null
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {

            }
        }
        appOpenAd?.show(activity)
    }

    private fun isAdActivityOnTop(activity: Activity): Boolean {
        val activityName = activity.javaClass.simpleName
        return activityName.contains("AdActivity")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("AppOpenAdManager", "🚫 appOpenAd $appOpenAd")
        if (isResumedOnce) {
            Log.d("AppOpenAdManager", "🚫 Skipping duplicate onActivityResumed call")
            return
        }

        isResumedOnce = true
        Handler(Looper.getMainLooper()).postDelayed({ isResumedOnce = false }, adCooldownMillis) // Reset sau 500ms

        if (isShowingAd) {
            Log.d("AppOpenAdManager", "🚫 Ad is already showing, skipping onActivityResumed")
            return
        }

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