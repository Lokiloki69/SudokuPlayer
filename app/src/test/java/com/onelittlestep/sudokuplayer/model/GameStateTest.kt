package com.onelittlestep.sudokuplayer.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateTest {

    private val solution = intArrayOf(
        5, 3, 4, 6, 7, 8, 9, 1, 2,
        6, 7, 2, 1, 9, 5, 3, 4, 8,
        1, 9, 8, 3, 4, 2, 5, 6, 7,
        8, 5, 9, 7, 6, 1, 4, 2, 3,
        4, 2, 6, 8, 5, 3, 7, 9, 1,
        7, 1, 3, 9, 2, 4, 8, 5, 6,
        9, 6, 1, 5, 3, 7, 2, 8, 4,
        2, 8, 7, 4, 1, 9, 6, 3, 5,
        3, 4, 5, 2, 8, 6, 1, 7, 9
    )

    @Test
    fun `newGame marks non-zero puzzle cells as given`() {
        val puzzle = IntArray(81)
        puzzle[0] = 5
        val state = GameState.newGame(Difficulty.EASY, puzzle, solution)
        assertTrue(state.given[0])
        assertFalse(state.given[1])
    }

    @Test
    fun `newGame copies puzzle into the working board`() {
        val puzzle = IntArray(81)
        puzzle[3] = 6
        val state = GameState.newGame(Difficulty.EASY, puzzle, solution)
        assertTrue(state.board[3] == 6)
    }

    @Test
    fun `isComplete false when board is empty`() {
        val state = GameState.newGame(Difficulty.EASY, IntArray(81), solution)
        assertFalse(state.isComplete())
    }

    @Test
    fun `isComplete true when board matches solution exactly`() {
        val state = GameState.newGame(Difficulty.EASY, IntArray(81), solution)
        solution.copyInto(state.board)
        assertTrue(state.isComplete())
    }

    @Test
    fun `isComplete false when a single cell is wrong`() {
        val state = GameState.newGame(Difficulty.EASY, IntArray(81), solution)
        solution.copyInto(state.board)
        state.board[0] = if (state.board[0] == 9) 8 else 9
        assertFalse(state.isComplete())
    }

    @Test
    fun `notes support multiple candidates per cell`() {
        val state = GameState.newGame(Difficulty.EASY, IntArray(81), solution)
        state.notes[0].add(1)
        state.notes[0].add(4)
        state.notes[0].add(7)
        assertTrue(state.notes[0] == setOf(1, 4, 7))
    }
}
