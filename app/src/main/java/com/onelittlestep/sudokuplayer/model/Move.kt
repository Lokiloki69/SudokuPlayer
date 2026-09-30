package com.onelittlestep.sudokuplayer.model

/**
 * A single reversible player action, used for undo/redo.
 * Exactly one cell changes per move.
 */
data class Move(
    val cellIndex: Int,
    val prevValue: Int,
    val newValue: Int,
    val prevNotes: Set<Int>,
    val newNotes: Set<Int>,
    val wasHint: Boolean,
    val isHint: Boolean,
    val mistakeDelta: Int
)
