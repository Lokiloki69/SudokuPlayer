package com.onelittlestep.sudokuplayer.model

/**
 * Full mutable state of an in-progress (or finished) Sudoku game.
 * All board-shaped arrays are flat, size 81, indexed as row * 9 + col.
 */
class GameState(
    val difficulty: Difficulty,
    val puzzle: IntArray,
    val solution: IntArray,
    val board: IntArray,
    val given: BooleanArray,
    val notes: Array<MutableSet<Int>>,
    val hintCells: MutableSet<Int> = mutableSetOf(),
    var selectedIndex: Int = -1,
    var mistakes: Int = 0,
    var hintsUsed: Int = 0,
    var elapsedSeconds: Long = 0,
    var status: GameStatus = GameStatus.PLAYING,
    var notesMode: Boolean = false,
    val undoStack: MutableList<Move> = mutableListOf(),
    val redoStack: MutableList<Move> = mutableListOf(),
    var checkedErrorCells: MutableSet<Int> = mutableSetOf()
) {
    fun isComplete(): Boolean {
        for (i in 0 until 81) {
            if (board[i] != solution[i]) return false
        }
        return true
    }

    companion object {
        const val SIZE = 81

        fun newGame(difficulty: Difficulty, puzzle: IntArray, solution: IntArray): GameState {
            val board = puzzle.copyOf()
            val given = BooleanArray(SIZE) { puzzle[it] != 0 }
            val notes = Array(SIZE) { mutableSetOf<Int>() }
            return GameState(
                difficulty = difficulty,
                puzzle = puzzle,
                solution = solution,
                board = board,
                given = given,
                notes = notes
            )
        }
    }
}
