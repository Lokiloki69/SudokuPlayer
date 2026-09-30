package com.onelittlestep.sudokuplayer.model

enum class DarkMode {
    SYSTEM, LIGHT, DARK
}

data class AppSettings(
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val mistakeLimit: Int = 3,
    val highlightConflicts: Boolean = true,
    val autoRemoveNotes: Boolean = true,
    val confirmNewGame: Boolean = true
)
