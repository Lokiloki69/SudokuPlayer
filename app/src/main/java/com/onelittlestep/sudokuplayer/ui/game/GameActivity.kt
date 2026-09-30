package com.onelittlestep.sudokuplayer.ui.game

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.onelittlestep.sudokuplayer.R
import com.onelittlestep.sudokuplayer.SudokuApplication
import com.onelittlestep.sudokuplayer.ads.loadAdaptiveBanner
import com.onelittlestep.sudokuplayer.databinding.ActivityGameBinding
import com.onelittlestep.sudokuplayer.databinding.ItemControlButtonBinding
import com.onelittlestep.sudokuplayer.databinding.ItemHudCardBinding
import com.onelittlestep.sudokuplayer.databinding.ItemNumberPadBinding
import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.model.GameStatus
import com.onelittlestep.sudokuplayer.repository.SettingsRepository
import com.onelittlestep.sudokuplayer.ui.difficulty.DifficultyActivity
import com.onelittlestep.sudokuplayer.ui.settings.SettingsActivity
import com.onelittlestep.sudokuplayer.utils.TimeFormatter

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var numberPadCells: List<ItemNumberPadBinding>
    private lateinit var controlUndo: ItemControlButtonBinding
    private lateinit var controlRedo: ItemControlButtonBinding
    private lateinit var controlErase: ItemControlButtonBinding
    private lateinit var controlNotes: ItemControlButtonBinding
    private lateinit var controlHint: ItemControlButtonBinding
    private lateinit var cardTimer: ItemHudCardBinding
    private lateinit var cardMistakes: ItemHudCardBinding
    private lateinit var cardHints: ItemHudCardBinding

    private var pauseMenuItem: MenuItem? = null
    private var pauseOverlayShown = false
    private var bannerAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        settingsRepository = SettingsRepository(this)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.boardView.onCellClick = { index -> viewModel.selectCell(index) }

        setupHudCards()
        setupControlButtons()
        setupNumberPad()
        binding.buttonResume.setOnClickListener { viewModel.resume() }

        viewModel.uiState.observe(this) { renderState(it) }
        viewModel.elapsedSeconds.observe(this) { cardTimer.textValue.text = TimeFormatter.format(it) }
        viewModel.isGenerating.observe(this) { generating ->
            binding.progressGenerating.visibility = if (generating) View.VISIBLE else View.GONE
            binding.boardView.visibility = if (generating) View.INVISIBLE else View.VISIBLE
        }
        viewModel.events.observe(this) { event ->
            event.getContentIfNotHandled()?.let { handleEvent(it) }
        }

        val difficultyExtra = intent.getStringExtra(EXTRA_DIFFICULTY)
        val requestedDifficulty = difficultyExtra?.let { Difficulty.fromName(it) }
        val forceNewGame = intent.getBooleanExtra(EXTRA_NEW_GAME, false)
        viewModel.start(requestedDifficulty, forceNewGame)

        bannerAdView = loadAdaptiveBanner(this, binding.adContainerGame)
    }

    override fun onResume() {
        super.onResume()
        bannerAdView?.resume()
    }

    override fun onPause() {
        bannerAdView?.pause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.pause()
        viewModel.persistState()
    }

    override fun onDestroy() {
        bannerAdView?.destroy()
        super.onDestroy()
    }

    private fun setupHudCards() {
        cardTimer = ItemHudCardBinding.bind(binding.cardTimer.root)
        cardMistakes = ItemHudCardBinding.bind(binding.cardMistakes.root)
        cardHints = ItemHudCardBinding.bind(binding.cardHints.root)

        cardTimer.imageIcon.setImageResource(R.drawable.ic_clock)
        cardTimer.textLabel.text = getString(R.string.hud_time)
        cardTimer.textValue.text = getString(R.string.timer_default)

        cardMistakes.imageIcon.setImageResource(R.drawable.ic_error_outline)
        cardMistakes.textLabel.text = getString(R.string.hud_mistakes)

        cardHints.imageIcon.setImageResource(R.drawable.ic_lightbulb)
        cardHints.textLabel.text = getString(R.string.hud_hints)

        applyHudTint(cardTimer, R.drawable.bg_chip_primary, R.color.md_on_primary_container)
        applyHudTint(cardMistakes, R.drawable.bg_chip_secondary, R.color.md_on_secondary_container)
        applyHudTint(cardHints, R.drawable.bg_chip_tertiary, R.color.md_on_tertiary_container)
    }

    private fun applyHudTint(card: ItemHudCardBinding, backgroundRes: Int, colorRes: Int) {
        card.root.setBackgroundResource(backgroundRes)
        val color = ContextCompat.getColor(this, colorRes)
        ImageViewCompat.setImageTintList(card.imageIcon, ColorStateList.valueOf(color))
        card.textValue.setTextColor(color)
        card.textLabel.setTextColor(color)
    }

    private fun setupControlButtons() {
        controlUndo = ItemControlButtonBinding.bind(binding.controlUndo.root)
        controlRedo = ItemControlButtonBinding.bind(binding.controlRedo.root)
        controlErase = ItemControlButtonBinding.bind(binding.controlErase.root)
        controlNotes = ItemControlButtonBinding.bind(binding.controlNotes.root)
        controlHint = ItemControlButtonBinding.bind(binding.controlHint.root)

        configureControl(controlUndo, R.drawable.ic_undo, R.string.action_undo)
        configureControl(controlRedo, R.drawable.ic_redo, R.string.action_redo)
        configureControl(controlErase, R.drawable.ic_backspace, R.string.action_erase)
        configureControl(controlNotes, R.drawable.ic_edit, R.string.action_notes)
        configureControl(controlHint, R.drawable.ic_lightbulb, R.string.action_hint)

        controlUndo.imageIcon.setOnClickListener { viewModel.undo() }
        controlRedo.imageIcon.setOnClickListener { viewModel.redo() }
        controlErase.imageIcon.setOnClickListener { viewModel.eraseCell() }
        controlNotes.imageIcon.setOnClickListener { viewModel.toggleNotesMode() }
        controlHint.imageIcon.setOnClickListener {
            showHintInterstitialThen { viewModel.hint() }
        }

        listOf(
            controlUndo.imageIcon, controlRedo.imageIcon, controlErase.imageIcon,
            controlNotes.imageIcon, controlHint.imageIcon
        ).forEach { applyPressFeedback(it) }
    }

    /** Adds a small tactile scale-down/up on touch, purely visual - the click listener still fires normally. */
    @SuppressLint("ClickableViewAccessibility")
    private fun applyPressFeedback(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.90f).scaleY(0.90f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }
            false
        }
    }

    private fun configureControl(control: ItemControlButtonBinding, iconRes: Int, labelRes: Int) {
        control.imageIcon.setImageResource(iconRes)
        control.imageIcon.contentDescription = getString(labelRes)
        control.textLabel.setText(labelRes)
    }

    private fun setupNumberPad() {
        val cells = mutableListOf<ItemNumberPadBinding>()
        for (value in 1..9) {
            val cellBinding = ItemNumberPadBinding.inflate(LayoutInflater.from(this), binding.layoutNumberPad, false)
            val params = cellBinding.root.layoutParams as LinearLayout.LayoutParams
            val marginPx = (resources.displayMetrics.density * 3).toInt()
            params.marginStart = if (value == 1) 0 else marginPx
            params.marginEnd = if (value == 9) 0 else marginPx
            cellBinding.root.layoutParams = params
            cellBinding.textNumber.text = value.toString()
            cellBinding.root.contentDescription = getString(R.string.number_pad_number, value)
            cellBinding.root.setOnClickListener { viewModel.inputNumber(value) }
            applyPressFeedback(cellBinding.root)
            binding.layoutNumberPad.addView(cellBinding.root)
            cells.add(cellBinding)
        }
        numberPadCells = cells
    }

    private fun renderState(state: GameUiState) {
        binding.boardView.setState(
            board = state.board,
            given = state.given,
            notes = state.notes,
            selectedIndex = state.selectedIndex,
            hintCells = state.hintCells,
            errorCells = state.errorCells,
            conflictCells = state.conflictCells,
            sameNumberCells = state.sameNumberCells,
            relatedCells = state.relatedCells
        )
        binding.boardView.setInteractionEnabled(state.status == GameStatus.PLAYING)

        cardMistakes.textValue.text = if (state.mistakeLimit > 0) {
            getString(R.string.mistakes_value, state.mistakes, state.mistakeLimit)
        } else {
            state.mistakes.toString()
        }
        val mistakesColorRes = if (state.mistakes > 0) R.color.md_on_error_container else R.color.md_on_secondary_container
        applyHudTint(
            cardMistakes,
            backgroundRes = if (state.mistakes > 0) R.drawable.bg_chip_error else R.drawable.bg_chip_secondary,
            colorRes = mistakesColorRes
        )
        cardHints.textValue.text = state.hintsUsed.toString()
        supportActionBar?.title = state.difficulty.displayName

        controlUndo.imageIcon.isEnabled = state.canUndo
        controlUndo.imageIcon.alpha = if (state.canUndo) 1f else 0.4f
        controlRedo.imageIcon.isEnabled = state.canRedo
        controlRedo.imageIcon.alpha = if (state.canRedo) 1f else 0.4f
        controlNotes.imageIcon.setBackgroundResource(
            if (state.notesMode) R.drawable.bg_circle_button_active else R.drawable.bg_circle_button
        )
        ImageViewCompat.setImageTintList(
            controlNotes.imageIcon,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    if (state.notesMode) R.color.md_on_primary_container else R.color.text_primary
                )
            )
        )

        for (value in 1..9) {
            val cell = numberPadCells[value - 1]
            val remaining = state.remainingCounts[value]
            cell.textRemaining.text = getString(R.string.remaining_count, remaining)
            val depleted = remaining <= 0
            cell.root.isEnabled = !depleted && state.status == GameStatus.PLAYING
            cell.root.alpha = if (depleted) 0.35f else 1f
        }

        setPauseOverlayVisible(state.status == GameStatus.PAUSED)
        val isPlaying = state.status == GameStatus.PLAYING
        binding.layoutControls.alpha = if (isPlaying) 1f else 0.4f
        binding.layoutNumberPad.alpha = if (isPlaying) 1f else 0.4f

        pauseMenuItem?.let {
            val paused = state.status == GameStatus.PAUSED
            it.setIcon(if (paused) R.drawable.ic_play_arrow else R.drawable.ic_pause)
            it.setTitle(if (paused) R.string.action_resume else R.string.action_pause)
            it.isEnabled = state.status == GameStatus.PLAYING || state.status == GameStatus.PAUSED
        }
    }

    /** Fades the pause overlay in/out instead of an abrupt visibility swap. */
    private fun setPauseOverlayVisible(visible: Boolean) {
        if (visible == pauseOverlayShown) return
        pauseOverlayShown = visible
        binding.layoutPauseOverlay.animate().cancel()
        if (visible) {
            binding.layoutPauseOverlay.alpha = 0f
            binding.layoutPauseOverlay.visibility = View.VISIBLE
            binding.layoutPauseOverlay.animate().alpha(1f).setDuration(200).start()
        } else {
            binding.layoutPauseOverlay.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction { binding.layoutPauseOverlay.visibility = View.GONE }
                .start()
        }
    }

    private fun handleEvent(event: GameEvent) {
        when (event) {
            is GameEvent.Completed -> showCompletionDialog(event)
            GameEvent.GameOver -> showGameOverDialog()
            is GameEvent.CheckResult -> {
                val message = if (event.wrongCount == 0) {
                    getString(R.string.no_errors_found)
                } else {
                    val cellWord = cellCountWord(event.wrongCount)
                    getString(R.string.errors_found, event.wrongCount, cellWord)
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun cellCountWord(count: Int): String =
        getString(if (count == 1) R.string.cell_singular else R.string.cell_plural)

    private fun showCompletionDialog(event: GameEvent.Completed) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_completion, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<TextView>(R.id.textDifficulty).text =
            getString(R.string.completed_difficulty, event.difficulty.displayName)
        view.findViewById<TextView>(R.id.textTime).text =
            getString(R.string.completed_time, TimeFormatter.format(event.timeSeconds))
        view.findViewById<TextView>(R.id.textMistakes).text =
            getString(R.string.completed_mistakes, event.mistakes)
        view.findViewById<TextView>(R.id.textHints).text =
            getString(R.string.completed_hints, event.hints)

        view.findViewById<View>(R.id.buttonNewGame).setOnClickListener {
            dialog.dismiss()
            showInterstitialThen {
                startActivity(Intent(this, DifficultyActivity::class.java))
                finish()
            }
        }
        view.findViewById<View>(R.id.buttonPlayAgain).setOnClickListener {
            dialog.dismiss()
            showInterstitialThen { viewModel.generateNewGame(event.difficulty) }
        }
        dialog.show()
    }

    /**
     * Natural break point for a full-screen interstitial: right after the player dismisses the
     * completion dialog. [action] always runs - immediately if no ad is shown this time (see
     * [InterstitialAdManager]'s frequency cap), or once a shown ad is dismissed.
     */
    private fun showInterstitialThen(action: () -> Unit) {
        val adManager = (application as? SudokuApplication)?.interstitialAdManager
        if (adManager == null) {
            action()
            return
        }
        adManager.maybeShowAfterPuzzleCompletion(this, action)
    }

    /**
     * Shows a full-screen interstitial (if one is ready) before revealing a hint. [action] - which
     * actually reveals the hint - always eventually runs, whether or not an ad was shown.
     */
    private fun showHintInterstitialThen(action: () -> Unit) {
        val adManager = (application as? SudokuApplication)?.interstitialAdManager
        if (adManager == null) {
            action()
            return
        }
        adManager.showOnHintUsed(this, action)
    }

    private fun showGameOverDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.game_over_title)
            .setMessage(R.string.game_over_message)
            .setCancelable(false)
            .setPositiveButton(R.string.action_new_game) { _, _ ->
                startActivity(Intent(this, DifficultyActivity::class.java))
                finish()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_game, menu)
        pauseMenuItem = menu.findItem(R.id.action_pause_resume)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_check -> {
                viewModel.check()
                true
            }
            R.id.action_pause_resume -> {
                val paused = pauseMenuItem?.title == getString(R.string.action_resume)
                if (paused) viewModel.resume() else viewModel.pause()
                true
            }
            R.id.action_restart -> {
                confirmThen(
                    settingsRepository.getSettings().confirmNewGame,
                    R.string.dialog_restart_title,
                    R.string.dialog_restart_message
                ) { viewModel.restart() }
                true
            }
            R.id.action_new_game -> {
                confirmThen(
                    settingsRepository.getSettings().confirmNewGame,
                    R.string.dialog_new_game_title,
                    R.string.dialog_new_game_message
                ) {
                    startActivity(Intent(this, DifficultyActivity::class.java))
                    finish()
                }
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmThen(needsConfirm: Boolean, titleRes: Int, messageRes: Int, action: () -> Unit) {
        if (!needsConfirm) {
            action()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(R.string.dialog_confirm) { _, _ -> action() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    companion object {
        private const val EXTRA_DIFFICULTY = "extra_difficulty"
        private const val EXTRA_NEW_GAME = "extra_new_game"

        fun newGameIntent(context: Context, difficulty: Difficulty): Intent =
            Intent(context, GameActivity::class.java)
                .putExtra(EXTRA_DIFFICULTY, difficulty.name)
                .putExtra(EXTRA_NEW_GAME, true)

        fun continueIntent(context: Context): Intent = Intent(context, GameActivity::class.java)
    }
}
