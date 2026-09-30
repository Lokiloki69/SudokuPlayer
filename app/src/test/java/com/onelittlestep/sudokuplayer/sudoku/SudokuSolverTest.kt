package com.onelittlestep.sudokuplayer.sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuSolverTest {

    // A well-known puzzle with exactly one solution.
    private val uniquePuzzle = intArrayOf(
        5, 3, 0, 0, 7, 0, 0, 0, 0,
        6, 0, 0, 1, 9, 5, 0, 0, 0,
        0, 9, 8, 0, 0, 0, 0, 6, 0,
        8, 0, 0, 0, 6, 0, 0, 0, 3,
        4, 0, 0, 8, 0, 3, 0, 0, 1,
        7, 0, 0, 0, 2, 0, 0, 0, 6,
        0, 6, 0, 0, 0, 0, 2, 8, 0,
        0, 0, 0, 4, 1, 9, 0, 0, 5,
        0, 0, 0, 0, 8, 0, 0, 7, 9
    )

    @Test
    fun `solve returns a valid complete solution for a solvable puzzle`() {
        val solved = SudokuSolver.solve(uniquePuzzle)
        assertNotNull(solved)
        assertTrue(SudokuValidator.isValidCompleteBoard(solved!!))
        for (i in uniquePuzzle.indices) {
            if (uniquePuzzle[i] != 0) assertEquals(uniquePuzzle[i], solved[i])
        }
    }

    @Test
    fun `solve does not mutate the input board`() {
        val copy = uniquePuzzle.copyOf()
        SudokuSolver.solve(uniquePuzzle)
        assertTrue(uniquePuzzle.contentEquals(copy))
    }

    @Test
    fun `solve returns null for an unsolvable board`() {
        val board = uniquePuzzle.copyOf()
        // Force a contradiction: two 5s in the same row.
        board[1] = 5
        assertNull(SudokuSolver.solve(board))
    }

    @Test
    fun `countSolutions finds exactly one solution for a well-formed puzzle`() {
        assertEquals(1, SudokuSolver.countSolutions(uniquePuzzle, limit = 2))
    }

    @Test
    fun `countSolutions caps at the requested limit`() {
        // A nearly empty board has vastly more than 2 solutions.
        val board = IntArray(81)
        assertEquals(2, SudokuSolver.countSolutions(board, limit = 2))
    }

    @Test
    fun `generateFullSolution produces a valid complete board`() {
        val solution = SudokuSolver.generateFullSolution(Random(42))
        assertTrue(SudokuValidator.isValidCompleteBoard(solution))
    }

    @Test
    fun `generateFullSolution is randomized across seeds`() {
        val a = SudokuSolver.generateFullSolution(Random(1))
        val b = SudokuSolver.generateFullSolution(Random(2))
        assertTrue(!a.contentEquals(b))
    }
}
