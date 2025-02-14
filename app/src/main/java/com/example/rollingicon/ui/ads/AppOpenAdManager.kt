package com.example.rollingicon.ui.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
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

    init {
        application.registerActivityLifecycleCallbacks(this)
        loadAd()
    }

    private fun loadAd() {
        if (!ConsentHelper.canRequestAds()) return

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

        if (currentTime - lastAdShownTime < adCooldownMillis) {
            onAdDismissed()
            return
        }

        if (!AppOpenAdController.shouldShowAd || AppOpenAdController.disableByClickAction || isShowingAd || appOpenAd == null || !ConsentHelper.canRequestAds()) {
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

    override fun onActivityResumed(activity: Activity) {
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