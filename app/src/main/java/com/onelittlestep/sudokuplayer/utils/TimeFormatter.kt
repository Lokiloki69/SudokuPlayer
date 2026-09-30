package com.onelittlestep.sudokuplayer.utils

import java.util.Locale

object TimeFormatter {
    /** Formats a duration in seconds as MM:SS, or H:MM:SS once it reaches an hour. */
    fun format(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
