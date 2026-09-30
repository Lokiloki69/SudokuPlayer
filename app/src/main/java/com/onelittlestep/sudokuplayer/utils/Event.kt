package com.onelittlestep.sudokuplayer.utils

/**
 * Wraps a value that represents a one-off event (e.g. showing a dialog) so LiveData
 * observers - which may re-fire on configuration changes - only handle it once.
 */
class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
