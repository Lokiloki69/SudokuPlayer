package com.onelittlestep.sudokuplayer.ads

import com.onelittlestep.sudokuplayer.BuildConfig

/**
 * Central place for every AdMob ad unit ID used in the app.
 *
 * Debug builds always resolve to Google's official test ad units so development and manual
 * testing never serve (or accidentally click) real ads. Release builds resolve to the real,
 * production ad units.
 *
 * IMPORTANT - before publishing: double check you are building a `release` variant so
 * [BuildConfig.DEBUG] is false and the PROD_* constants below are actually served. Do not
 * remove the DEBUG check to "test the real ads" locally - use a release build instead, per
 * AdMob policy (https://support.google.com/admob/answer/6065877).
 */
object AdUnitIds {

    // --- Production ad units (com.onelittlestep.sudokuplayer / AdMob app ca-app-pub-8696769092648191) ---
    private const val PROD_BANNER = "ca-app-pub-8696769092648191/1958089625"
    private const val PROD_INTERSTITIAL = "ca-app-pub-8696769092648191/2560724858"
    private const val PROD_NATIVE_ADVANCED = "ca-app-pub-8696769092648191/7621479845"

    // --- Google's official sample/test ad units - safe to serve in any app during development ---
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_NATIVE_ADVANCED = "ca-app-pub-3940256099942544/2247696110"

    val BANNER: String get() = if (BuildConfig.DEBUG) TEST_BANNER else PROD_BANNER
    val INTERSTITIAL: String get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL else PROD_INTERSTITIAL
    val NATIVE_ADVANCED: String get() = if (BuildConfig.DEBUG) TEST_NATIVE_ADVANCED else PROD_NATIVE_ADVANCED
}
