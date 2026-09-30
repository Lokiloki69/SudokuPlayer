package com.onelittlestep.sudokuplayer.ads

import android.app.Activity
import android.util.DisplayMetrics
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Creates and loads an adaptive banner **in code** and drops it into [container].
 *
 * The Mobile Ads SDK requires `adSize`/`adUnitId` to be set before the AdView is attached to
 * the window; an `<com.google.android.gms.ads.AdView>` declared directly in XML can't satisfy
 * that in time (it gets attached during `setContentView`, before any of our Kotlin code runs)
 * and fails with "Required XML attribute 'adSize' was missing". Building the view in code, as
 * Google's own adaptive-banner samples do, avoids that entirely.
 *
 * Returns the created [AdView] so the caller can forward `resume()`/`pause()`/`destroy()` from
 * its own Activity lifecycle callbacks.
 */
fun loadAdaptiveBanner(activity: Activity, container: FrameLayout): AdView {
    val adView = AdView(activity).apply {
        adUnitId = AdUnitIds.BANNER
        setAdSize(currentOrientationAnchoredAdaptiveBannerAdSize(activity))
    }
    container.removeAllViews()
    container.addView(adView)
    adView.loadAd(AdRequest.Builder().build())
    return adView
}

/** Full-width adaptive banner size for the device's current screen width and orientation. */
@Suppress("DEPRECATION")
private fun currentOrientationAnchoredAdaptiveBannerAdSize(activity: Activity): AdSize {
    val outMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(outMetrics)
    val adWidthPixels = outMetrics.widthPixels.toFloat()
    val density = outMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
}
