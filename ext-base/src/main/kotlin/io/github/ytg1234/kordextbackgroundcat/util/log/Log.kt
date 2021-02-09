package io.github.ytg1234.kordextbackgroundcat.util.log

/**
 * Represents a log - Constructed from a [LogSource] and some text.
 *
 * @param source The source if this log - Where did it come from?
 * @param text The contents of the log.
 */
data class Log(val source: LogSource, val text: String) : CharSequence by text

/**
 * Different places that logs can come from.
 */
enum class LogSource {
    /**
     * MultiMC Launcher
     */
    MultiMc,

    /**
     * Not MutiMC Launcher
     */
    NotMultiMc
}
