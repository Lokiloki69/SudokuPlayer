package com.onelittlestep.sudokuplayer.sudoku

import kotlin.random.Random

/**
 * Backtracking Sudoku solver operating on a flat 81-cell IntArray (0 = empty).
 * Uses bitmasks per row/column/box and a minimum-remaining-values heuristic so that
 * it stays fast enough to check puzzle uniqueness during generation.
 */
object SudokuSolver {

    private const val FULL_MASK = 0x3FE // bits 1..9 set

    private class Masks(board: IntArray) {
        val row = IntArray(9)
        val col = IntArray(9)
        val box = IntArray(9)

        init {
            for (i in board.indices) {
                val v = board[i]
                if (v != 0) place(i, v)
            }
        }

        private fun boxIndex(index: Int): Int = SudokuValidator.boxOf(index)

        fun place(index: Int, value: Int) {
            val bit = 1 shl value
            row[SudokuValidator.rowOf(index)] = row[SudokuValidator.rowOf(index)] or bit
            col[SudokuValidator.colOf(index)] = col[SudokuValidator.colOf(index)] or bit
            box[boxIndex(index)] = box[boxIndex(index)] or bit
        }

        fun remove(index: Int, value: Int) {
            val bit = (1 shl value).inv()
            row[SudokuValidator.rowOf(index)] = row[SudokuValidator.rowOf(index)] and bit
            col[SudokuValidator.colOf(index)] = col[SudokuValidator.colOf(index)] and bit
            box[boxIndex(index)] = box[boxIndex(index)] and bit
        }

        fun candidates(index: Int): Int {
            val used = row[SudokuValidator.rowOf(index)] or
                col[SudokuValidator.colOf(index)] or
                box[boxIndex(index)]
            return FULL_MASK and used.inv()
        }
    }

    /**
     * Attempts to fully solve [board]. Returns a solved copy, or null if unsolvable.
     * Does not mutate [board].
     */
    fun solve(board: IntArray, random: Random? = null): IntArray? {
        val work = board.copyOf()
        val masks = Masks(work)
        return if (solveInternal(work, masks, random)) work else null
    }

    private fun solveInternal(board: IntArray, masks: Masks, random: Random?): Boolean {
        val next = findBestEmptyCell(board, masks) ?: return true
        val (index, candidateMask) = next
        if (candidateMask == 0) return false

        val values = (1..9).filter { candidateMask and (1 shl it) != 0 }.toMutableList()
        if (random != null) values.shuffle(random)

        for (v in values) {
            board[index] = v
            masks.place(index, v)
            if (solveInternal(board, masks, random)) return true
            masks.remove(index, v)
            board[index] = 0
        }
        return false
    }

    /** Returns the empty cell with fewest candidates (MRV heuristic) plus its candidate bitmask. */
    private fun findBestEmptyCell(board: IntArray, masks: Masks): Pair<Int, Int>? {
        var bestIndex = -1
        var bestMask = 0
        var bestCount = 10
        for (i in board.indices) {
            if (board[i] != 0) continue
            val mask = masks.candidates(i)
            val count = Integer.bitCount(mask)
            if (count < bestCount) {
                bestCount = count
                bestIndex = i
                bestMask = mask
                if (count == 0) break
                if (count == 1) break
            }
        }
        return if (bestIndex == -1) null else bestIndex to bestMask
    }

    /**
     * Counts solutions of [board] up to [limit] (stops early once reached, so this is fast
     * even on boards with many solutions). Used to verify a generated puzzle has exactly one.
     */
    fun countSolutions(board: IntArray, limit: Int = 2, nodeLimit: Int = 200_000): Int {
        val work = board.copyOf()
        val masks = Masks(work)
        val counter = intArrayOf(0)
        val nodes = intArrayOf(0)
        countInternal(work, masks, limit, counter, nodes, nodeLimit)
        return counter[0]
    }

    private fun countInternal(
        board: IntArray,
        masks: Masks,
        limit: Int,
        counter: IntArray,
        nodes: IntArray,
        nodeLimit: Int
    ) {
        if (counter[0] >= limit) return
        nodes[0]++
        if (nodes[0] > nodeLimit) {
            // Treat as ambiguous (>=2) so the caller conservatively rejects this puzzle.
            counter[0] = maxOf(counter[0], 2)
            return
        }

        val next = findBestEmptyCell(board, masks)
        if (next == null) {
            counter[0]++
            return
        }
        val (index, candidateMask) = next
        if (candidateMask == 0) return

        for (v in 1..9) {
            if (candidateMask and (1 shl v) == 0) continue
            board[index] = v
            masks.place(index, v)
            countInternal(board, masks, limit, counter, nodes, nodeLimit)
            masks.remove(index, v)
            board[index] = 0
            if (counter[0] >= limit) return
        }
    }

    /** Generates a fully solved, randomized, valid 9x9 Sudoku grid. */
    fun generateFullSolution(random: Random = Random.Default): IntArray {
        val board = IntArray(81)
        val masks = Masks(board)
        val ok = solveInternal(board, masks, random)
        check(ok) { "Failed to generate a full Sudoku solution" }
        return board
    }
}
