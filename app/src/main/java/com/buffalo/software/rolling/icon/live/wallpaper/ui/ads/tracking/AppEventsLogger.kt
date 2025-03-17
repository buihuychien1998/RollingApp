package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.tracking
import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import java.math.BigDecimal
import java.util.Currency

const val EVENT_NAME_PURCHASE = "EVENT_NAME_PURCHASE"
fun logAdRevenue(context: Context, adValue: AdValue, adType: String) {
    val logger = AppEventsLogger.newLogger(context)
    val revenue = adValue.valueMicros / 1_000_000.0 // Convert micros to USD
    val currency = Currency.getInstance(adValue.currencyCode)

    // Log purchase event
    logger.logPurchase(BigDecimal(revenue), currency)

    // Additional event logging
    val params = Bundle().apply {
        putString("ad_network", "AdMob")
        putString("ad_type", adType)
        putString("placement_id", "ad_placement_${adType.lowercase()}")
    }

    logger.logEvent(EVENT_NAME_PURCHASE, revenue, params)
}
fun setupBannerAd(context: Context, adView: AdView) {
    adView.setOnPaidEventListener { adValue ->
        logAdRevenue(context, adValue, "banner")
    }
}

fun setupNativeAd(context: Context, nativeAd: NativeAd) {
    nativeAd.setOnPaidEventListener { adValue ->
        logAdRevenue(context, adValue, "native")
    }
}

fun setupInterstitialAd(context: Context, interstitialAd: InterstitialAd) {
    interstitialAd.setOnPaidEventListener { adValue ->
        logAdRevenue(context, adValue, "interstitial")
    }
}

fun setupAppOpenAd(context: Context, appOpenAd: AppOpenAd) {
    appOpenAd.setOnPaidEventListener { adValue ->
        logAdRevenue(context, adValue, "app_open")
    }
}