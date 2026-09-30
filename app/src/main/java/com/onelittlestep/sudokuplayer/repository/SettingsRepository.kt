package com.onelittlestep.sudokuplayer.repository

import android.content.Context
import android.content.SharedPreferences
import com.onelittlestep.sudokuplayer.model.AppSettings
import com.onelittlestep.sudokuplayer.model.DarkMode

/** Persists user preferences locally via SharedPreferences. */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSettings(): AppSettings = AppSettings(
        darkMode = DarkMode.entries.firstOrNull { it.name == prefs.getString(KEY_DARK_MODE, null) }
            ?: DarkMode.SYSTEM,
        soundEnabled = prefs.getBoolean(KEY_SOUND, true),
        vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
        mistakeLimit = prefs.getInt(KEY_MISTAKE_LIMIT, 3),
        highlightConflicts = prefs.getBoolean(KEY_HIGHLIGHT_CONFLICTS, true),
        autoRemoveNotes = prefs.getBoolean(KEY_AUTO_REMOVE_NOTES, true),
        confirmNewGame = prefs.getBoolean(KEY_CONFIRM_NEW_GAME, true)
    )

    fun setDarkMode(mode: DarkMode) = prefs.edit().putString(KEY_DARK_MODE, mode.name).apply()
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
    fun setMistakeLimit(limit: Int) = prefs.edit().putInt(KEY_MISTAKE_LIMIT, limit).apply()
    fun setHighlightConflicts(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_HIGHLIGHT_CONFLICTS, enabled).apply()
    fun setAutoRemoveNotes(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_AUTO_REMOVE_NOTES, enabled).apply()
    fun setConfirmNewGame(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_CONFIRM_NEW_GAME, enabled).apply()

    companion object {
        private const val PREFS_NAME = "sudoku_settings_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_MISTAKE_LIMIT = "mistake_limit"
        private const val KEY_HIGHLIGHT_CONFLICTS = "highlight_conflicts"
        private const val KEY_AUTO_REMOVE_NOTES = "auto_remove_notes"
        private const val KEY_CONFIRM_NEW_GAME = "confirm_new_game"
    }
}
