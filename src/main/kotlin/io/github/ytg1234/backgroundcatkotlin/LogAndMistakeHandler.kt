package io.github.ytg1234.backgroundcatkotlin

import io.github.ytg1234.backgroundcatkotlin.util.internal.ConfigHolder
import io.github.ytg1234.backgroundcatkotlin.util.internal.logger
import io.github.ytg1234.backgroundcatkotlin.util.log.Log
import io.github.ytg1234.backgroundcatkotlin.util.log.LogProcessorOption
import io.github.ytg1234.backgroundcatkotlin.util.log.LogProcessorWithOptions
import io.github.ytg1234.backgroundcatkotlin.util.log.LogSource
import io.github.ytg1234.backgroundcatkotlin.util.log.Mistake

/**
 * Returns a [LogSource] that a specified log is coming from.
 *
 * @param text The log.
 *
 * @return The source of this log.
 */
fun sourceFromLog(text: String): LogSource {
    return if (text.startsWith("MultiMC version")) LogSource.MultiMc
    else LogSource.NotMultiMc
}

/**
 * Map of a [String] id and a [LogProcessorWithOptions].
 */
val processors = mutableMapOf<String, LogProcessorWithOptions>()

/**
 * Adds a new [LogProcessorWithOptions] to [the map][processors].
 *
 * @param id The unique identifier for this processor.
 * @param processor The processor being added.
 */
fun addProcessor(id: String, processor: LogProcessorWithOptions) {
    if (id == "") throw IllegalArgumentException("Tried to add a processor for empty ID!")
    if (processors[id] != null) throw IllegalArgumentException("Tried to add a processor for ID $id which was already added!")

    if (!ConfigHolder.isParserEnabled(id)) {
        logger.debug("Not adding processor with ID $id because it is not enabled.")
        return
    }

    processors[id] = processor
}

/**
 * Adds a new [LogProcessorWithOptions] to [the map][processors], but
 * with more syntax sugar.
 *
 * @param id The unique identifier for this processor.
 * @param options A set of [LogProcessorOption]s that are passed to the constructor.
 * @param processor The [process][LogProcessorWithOptions.process] function of the processor, from a different point of view.
 *
 * @see addProcessor
 */
fun withProcessor(id: String, options: Set<LogProcessorOption> = setOf(), processor: Log.() -> Mistake?) =
    addProcessor(id, LogProcessorWithOptions.of(options, processor))

/**
 * More syntax sugar for [addProcessor]!
 *
 * @param id The unique identifier for this processor.
 * @param options The options being passed to the constructor.
 * @param processor The [process][LogProcessorWithOptions.process] function of the processor, from a different point of view.
 *
 * @see withProcessor
 * @see addProcessor
 */
fun withProcessor(id: String, vararg options: LogProcessorOption, processor: Log.() -> Mistake?) =
    withProcessor(id, setOf(*options), processor)

/**
 * Scans a log, runs processors and constructs a list
 * of [Mistake]s.
 *
 * If a processor has the [CancelOthers][LogProcessorOption.CancelOthers] option,
 * other processors will not be executed after it.
 *
 * @param text The log to process.
 *
 * @return A list of [Mistake]s that are present in the log.
 *
 * @see LogProcessorWithOptions
 * @see Log
 * @see Mistake
 */
fun mistakesFromLog(text: String) = mistakesFromLog(Log(sourceFromLog(text), text))

/**
 * Scans a log, runs processors and constructs a list
 * of [Mistake]s.
 *
 * If a processor has the [CancelOthers][LogProcessorOption.CancelOthers] option,
 * other processors will not be executed after it.
 *
 * @param log The log to process.
 *
 * @return A list of [Mistake]s that are present in the log.
 *
 * @see LogProcessorWithOptions
 * @see Log
 * @see Mistake
 */
fun mistakesFromLog(log: Log): List<Mistake> {
    val mistakes = mutableListOf<Mistake>()
    val ran = mutableListOf<String>()

    for ((id, processor) in processors) {
        var toContinue = false
        for (option in processor.options) {
            if (option is LogProcessorOption.CancelIfRan && ran.any(option.processors::contains)) toContinue =
                true // Double iteration :-)
        }
        if (toContinue) continue

        val mistake = processor(log) ?: continue
        mistakes.add(mistake)
        ran.add(id)
        if (processor.options.contains(LogProcessorOption.CancelOthers)) return mistakes
    }
    return mistakes
}
