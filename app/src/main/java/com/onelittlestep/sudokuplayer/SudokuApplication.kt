package com.onelittlestep.sudokuplayer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.onelittlestep.sudokuplayer.ads.InterstitialAdManager
import com.onelittlestep.sudokuplayer.model.DarkMode
import com.onelittlestep.sudokuplayer.repository.SettingsRepository

class SudokuApplication : Application() {

    val interstitialAdManager by lazy { InterstitialAdManager(this) }

    override fun onCreate() {
        super.onCreate()
        val mode = SettingsRepository(this).getSettings().darkMode
        applyDarkMode(mode)

        // Initialize the Mobile Ads SDK once, as early as possible. This call returns
        // immediately - the SDK finishes its own setup asynchronously in the background.
        MobileAds.initialize(this)
        interstitialAdManager.preload()
    }
}

fun applyDarkMode(mode: DarkMode) {
    val nightMode = when (mode) {
        DarkMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        DarkMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DarkMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }
    AppCompatDelegate.setDefaultNightMode(nightMode)
}
