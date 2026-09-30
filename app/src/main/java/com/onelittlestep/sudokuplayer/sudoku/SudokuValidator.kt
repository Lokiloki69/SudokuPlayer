package com.onelittlestep.sudokuplayer.sudoku

/**
 * Stateless rule-checking helpers for a flat 81-cell (row * 9 + col) Sudoku board.
 * A value of 0 means "empty".
 */
object SudokuValidator {

    fun rowOf(index: Int) = index / 9
    fun colOf(index: Int) = index % 9
    fun boxOf(index: Int): Int {
        val r = rowOf(index)
        val c = colOf(index)
        return (r / 3) * 3 + (c / 3)
    }

    fun indexOf(row: Int, col: Int) = row * 9 + col

    /** Indices of all cells sharing a row, column or box with [index], excluding itself. */
    fun peersOf(index: Int): List<Int> {
        val row = rowOf(index)
        val col = colOf(index)
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        val peers = LinkedHashSet<Int>()
        for (c in 0 until 9) if (c != col) peers.add(indexOf(row, c))
        for (r in 0 until 9) if (r != row) peers.add(indexOf(r, col))
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                val i = indexOf(r, c)
                if (i != index) peers.add(i)
            }
        }
        return peers.toList()
    }

    /**
     * True if placing [value] at [index] would duplicate an existing value in the
     * same row, column or box (the cell at [index] itself is ignored).
     */
    fun conflictsAt(board: IntArray, index: Int, value: Int): Boolean {
        if (value == 0) return false
        for (peer in peersOf(index)) {
            if (board[peer] == value) return true
        }
        return false
    }

    /** All indices that currently duplicate another value within their row/column/box. */
    fun conflictingIndices(board: IntArray): Set<Int> {
        val result = mutableSetOf<Int>()
        for (i in board.indices) {
            val v = board[i]
            if (v == 0) continue
            if (conflictsAt(board, i, v)) result.add(i)
        }
        return result
    }

    /** True if the board is completely filled and matches [solution] exactly. */
    fun isSolved(board: IntArray, solution: IntArray): Boolean {
        for (i in board.indices) {
            if (board[i] != solution[i]) return false
        }
        return true
    }

    /** True if a fully populated 9x9 grid obeys all Sudoku rules (no duplicates, no zeros). */
    fun isValidCompleteBoard(board: IntArray): Boolean {
        if (board.any { it !in 1..9 }) return false
        return conflictingIndices(board).isEmpty()
    }
}
