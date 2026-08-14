package com.example.ui.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.config.AdMobConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun AdMobNativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.NATIVE_AD_UNIT_ID
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isFailed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
                isLoading = false
                isFailed = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    isFailed = true
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    when {
        isFailed -> {
            // If ad fails to load, collapse height so no blank space is left
        }
        isLoading -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("native_ad_loading"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .shimmerEffect()
                )
            }
        }
        nativeAd != null -> {
            val loadedAd = nativeAd!!
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("native_ad_card"),
                factory = { ctx ->
                    val view = LayoutInflater.from(ctx).inflate(R.layout.item_native_ad, null, false) as NativeAdView
                    populateNativeAdView(loadedAd, view)
                    view
                },
                update = { view ->
                    populateNativeAdView(loadedAd, view)
                }
            )
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.mediaView = adView.findViewById(R.id.ad_media) as? MediaView
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    (adView.headlineView as? TextView)?.text = nativeAd.headline

    val bodyView = adView.bodyView as? TextView
    if (nativeAd.body.isNullOrBlank()) {
        bodyView?.visibility = View.GONE
    } else {
        bodyView?.visibility = View.VISIBLE
        bodyView?.text = nativeAd.body
    }

    val ctaView = adView.callToActionView as? Button
    if (nativeAd.callToAction.isNullOrBlank()) {
        ctaView?.visibility = View.GONE
    } else {
        ctaView?.visibility = View.VISIBLE
        ctaView?.text = nativeAd.callToAction
    }

    val iconView = adView.iconView as? ImageView
    if (nativeAd.icon == null) {
        iconView?.visibility = View.GONE
    } else {
        iconView?.visibility = View.VISIBLE
        iconView?.setImageDrawable(nativeAd.icon?.drawable)
    }

    val advertiserView = adView.advertiserView as? TextView
    if (nativeAd.advertiser.isNullOrBlank()) {
        advertiserView?.visibility = View.GONE
    } else {
        advertiserView?.visibility = View.VISIBLE
        advertiserView?.text = nativeAd.advertiser
    }

    val mediaView = adView.mediaView as? MediaView
    if (mediaView != null) {
        val mediaContent = nativeAd.mediaContent
        if (mediaContent != null && (mediaContent.hasVideoContent() || mediaContent.mainImage != null)) {
            mediaView.mediaContent = mediaContent
            mediaView.visibility = View.VISIBLE
        } else {
            mediaView.visibility = View.GONE
        }
    }

    adView.setNativeAd(nativeAd)
}
