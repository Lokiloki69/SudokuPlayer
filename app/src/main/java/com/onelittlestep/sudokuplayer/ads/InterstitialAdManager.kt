package com.onelittlestep.sudokuplayer.ads

import android.app.Activity
import android.app.Application
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Preloads and shows full-screen interstitial ads at natural break points: a completed puzzle,
 * or a Hint tap.
 *
 * One instance lives for the whole app ([com.onelittlestep.sudokuplayer.SudokuApplication]) so an
 * ad can be preloading in the background even while the user is mid-puzzle, and so the "show every
 * N completions" cap is tracked across screens rather than reset per Activity.
 *
 * The ad never blocks gameplay: if nothing is loaded, or a load/show fails, [onDone] still runs.
 */
class InterstitialAdManager(private val application: Application) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var completionsSinceLastShow = 0

    /** Call once, early (e.g. from [com.onelittlestep.sudokuplayer.SudokuApplication]), to warm the cache. */
    fun preload() {
        if (isLoading || interstitialAd != null) return
        isLoading = true
        InterstitialAd.load(
            application,
            AdUnitIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    // Non-fatal: the puzzle remains fully playable without ads. We simply retry
                    // the next time a natural break point is reached.
                    isLoading = false
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Call right after a puzzle is completed (e.g. when the completion dialog is dismissed).
     * Shows a preloaded interstitial at most once every [SHOW_EVERY_N_COMPLETIONS] completions
     * so ads never appear back-to-back or on every single puzzle. [onDone] always eventually
     * runs - immediately if no ad is shown, or once the shown ad is dismissed/fails.
     */
    fun maybeShowAfterPuzzleCompletion(activity: Activity, onDone: () -> Unit) {
        completionsSinceLastShow++
        if (completionsSinceLastShow < SHOW_EVERY_N_COMPLETIONS) {
            onDone()
            return
        }
        completionsSinceLastShow = 0
        showLoadedAdOrRunDone(activity, onDone)
    }

    /**
     * Call when the player taps Hint. Shows a preloaded interstitial before the hint is revealed;
     * [onDone] (which should reveal the hint) always eventually runs - immediately if no ad is
     * ready yet, or once a shown ad is dismissed/fails. Unlike [maybeShowAfterPuzzleCompletion]
     * this has no frequency cap - it shows whatever is loaded every time, and simply does nothing
     * extra when preloading hasn't caught up yet (e.g. two hints tapped in quick succession).
     */
    fun showOnHintUsed(activity: Activity, onDone: () -> Unit) {
        showLoadedAdOrRunDone(activity, onDone)
    }

    private fun showLoadedAdOrRunDone(activity: Activity, onDone: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            onDone()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preload()
                onDone()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                preload()
                onDone()
            }
        }
        // An InterstitialAd instance can only be shown once - drop our reference immediately.
        interstitialAd = null
        ad.show(activity)
    }

    companion object {
        /** Tune this to change post-completion ad frequency. 2 = at most every other completed puzzle. */
        private const val SHOW_EVERY_N_COMPLETIONS = 2
    }
}
