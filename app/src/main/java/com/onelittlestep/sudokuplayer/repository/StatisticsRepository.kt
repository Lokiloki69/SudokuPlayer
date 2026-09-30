package com.onelittlestep.sudokuplayer.repository

import android.content.Context
import com.onelittlestep.sudokuplayer.model.AllStatistics
import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.model.DifficultyStats
import org.json.JSONObject

/** Persists per-difficulty play statistics locally via SharedPreferences. */
class StatisticsRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): AllStatistics {
        val map = Difficulty.entries.associateWith { getStats(it) }
        return AllStatistics(map)
    }

    fun getStats(difficulty: Difficulty): DifficultyStats {
        val raw = prefs.getString(keyFor(difficulty), null) ?: return DifficultyStats()
        return try {
            val json = JSONObject(raw)
            DifficultyStats(
                played = json.getInt("played"),
                completed = json.getInt("completed"),
                bestTimeSeconds = json.getLong("bestTimeSeconds"),
                totalTimeSeconds = json.getLong("totalTimeSeconds"),
                totalMistakes = json.getInt("totalMistakes"),
                totalHints = json.getInt("totalHints")
            )
        } catch (e: Exception) {
            DifficultyStats()
        }
    }

    private fun saveStats(difficulty: Difficulty, stats: DifficultyStats) {
        val json = JSONObject().apply {
            put("played", stats.played)
            put("completed", stats.completed)
            put("bestTimeSeconds", stats.bestTimeSeconds)
            put("totalTimeSeconds", stats.totalTimeSeconds)
            put("totalMistakes", stats.totalMistakes)
            put("totalHints", stats.totalHints)
        }
        prefs.edit().putString(keyFor(difficulty), json.toString()).apply()
    }

    /** Call once when a new puzzle is generated / started. */
    fun recordGameStarted(difficulty: Difficulty) {
        val current = getStats(difficulty)
        saveStats(difficulty, current.copy(played = current.played + 1))
    }

    /** Call once a game ends, whether completed or lost to the mistake limit. */
    fun recordGameEnded(
        difficulty: Difficulty,
        completed: Boolean,
        timeSeconds: Long,
        mistakes: Int,
        hints: Int
    ) {
        val current = getStats(difficulty)
        val updated = current.copy(
            completed = if (completed) current.completed + 1 else current.completed,
            bestTimeSeconds = if (completed) {
                if (current.bestTimeSeconds < 0) timeSeconds else minOf(current.bestTimeSeconds, timeSeconds)
            } else current.bestTimeSeconds,
            totalTimeSeconds = if (completed) current.totalTimeSeconds + timeSeconds else current.totalTimeSeconds,
            totalMistakes = current.totalMistakes + mistakes,
            totalHints = current.totalHints + hints
        )
        saveStats(difficulty, updated)
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    private fun keyFor(difficulty: Difficulty) = "stats_${difficulty.name}"

    companion object {
        private const val PREFS_NAME = "sudoku_stats_prefs"
    }
}
