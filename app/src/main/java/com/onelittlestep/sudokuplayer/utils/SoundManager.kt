package com.onelittlestep.sudokuplayer.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Small offline sound/haptics helper - no audio assets needed, uses system tone/vibration APIs. */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext
    private var toneGenerator: ToneGenerator? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playTap(soundEnabled: Boolean) {
        if (!soundEnabled) return
        play(ToneGenerator.TONE_PROP_BEEP, 40)
    }

    fun playError(soundEnabled: Boolean) {
        if (!soundEnabled) return
        play(ToneGenerator.TONE_SUP_ERROR, 120)
    }

    fun playSuccess(soundEnabled: Boolean) {
        if (!soundEnabled) return
        play(ToneGenerator.TONE_PROP_ACK, 150)
    }

    private fun play(tone: Int, durationMs: Int) {
        try {
            val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 70).also {
                toneGenerator = it
            }
            generator.startTone(tone, durationMs)
        } catch (e: RuntimeException) {
            // ToneGenerator can fail to init on some devices/emulators - sound is non-critical.
        }
    }

    fun vibrate(enabled: Boolean, durationMs: Long = 20) {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
