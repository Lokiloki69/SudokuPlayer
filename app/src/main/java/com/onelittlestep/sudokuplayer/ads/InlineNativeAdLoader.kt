package com.onelittlestep.sudokuplayer.ads

import android.app.Activity
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.onelittlestep.sudokuplayer.databinding.NativeAdLayoutBinding

/**
 * Loads one Native Advanced ad and binds it into an inflated `native_ad_layout.xml`, showing the
 * card only once a real ad has loaded and hiding it again on failure - the screen it's placed on
 * (e.g. Statistics, Settings) is always fully usable with or without the ad.
 *
 * [onResult] receives the loaded [NativeAd] (so the caller can `destroy()` it in `onDestroy()`),
 * or `null` if loading failed.
 */
fun loadInlineNativeAd(
    activity: Activity,
    binding: NativeAdLayoutBinding,
    onResult: (NativeAd?) -> Unit
) {
    val adLoader = AdLoader.Builder(activity, AdUnitIds.NATIVE_ADVANCED)
        .forNativeAd { nativeAd ->
            if (activity.isFinishing || activity.isDestroyed) {
                nativeAd.destroy()
                return@forNativeAd
            }
            NativeAdBinder.bind(binding, nativeAd)
            binding.root.visibility = View.VISIBLE
            onResult(nativeAd)
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                // Non-fatal: the screen remains fully usable without the ad card.
                binding.root.visibility = View.GONE
                onResult(null)
            }
        })
        .withNativeAdOptions(NativeAdOptions.Builder().build())
        .build()
    adLoader.loadAd(AdRequest.Builder().build())
}
