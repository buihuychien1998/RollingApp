package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentHelper {
    private var consentInformation: ConsentInformation? = null

    /**
     * Initialize Consent SDK and request user consent.
     */
    fun initializeConsent(activity: Activity, onConsentResult: (Boolean) -> Unit) {
        consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA) // Enable for EU testing
                    .addTestDeviceHashedId("TEST-DEVICE-ID") // Replace with your test device ID
                    .build()
            )
            .build()

        consentInformation?.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Check if consent form is available
                if (consentInformation?.isConsentFormAvailable == true) {
                    loadAndShowConsentForm(activity, onConsentResult)
                } else {
                    onConsentResult(consentInformation?.canRequestAds() == true)
                }
            },
            { error ->
                println("Consent request error: ${error.message}")
                onConsentResult(false)
            }
        )
    }

    /**
     * Load and show the consent form if required.
     */
    private fun loadAndShowConsentForm(activity: Activity, onConsentResult: (Boolean) -> Unit) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
            activity
        ) { formError ->
            if (formError != null) {
                println("Consent form error: ${formError.message}")
            }
            onConsentResult(consentInformation?.canRequestAds() == true)
        }
    }

    /**
     * Check if ads can be requested after consent.
     */
    fun canRequestAds(): Boolean {
        return consentInformation?.canRequestAds() == true
    }

    /**
     * Reset consent information (for testing).
     */
    fun resetConsent(context: Context) {
        UserMessagingPlatform.getConsentInformation(context).reset()
    }
}