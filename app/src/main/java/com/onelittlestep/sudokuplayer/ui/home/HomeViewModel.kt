package com.onelittlestep.sudokuplayer.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.repository.GameRepository

data class ContinueGameSummary(
    val difficulty: Difficulty,
    val elapsedSeconds: Long,
    val progressPercent: Int
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val gameRepository = GameRepository(application)

    private val _continueGame = MutableLiveData<ContinueGameSummary?>(null)
    val continueGame: LiveData<ContinueGameSummary?> = _continueGame

    fun refresh() {
        val saved = gameRepository.loadGame()
        if (saved == null || !gameRepository.hasResumableGame()) {
            _continueGame.value = null
            return
        }
        val fillableCells = saved.given.count { !it }
        val filledCells = saved.board.indices.count { saved.board[it] != 0 && !saved.given[it] }
        val progress = if (fillableCells == 0) 0 else (filledCells * 100) / fillableCells
        _continueGame.value = ContinueGameSummary(
            difficulty = saved.difficulty,
            elapsedSeconds = saved.elapsedSeconds,
            progressPercent = progress
        )
    }
}
