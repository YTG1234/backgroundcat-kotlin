package io.github.ytg1234.kordextbackgroundcat.util.log

/**
 * A [LogProcessor] is an object that receives a [Log] and processes it
 * for a [Mistake].
 */
fun interface LogProcessor {
    /**
     * Processes a log object for a [Mistake].
     *
     * @return A [Mistake] instance if a mistake has been found, `null` otherwise.
     */
    fun process(log: Log): Mistake?
    operator fun invoke(log: Log) = process(log)
}

/**
 * A [LogProcessor] that has some options for extra
 * functionality.
 */
interface LogProcessorWithOptions : LogProcessor {
    /**
     * Set of [LogProcessorOption]s to change the behaviour of this processor.
     */
    val options: Set<LogProcessorOption>

    companion object {
        /**
         * Creates a new [LogProcessorWithOptions] from a set of options and
         * a process lambda.
         */
        @JvmStatic
        fun of(options: Set<LogProcessorOption>, process: Log.() -> Mistake?) =
            LogProcessorWithOptionsImpl(options) { it.process() }
    }
}

/**
 * The default implementation for [LogProcessorWithOptions].
 *
 * @see LogProcessorWithOptions
 */
class LogProcessorWithOptionsImpl(override val options: Set<LogProcessorOption>, private val delegate: LogProcessor) :
    LogProcessorWithOptions {
    override fun process(log: Log): Mistake? = delegate.process(log)
}

/**
 * Represents an option that can change the behaviour of
 * a [LogProcessorWithOptions].
 */
sealed class LogProcessorOption {
    /**
     * Cancel this processor if any of [processors] has been run.
     *
     * @param processors The processor IDs for cancelling.
     */
    data class CancelIfRan(val processors: Set<String>) : LogProcessorOption() {
        constructor(vararg processors: String) : this(setOf(*processors))
    }

    /**
     * Don't continue processing after this processor
     * is done.
     */
    object CancelOthers : LogProcessorOption()
}
