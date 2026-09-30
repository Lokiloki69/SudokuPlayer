package com.onelittlestep.sudokuplayer.sudoku

import com.onelittlestep.sudokuplayer.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuGeneratorTest {

    @Test
    fun `generated puzzle solution is a valid complete board`() {
        val result = SudokuGenerator.generate(Difficulty.EASY, Random(7))
        assertTrue(SudokuValidator.isValidCompleteBoard(result.solution))
    }

    @Test
    fun `generated puzzle cells match the solution wherever filled`() {
        val result = SudokuGenerator.generate(Difficulty.MEDIUM, Random(7))
        for (i in result.puzzle.indices) {
            if (result.puzzle[i] != 0) {
                assertEquals(result.solution[i], result.puzzle[i])
            }
        }
    }

    @Test
    fun `generated puzzle has a unique solution`() {
        val result = SudokuGenerator.generate(Difficulty.HARD, Random(7))
        assertEquals(1, SudokuSolver.countSolutions(result.puzzle, limit = 2))
    }

    @Test
    fun `harder difficulties leave fewer clues`() {
        val easy = SudokuGenerator.generate(Difficulty.EASY, Random(3))
        val expert = SudokuGenerator.generate(Difficulty.EXPERT, Random(3))
        val easyClues = easy.puzzle.count { it != 0 }
        val expertClues = expert.puzzle.count { it != 0 }
        assertTrue(expertClues <= easyClues)
    }

    @Test
    fun `expert puzzle stays close to its target clue count`() {
        val result = SudokuGenerator.generate(Difficulty.EXPERT, Random(11))
        val clues = result.puzzle.count { it != 0 }
        // Uniqueness constraints can prevent hitting the exact target; allow some slack.
        assertTrue(clues in Difficulty.EXPERT.clues..(Difficulty.EXPERT.clues + 8))
    }
}
