package com.onelittlestep.sudokuplayer.sudoku

import com.onelittlestep.sudokuplayer.model.Difficulty
import kotlin.random.Random

data class GeneratedPuzzle(
    val puzzle: IntArray,
    val solution: IntArray,
    val difficulty: Difficulty
)

/**
 * Produces valid, uniquely-solvable Sudoku puzzles. Generation is CPU-bound and should be
 * called from a background thread/coroutine, never the main thread.
 */
object SudokuGenerator {

    fun generate(difficulty: Difficulty, random: Random = Random.Default): GeneratedPuzzle {
        val solution = SudokuSolver.generateFullSolution(random)
        val puzzle = solution.copyOf()

        // Remove cells in symmetric pairs for a visually balanced puzzle, only keeping a
        // removal if the resulting puzzle still has exactly one solution.
        val cellsToRemove = (81 - difficulty.clues).coerceIn(0, 81)
        var removed = 0

        val order = (0 until 81).shuffled(random)
        for (index in order) {
            if (removed >= cellsToRemove) break
            if (puzzle[index] == 0) continue

            val mirror = 80 - index
            val removingPair = mirror != index && puzzle[mirror] != 0
            val backupA = puzzle[index]
            val backupB = if (removingPair) puzzle[mirror] else 0

            puzzle[index] = 0
            if (removingPair && removed + 2 <= cellsToRemove) puzzle[mirror] = 0

            val stillUnique = SudokuSolver.countSolutions(puzzle, limit = 2) == 1
            if (stillUnique) {
                removed += if (removingPair && puzzle[mirror] == 0) 2 else 1
            } else {
                puzzle[index] = backupA
                if (removingPair) puzzle[mirror] = backupB
            }
        }

        return GeneratedPuzzle(puzzle, solution, difficulty)
    }
}
