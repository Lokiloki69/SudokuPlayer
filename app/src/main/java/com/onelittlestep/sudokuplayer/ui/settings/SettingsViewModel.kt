package com.onelittlestep.sudokuplayer.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.onelittlestep.sudokuplayer.model.AppSettings
import com.onelittlestep.sudokuplayer.model.DarkMode
import com.onelittlestep.sudokuplayer.repository.SettingsRepository
import com.onelittlestep.sudokuplayer.repository.StatisticsRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val statisticsRepository = StatisticsRepository(application)

    fun getSettings(): AppSettings = settingsRepository.getSettings()

    fun setDarkMode(mode: DarkMode) = settingsRepository.setDarkMode(mode)
    fun setSoundEnabled(enabled: Boolean) = settingsRepository.setSoundEnabled(enabled)
    fun setVibrationEnabled(enabled: Boolean) = settingsRepository.setVibrationEnabled(enabled)
    fun setMistakeLimit(limit: Int) = settingsRepository.setMistakeLimit(limit)
    fun setHighlightConflicts(enabled: Boolean) = settingsRepository.setHighlightConflicts(enabled)
    fun setAutoRemoveNotes(enabled: Boolean) = settingsRepository.setAutoRemoveNotes(enabled)
    fun setConfirmNewGame(enabled: Boolean) = settingsRepository.setConfirmNewGame(enabled)

    fun resetStatistics() = statisticsRepository.resetAll()
}
