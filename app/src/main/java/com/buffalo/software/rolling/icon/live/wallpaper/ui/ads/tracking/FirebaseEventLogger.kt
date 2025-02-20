package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.tracking

import android.content.Context
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

object FirebaseEventLogger {

    private fun logEvent(context: Context, eventName: String, params: Map<String, Any>? = null) {
        val firebaseAnalytics = Firebase.analytics
        val bundle = Bundle()
        params?.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    // 📌 Tracking Functions
    fun trackScreenView(context: Context, screenName: String) {
        logEvent(context, "screen_view", mapOf("screen_name" to screenName))
    }

    fun trackButtonClick(context: Context, buttonName: String) {
        logEvent(context, "button_click", mapOf("button_name" to buttonName))
    }

    fun trackUserAction(context: Context, action: String, param: String, value: String) {
        logEvent(context, action, mapOf(param to value))
    }
}