package com.example.rollingicon.ui.ads

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.rollingicon.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdViewCompose(
    context: Context,
    nativeID: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    adLayout: (NativeAdView, NativeAd?) -> View? = { adView, ad -> createAdLayout(context, adView, ad) } // ✅ Custom layout support
) {
    var nativeAd by remember { mutableStateOf(NativeAdManager.getPreloadedAd()) }
    var isAdLoading by remember { mutableStateOf(true) }

    fun reloadAd() {
        isAdLoading = true
        NativeAdManager.reloadNativeAd(context, nativeID) { newAd ->
            nativeAd = newAd
            isAdLoading = false
        }
    }

    LaunchedEffect(Unit){
        reloadAd()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("NativeAd", "🔥 App Resumed -> Reloading Ad")
                if(!AppOpenAdController.shouldShowAd){
                    AppOpenAdController.shouldShowAd = true
                    reloadAd()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            NativeAdManager.destroyAd()
        }
    }

    if(ConsentHelper.canRequestAds()){
        Box(modifier = modifier.padding(8.dp)) {
            if (isAdLoading) {
                NativeShimmerEffect(
                    modifier
                        .height(268.dp)
                        .clip(RoundedCornerShape(8.dp)))
            } else {
                nativeAd?.let {
                    AndroidView(
                        factory = { context ->
                            val nativeAdView = NativeAdView(context)
                            nativeAdView.apply {
                                removeAllViews()
                                addView(adLayout(this, nativeAd))
                            }

                        },
                        update = { nativeAdView ->
                            nativeAdView.apply {
                                removeAllViews()
                                addView(adLayout(this, nativeAd))
                            }
                        }
                    )
                }
            }
        }
    }
}

fun createAdLayout(context: Context, nativeAdView: NativeAdView, nativeAd: NativeAd?): View? {
    if (nativeAd == null) return null
    val layout = LayoutInflater.from(context).inflate(R.layout.native_ad_layout, null)

    nativeAdView.apply {
        val headlineView = layout.findViewById<TextView>(R.id.ad_headline)
        val advertiserView = layout.findViewById<TextView>(R.id.ad_advertiser)
        val iconView = layout.findViewById<ImageView>(R.id.ad_app_icon)
        val callToActionView = layout.findViewById<Button>(R.id.ad_call_to_action)
        val bodyView = layout.findViewById<TextView>(R.id.ad_body)
        val mediaView = layout.findViewById<MediaView>(R.id.ad_media)

        // Assign views to nativeAdView
        this.headlineView = headlineView
        this.advertiserView = advertiserView
        this.iconView = iconView
        this.callToActionView = callToActionView
        this.bodyView = bodyView
        this.mediaView = mediaView

        // ✅ Set text data
        headlineView.text = nativeAd.headline
        bodyView.text = nativeAd.body ?: ""
        advertiserView.text = nativeAd.advertiser ?: ""

        // ✅ Set CTA button
        callToActionView.apply {
            text = nativeAd.callToAction
            visibility = if (nativeAd.callToAction != null) View.VISIBLE else View.GONE
            setOnClickListener { nativeAdView.performClick() }
        }

        // ✅ Set icon if available
        nativeAd.icon?.let {
            iconView.setImageDrawable(it.drawable)
            iconView.visibility = View.VISIBLE
        } ?: run { iconView.visibility = View.GONE }

        // ✅ Set MediaView (handles video/image ads)
        mediaView.mediaContent = nativeAd.mediaContent

        // ✅ Register Ad after setting all views
        setNativeAd(nativeAd)
    }

    return layout
}