package com.example.rollingicon.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

@Composable
fun InterstitialAdButton(context: Context, content: @Composable () -> Unit) {

    var mInterstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }

    fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("InterstitialAdButton", adError.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("InterstitialAdButton", "Ad was loaded.".toString())
                    mInterstitialAd = interstitialAd
                }
            })
    }

    LaunchedEffect(Unit) {
        loadAd()
    }

    Button(onClick = {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d("InterstitialAdButton", "Ad was clicked.")
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    Log.d("InterstitialAdButton", "Ad dismissed fullscreen content.")
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    Log.e("InterstitialAdButton", "Ad failed to show fullscreen content.")
                    loadAd()
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d("InterstitialAdButton", "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d("InterstitialAdButton", "Ad showed fullscreen content.")
                }
            }

            mInterstitialAd?.show(context as Activity)
        } else {
            Toast.makeText(context, "Ad is loading, please try again", Toast.LENGTH_LONG).show()
            loadAd()
        }

    }) {
        content()
    }


}
