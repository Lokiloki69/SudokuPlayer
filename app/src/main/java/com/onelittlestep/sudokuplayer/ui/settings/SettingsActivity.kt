package com.onelittlestep.sudokuplayer.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.snackbar.Snackbar
import com.onelittlestep.sudokuplayer.R
import com.onelittlestep.sudokuplayer.ads.loadInlineNativeAd
import com.onelittlestep.sudokuplayer.applyDarkMode
import com.onelittlestep.sudokuplayer.databinding.ActivitySettingsBinding
import com.onelittlestep.sudokuplayer.databinding.ItemSettingsSwitchBinding
import com.onelittlestep.sudokuplayer.model.DarkMode

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private var currentNativeAd: NativeAd? = null

    private val mistakeLimitValues = listOf(1, 3, 5, 10, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val settings = viewModel.getSettings()

        setupDarkModeToggle(settings.darkMode)
        setupSwitchRow(
            binding.rowSound, R.string.settings_sound, null,
            settings.soundEnabled
        ) { viewModel.setSoundEnabled(it) }
        setupSwitchRow(
            binding.rowVibration, R.string.settings_vibration, null,
            settings.vibrationEnabled
        ) { viewModel.setVibrationEnabled(it) }
        setupSwitchRow(
            binding.rowHighlightConflicts, R.string.settings_highlight_conflicts, R.string.settings_highlight_conflicts_desc,
            settings.highlightConflicts
        ) { viewModel.setHighlightConflicts(it) }
        setupSwitchRow(
            binding.rowAutoRemoveNotes, R.string.settings_auto_remove_notes, R.string.settings_auto_remove_notes_desc,
            settings.autoRemoveNotes
        ) { viewModel.setAutoRemoveNotes(it) }
        setupSwitchRow(
            binding.rowConfirmNewGame, R.string.settings_confirm_new_game, R.string.settings_confirm_new_game_desc,
            settings.confirmNewGame
        ) { viewModel.setConfirmNewGame(it) }

        setupMistakeLimitSpinner(settings.mistakeLimit)

        binding.buttonResetStatistics.setOnClickListener { confirmResetStatistics() }

        loadInlineNativeAd(this, binding.nativeAdContainer) { nativeAd ->
            currentNativeAd?.destroy()
            currentNativeAd = nativeAd
        }
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
    }

    private fun setupDarkModeToggle(current: DarkMode) {
        val checkedId = when (current) {
            DarkMode.SYSTEM -> R.id.buttonModeSystem
            DarkMode.LIGHT -> R.id.buttonModeLight
            DarkMode.DARK -> R.id.buttonModeDark
        }
        binding.toggleDarkMode.check(checkedId)
        binding.toggleDarkMode.addOnButtonCheckedListener { _, checkedButtonId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedButtonId) {
                R.id.buttonModeLight -> DarkMode.LIGHT
                R.id.buttonModeDark -> DarkMode.DARK
                else -> DarkMode.SYSTEM
            }
            viewModel.setDarkMode(mode)
            applyDarkMode(mode)
        }
    }

    private fun setupSwitchRow(
        row: ItemSettingsSwitchBinding,
        titleRes: Int,
        descRes: Int?,
        initialValue: Boolean,
        onChanged: (Boolean) -> Unit
    ) {
        row.textTitle.setText(titleRes)
        if (descRes != null) {
            row.textDescription.setText(descRes)
            row.textDescription.visibility = View.VISIBLE
        }
        row.switchControl.isChecked = initialValue
        row.switchControl.setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
    }

    private fun setupMistakeLimitSpinner(currentLimit: Int) {
        val labels = listOf(
            getString(R.string.settings_mistake_limit_option, 1),
            getString(R.string.settings_mistake_limit_option, 3),
            getString(R.string.settings_mistake_limit_option, 5),
            getString(R.string.settings_mistake_limit_option, 10),
            getString(R.string.settings_mistake_limit_unlimited)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMistakeLimit.adapter = adapter
        val initialIndex = mistakeLimitValues.indexOf(currentLimit).let { if (it >= 0) it else 1 }
        binding.spinnerMistakeLimit.setSelection(initialIndex)
        binding.spinnerMistakeLimit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setMistakeLimit(mistakeLimitValues[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun confirmResetStatistics() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_reset_statistics_title)
            .setMessage(R.string.dialog_reset_statistics_message)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                viewModel.resetStatistics()
                Snackbar.make(binding.root, R.string.statistics_reset_confirmation, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
}
