package com.onelittlestep.sudokuplayer.model

/**
 * Aggregate statistics for a single difficulty level.
 * [bestTimeSeconds] is -1 when no game has been completed yet.
 */
data class DifficultyStats(
    val played: Int = 0,
    val completed: Int = 0,
    val bestTimeSeconds: Long = -1,
    val totalTimeSeconds: Long = 0,
    val totalMistakes: Int = 0,
    val totalHints: Int = 0
) {
    val winRatePercent: Int
        get() = if (played == 0) 0 else (completed * 100) / played

    val averageTimeSeconds: Long
        get() = if (completed == 0) 0 else totalTimeSeconds / completed

    operator fun plus(other: DifficultyStats): DifficultyStats {
        val best = when {
            bestTimeSeconds < 0 -> other.bestTimeSeconds
            other.bestTimeSeconds < 0 -> bestTimeSeconds
            else -> minOf(bestTimeSeconds, other.bestTimeSeconds)
        }
        return DifficultyStats(
            played = played + other.played,
            completed = completed + other.completed,
            bestTimeSeconds = best,
            totalTimeSeconds = totalTimeSeconds + other.totalTimeSeconds,
            totalMistakes = totalMistakes + other.totalMistakes,
            totalHints = totalHints + other.totalHints
        )
    }
}

data class AllStatistics(
    val perDifficulty: Map<Difficulty, DifficultyStats>
) {
    val overall: DifficultyStats
        get() = perDifficulty.values.fold(DifficultyStats()) { acc, s -> acc + s }
}
