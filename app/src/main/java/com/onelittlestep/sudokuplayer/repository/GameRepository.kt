package com.onelittlestep.sudokuplayer.repository

import android.content.Context
import com.onelittlestep.sudokuplayer.model.GameState
import com.onelittlestep.sudokuplayer.model.GameStatus
import org.json.JSONObject

/** Persists the single in-progress game so it survives rotation, backgrounding and restarts. */
class GameRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveGame(state: GameState) {
        val json = GameStateJson.toJson(state)
        prefs.edit().putString(KEY_GAME_STATE, json.toString()).apply()
    }

    fun loadGame(): GameState? {
        val raw = prefs.getString(KEY_GAME_STATE, null) ?: return null
        return try {
            GameStateJson.fromJson(JSONObject(raw))
        } catch (e: Exception) {
            null
        }
    }

    fun hasResumableGame(): Boolean {
        val state = loadGame() ?: return false
        return state.status == GameStatus.PLAYING || state.status == GameStatus.PAUSED
    }

    fun clearGame() {
        prefs.edit().remove(KEY_GAME_STATE).apply()
    }

    companion object {
        private const val PREFS_NAME = "sudoku_game_prefs"
        private const val KEY_GAME_STATE = "game_state"
    }
}
