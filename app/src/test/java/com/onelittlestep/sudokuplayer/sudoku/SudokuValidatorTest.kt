package com.onelittlestep.sudokuplayer.sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuValidatorTest {

    private val solvedBoard = intArrayOf(
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
    fun `peersOf returns exactly 20 unique cells`() {
        val peers = SudokuValidator.peersOf(40)
        assertEquals(20, peers.size)
        assertEquals(20, peers.toSet().size)
        assertFalse(peers.contains(40))
    }

    @Test
    fun `rowOf colOf boxOf compute correct coordinates`() {
        assertEquals(4, SudokuValidator.rowOf(40))
        assertEquals(4, SudokuValidator.colOf(40))
        assertEquals(4, SudokuValidator.boxOf(40))
        assertEquals(0, SudokuValidator.boxOf(0))
        assertEquals(8, SudokuValidator.boxOf(80))
    }

    @Test
    fun `conflictsAt detects duplicate in row`() {
        val board = IntArray(81)
        board[0] = 5
        board[1] = 5
        assertTrue(SudokuValidator.conflictsAt(board, 1, 5))
    }

    @Test
    fun `conflictsAt detects duplicate in column`() {
        val board = IntArray(81)
        board[0] = 7
        board[9] = 7
        assertTrue(SudokuValidator.conflictsAt(board, 9, 7))
    }

    @Test
    fun `conflictsAt detects duplicate in box`() {
        val board = IntArray(81)
        board[0] = 3
        board[10] = 3
        assertTrue(SudokuValidator.conflictsAt(board, 10, 3))
    }

    @Test
    fun `conflictsAt returns false when no duplicate`() {
        val board = IntArray(81)
        board[0] = 3
        assertFalse(SudokuValidator.conflictsAt(board, 1, 4))
    }

    @Test
    fun `conflictsAt ignores empty value`() {
        val board = IntArray(81)
        assertFalse(SudokuValidator.conflictsAt(board, 5, 0))
    }

    @Test
    fun `conflictingIndices finds all duplicated cells`() {
        val board = IntArray(81)
        board[0] = 5
        board[1] = 5
        val conflicts = SudokuValidator.conflictingIndices(board)
        assertEquals(setOf(0, 1), conflicts)
    }

    @Test
    fun `isSolved true when board matches solution`() {
        assertTrue(SudokuValidator.isSolved(solvedBoard, solvedBoard))
    }

    @Test
    fun `isSolved false when a cell differs`() {
        val board = solvedBoard.copyOf()
        board[0] = if (board[0] == 9) 8 else 9
        assertFalse(SudokuValidator.isSolved(board, solvedBoard))
    }

    @Test
    fun `isValidCompleteBoard true for a correct solution`() {
        assertTrue(SudokuValidator.isValidCompleteBoard(solvedBoard))
    }

    @Test
    fun `isValidCompleteBoard false when a zero remains`() {
        val board = solvedBoard.copyOf()
        board[0] = 0
        assertFalse(SudokuValidator.isValidCompleteBoard(board))
    }

    @Test
    fun `isValidCompleteBoard false when duplicates exist`() {
        val board = solvedBoard.copyOf()
        board[1] = board[0]
        assertFalse(SudokuValidator.isValidCompleteBoard(board))
    }
}
