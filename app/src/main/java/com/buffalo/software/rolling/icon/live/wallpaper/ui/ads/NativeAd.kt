package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.buffalo.software.rolling.icon.live.wallpaper.R
import com.buffalo.software.rolling.icon.live.wallpaper.utils.SHOW_AD
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@Composable
fun NativeAdViewCompose(
    context: Context,
    nativeID: String,
    fallbackNativeID: String? = null,
    existingAd: NativeAd? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    layoutResId: Int = R.layout.native_ad_layout,
    backgroundTint: Int = Color.WHITE,
    adLayout: (NativeAdView, NativeAd?) -> View? = { adView, ad ->
        createAdLayout(
            context,
            adView,
            ad,
            layoutResId,
            backgroundTint
        )
    },
    reloadTrigger: MutableState<Boolean> = mutableStateOf(true),
    onAdLoaded: ((NativeAd?) -> Unit)? = null
) {
    var nativeAd by remember { mutableStateOf(existingAd ?: NativeAdManager.getPreloadedAd()) }
    var isAdLoading by remember { mutableStateOf(true) }

    var reloadJob by remember { mutableStateOf<Job?>(null) } // Track ongoing reload job

    fun reloadAd() {
        reloadJob?.cancel() // ✅ Cancel any ongoing reload
        reloadJob = CoroutineScope(Dispatchers.Main).launch {
            isAdLoading = true
            NativeAdManager.reloadNativeAd(context, nativeID, fallbackNativeID) { newAd ->
                nativeAd = newAd
                isAdLoading = false
                reloadTrigger.value = false // ✅ Reset trigger after load completes
                onAdLoaded?.invoke(newAd)
            }
        }
    }

    // 🔥 Reload when `reloadTrigger` changes, canceling any ongoing reload
    LaunchedEffect(reloadTrigger.value) {
        println("NativeAdViewCompose: reloadTrigger.value  ${reloadTrigger.value}")

        if (reloadTrigger.value) {
            reloadAd()
        }
    }

    LaunchedEffect(existingAd) {
        if (existingAd != null) {
            nativeAd = existingAd
            isAdLoading = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("NativeAdViewCompose: AppOpenAdController.shouldShowAd ${AppOpenAdController.shouldShowAd}")

                if (!AppOpenAdController.shouldShowAd) {
                    AppOpenAdController.shouldShowAd = true
                    reloadAd()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
//            NativeAdManager.destroyAds()
            reloadJob?.cancel() // ✅ Cancel reload when disposed
        }
    }

    if (SHOW_AD && ConsentHelper.canRequestAds()) {
        Box(modifier = modifier.padding(8.dp)) {
            if (isAdLoading) {
                NativeShimmerEffect(
                    modifier
                        .height(268.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    backgroundTint = androidx.compose.ui.graphics.Color(backgroundTint)
                )
            } else {
                nativeAd?.let { ad ->
                    AndroidView(
                        factory = { context ->
                            val nativeAdView = NativeAdView(context)
                            nativeAdView.apply {
                                removeAllViews()
                                addView(adLayout(this, ad))
                            }

                        },
                        update = { nativeAdView ->
                            nativeAdView.apply {
                                removeAllViews()
                                addView(adLayout(this, ad))
                            }
                        }
                    )
                }
            }
        }
    }
}


fun createAdLayout(
    context: Context, nativeAdView: NativeAdView, nativeAd: NativeAd?, layoutResId: Int,
    backgroundTint: Int = Color.WHITE
): View? {
    if (nativeAd == null) return null
    val layout = LayoutInflater.from(context).inflate(layoutResId, null)
    ViewCompat.setBackgroundTintList(layout, ColorStateList.valueOf(backgroundTint))

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