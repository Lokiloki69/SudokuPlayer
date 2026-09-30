package com.onelittlestep.sudokuplayer.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.ads.AdView
import com.onelittlestep.sudokuplayer.R
import com.onelittlestep.sudokuplayer.ads.loadAdaptiveBanner
import com.onelittlestep.sudokuplayer.databinding.ActivityHomeBinding
import com.onelittlestep.sudokuplayer.ui.difficulty.DifficultyActivity
import com.onelittlestep.sudokuplayer.ui.game.GameActivity
import com.onelittlestep.sudokuplayer.ui.settings.SettingsActivity
import com.onelittlestep.sudokuplayer.ui.statistics.StatisticsActivity
import com.onelittlestep.sudokuplayer.utils.TimeFormatter

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private var bannerAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonContinue.setOnClickListener {
            startActivity(GameActivity.continueIntent(this))
        }
        binding.buttonNewGame.setOnClickListener {
            startActivity(Intent(this, DifficultyActivity::class.java))
        }
        binding.cardStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        applyPressFeedback(binding.cardStatistics)
        applyPressFeedback(binding.cardSettings)

        viewModel.continueGame.observe(this) { summary ->
            if (summary == null) {
                binding.cardContinueGame.visibility = View.GONE
                return@observe
            }
            binding.cardContinueGame.visibility = View.VISIBLE
            binding.textContinueDifficulty.text = getString(
                R.string.home_continue_summary,
                summary.difficulty.displayName,
                TimeFormatter.format(summary.elapsedSeconds)
            )
            binding.progressContinue.progress = summary.progressPercent
            binding.textContinueProgress.text =
                getString(R.string.home_continue_progress, summary.progressPercent)
        }

        bannerAdView = loadAdaptiveBanner(this, binding.adContainer)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
        bannerAdView?.resume()
    }

    override fun onPause() {
        bannerAdView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        bannerAdView?.destroy()
        super.onDestroy()
    }

    /** Small tactile scale-down/up on touch, purely visual - the click listener still fires normally. */
    @SuppressLint("ClickableViewAccessibility")
    private fun applyPressFeedback(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }
            false
        }
    }
}
