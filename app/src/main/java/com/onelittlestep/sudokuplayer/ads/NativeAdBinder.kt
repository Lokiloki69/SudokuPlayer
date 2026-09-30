package com.onelittlestep.sudokuplayer.ads

import android.view.View
import com.google.android.gms.ads.nativead.NativeAd
import com.onelittlestep.sudokuplayer.databinding.NativeAdLayoutBinding

/**
 * Binds a loaded [NativeAd] onto an inflated `native_ad_layout.xml`, registering each creative
 * view with the [com.google.android.gms.ads.nativead.NativeAdView] as required by the Native
 * Advanced Ads API. Optional assets (icon, media, body) are hidden individually if the specific
 * ad has no content for them, per AdMob's native ad implementation guidelines.
 */
object NativeAdBinder {

    fun bind(binding: NativeAdLayoutBinding, nativeAd: NativeAd) {
        val adView = binding.nativeAdView

        adView.headlineView = binding.adHeadline
        adView.bodyView = binding.adBody
        adView.iconView = binding.adAppIcon
        adView.mediaView = binding.adMedia
        adView.callToActionView = binding.adCallToAction
        adView.adChoicesView = binding.adChoicesView

        binding.adHeadline.text = nativeAd.headline

        val body = nativeAd.body
        binding.adBody.text = body
        binding.adBody.isVisible = !body.isNullOrEmpty()

        val icon = nativeAd.icon
        if (icon != null) {
            binding.adAppIcon.setImageDrawable(icon.drawable)
            binding.adAppIcon.isVisible = true
        } else {
            binding.adAppIcon.isVisible = false
        }

        val callToAction = nativeAd.callToAction
        binding.adCallToAction.text = callToAction
        binding.adCallToAction.isVisible = !callToAction.isNullOrEmpty()

        binding.adMedia.isVisible = nativeAd.mediaContent != null

        // Must be last: this hands the ad object to the view so it can track impressions/clicks.
        adView.setNativeAd(nativeAd)
    }

    private var View.isVisible: Boolean
        get() = visibility == View.VISIBLE
        set(value) {
            visibility = if (value) View.VISIBLE else View.GONE
        }
}
