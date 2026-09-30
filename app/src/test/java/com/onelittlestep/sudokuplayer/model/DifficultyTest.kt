package com.onelittlestep.sudokuplayer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyTest {

    @Test
    fun `clue counts decrease as difficulty increases`() {
        assertTrue(Difficulty.EASY.clues > Difficulty.MEDIUM.clues)
        assertTrue(Difficulty.MEDIUM.clues > Difficulty.HARD.clues)
        assertTrue(Difficulty.HARD.clues > Difficulty.EXPERT.clues)
    }

    @Test
    fun `fromName resolves a known name`() {
        assertEquals(Difficulty.HARD, Difficulty.fromName("HARD"))
    }

    @Test
    fun `fromName falls back to medium for unknown or null input`() {
        assertEquals(Difficulty.MEDIUM, Difficulty.fromName("NOT_A_DIFFICULTY"))
        assertEquals(Difficulty.MEDIUM, Difficulty.fromName(null))
    }
}
