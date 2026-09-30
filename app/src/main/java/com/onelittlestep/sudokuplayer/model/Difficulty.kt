package com.onelittlestep.sudokuplayer.model

/**
 * Difficulty levels. [clues] is the number of pre-filled cells left in a generated
 * puzzle out of 81 total cells - fewer clues means a harder puzzle.
 */
enum class Difficulty(val clues: Int, val displayName: String) {
    EASY(44, "Easy"),
    MEDIUM(36, "Medium"),
    HARD(30, "Hard"),
    EXPERT(24, "Expert");

    companion object {
        fun fromName(name: String?): Difficulty =
            entries.firstOrNull { it.name == name } ?: MEDIUM
    }
}
