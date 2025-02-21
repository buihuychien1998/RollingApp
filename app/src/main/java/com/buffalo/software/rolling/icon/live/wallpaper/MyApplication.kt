package com.buffalo.software.rolling.icon.live.wallpaper

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Firebase Remote Config
        val remoteConfig = Firebase.remoteConfig

        // Set Remote Config settings
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0) // 0 for immediate fetching (useful for testing)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Load default values from XML
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // Fetch Remote Config values
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("MyApplication", "Config params updated: $updated")
                } else {
                    Log.d("MyApplication", "Fetch failed")
                }

                // Example: Retrieve a value from Remote Config
                val exampleValue = remoteConfig.getString("config_key_example")
                Log.d("MyApplication", "Fetched value: $exampleValue")
            }

        // Initialize Google Mobile Ads SDK in the background
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(applicationContext) {}
        }
    }

    private fun fetchRemoteConfig() {

    }

}