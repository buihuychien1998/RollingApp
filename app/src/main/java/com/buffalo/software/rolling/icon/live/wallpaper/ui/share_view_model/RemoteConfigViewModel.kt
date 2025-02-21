package com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buffalo.software.rolling.icon.live.wallpaper.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemoteConfigViewModel : ViewModel() {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private val _configValues = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val configValues = _configValues.asStateFlow()

    init {
        fetchAllRemoteConfigValues()
    }

    private fun fetchAllRemoteConfigValues() {
        viewModelScope.launch {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // Fetch instantly (for testing)
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

            // ✅ Start a timeout in case of no internet
            val timeoutJob = launch {
                delay(3000) // 3 seconds timeout
                if (_configValues.value.isEmpty()) {
                    _configValues.value = getCachedConfigValues() // Use cached values
                    Log.d("RemoteConfig", "Using cached values due to timeout")
                }
            }

            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                timeoutJob.cancel() // ✅ Cancel timeout if fetch succeeds
                val fetchedValues = mutableMapOf<String, Boolean>()

                if (task.isSuccessful) {
                    remoteConfig.all.keys.forEach { key ->
                        fetchedValues[key] = remoteConfig.getBoolean(key)
                        Log.d("RemoteConfig", "$key = ${fetchedValues[key]}")
                    }
                    _configValues.value = fetchedValues
                } else {
                    Log.d("RemoteConfig", "Fetch failed, using cached values")
                    _configValues.value = getCachedConfigValues() // Use cached values
                }
            }
        }
    }

    private fun getCachedConfigValues(): Map<String, Boolean> {
        return remoteConfig.all.mapValues { it.value.asBoolean() } // Return cached values
    }
}
