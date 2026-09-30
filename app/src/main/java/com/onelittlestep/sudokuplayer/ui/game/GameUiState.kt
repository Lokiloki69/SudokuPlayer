package com.onelittlestep.sudokuplayer.ui.game

import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.model.GameStatus

/** Immutable snapshot of everything the game screen needs to render. */
data class GameUiState(
    val board: IntArray,
    val given: BooleanArray,
    val notes: Array<Set<Int>>,
    val selectedIndex: Int,
    val hintCells: Set<Int>,
    val errorCells: Set<Int>,
    val conflictCells: Set<Int>,
    val sameNumberCells: Set<Int>,
    val relatedCells: Set<Int>,
    val mistakes: Int,
    val mistakeLimit: Int,
    val hintsUsed: Int,
    val difficulty: Difficulty,
    val elapsedSeconds: Long,
    val status: GameStatus,
    val notesMode: Boolean,
    val canUndo: Boolean,
    val canRedo: Boolean,
    val remainingCounts: IntArray
)

sealed class GameEvent {
    data class Completed(
        val difficulty: Difficulty,
        val timeSeconds: Long,
        val mistakes: Int,
        val hints: Int
    ) : GameEvent()

    object GameOver : GameEvent()

    data class CheckResult(val wrongCount: Int) : GameEvent()
}
