package com.onelittlestep.sudokuplayer.ui.statistics

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.tabs.TabLayout
import com.onelittlestep.sudokuplayer.R
import com.onelittlestep.sudokuplayer.ads.loadInlineNativeAd
import com.onelittlestep.sudokuplayer.databinding.ActivityStatisticsBinding
import com.onelittlestep.sudokuplayer.databinding.ItemStatTileBinding
import com.onelittlestep.sudokuplayer.model.AllStatistics
import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.model.DifficultyStats
import com.onelittlestep.sudokuplayer.utils.TimeFormatter

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()
    private var currentStatistics: AllStatistics? = null
    private var currentNativeAd: NativeAd? = null

    private val tabs = listOf<Difficulty?>(null, Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.EXPERT)

    private data class TileStyle(val containerRes: Int, val onContainerRes: Int)

    private val tileStyles = listOf(
        TileStyle(R.color.md_primary_container, R.color.md_on_primary_container),
        TileStyle(R.color.md_secondary_container, R.color.md_on_secondary_container),
        TileStyle(R.color.md_tertiary_container, R.color.md_on_tertiary_container)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.stats_tab_overall))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.difficulty_easy))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.difficulty_medium))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.difficulty_hard))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.difficulty_expert))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = renderSelectedTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        viewModel.statistics.observe(this) { stats ->
            currentStatistics = stats
            renderSelectedTab(binding.tabLayout.selectedTabPosition.coerceAtLeast(0))
        }

        loadInlineNativeAd(this, binding.nativeAdContainer) { nativeAd ->
            currentNativeAd?.destroy()
            currentNativeAd = nativeAd
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
    }

    private fun renderSelectedTab(position: Int) {
        val stats = currentStatistics ?: return
        val difficulty = tabs.getOrNull(position)
        val stat = if (difficulty == null) stats.overall else stats.perDifficulty.getValue(difficulty)
        renderStats(stat)
    }

    private fun renderStats(stat: DifficultyStats) {
        binding.layoutStatsContainer.removeAllViews()

        val bestTime = if (stat.bestTimeSeconds < 0) getString(R.string.stats_not_available) else TimeFormatter.format(stat.bestTimeSeconds)
        val avgTime = if (stat.completed == 0) getString(R.string.stats_not_available) else TimeFormatter.format(stat.averageTimeSeconds)

        binding.textWinRateValue.text = "${stat.winRatePercent}%"

        val tiles = listOf(
            Triple(R.drawable.ic_target, getString(R.string.stats_games_played), stat.played.toString()),
            Triple(R.drawable.ic_check, getString(R.string.stats_games_completed), stat.completed.toString()),
            Triple(R.drawable.ic_clock, getString(R.string.stats_best_time), bestTime),
            Triple(R.drawable.ic_clock, getString(R.string.stats_average_time), avgTime),
            Triple(R.drawable.ic_close, getString(R.string.stats_total_mistakes), stat.totalMistakes.toString()),
            Triple(R.drawable.ic_lightbulb, getString(R.string.stats_total_hints), stat.totalHints.toString())
        )

        tiles.chunked(2).forEachIndexed { rowIndex, rowTiles ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (rowIndex == 0) 0 else resources.getDimensionPixelSize(R.dimen.spacing_s)
                }
            }
            rowTiles.forEachIndexed { columnIndex, (iconRes, label, value) ->
                val tileIndex = rowIndex * 2 + columnIndex
                val tile = ItemStatTileBinding.inflate(layoutInflater, row, false)
                tile.imageIcon.setImageResource(iconRes)
                tile.textLabel.text = label
                tile.textValue.text = value
                applyTileStyle(tile, tileStyles[tileIndex % tileStyles.size])
                val params = tile.root.layoutParams as LinearLayout.LayoutParams
                params.marginStart = if (columnIndex == 0) 0 else resources.getDimensionPixelSize(R.dimen.spacing_s)
                tile.root.layoutParams = params
                row.addView(tile.root)
            }
            binding.layoutStatsContainer.addView(row)
        }
    }

    private fun applyTileStyle(tile: ItemStatTileBinding, style: TileStyle) {
        val container = ContextCompat.getColor(this, style.containerRes)
        val onContainer = ContextCompat.getColor(this, style.onContainerRes)
        tile.root.backgroundTintList = ColorStateList.valueOf(container)
        tile.imageIcon.imageTintList = ColorStateList.valueOf(onContainer)
        tile.textValue.setTextColor(onContainer)
        tile.textLabel.setTextColor(onContainer)
    }
}
