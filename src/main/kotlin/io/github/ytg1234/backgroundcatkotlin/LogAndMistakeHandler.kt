package io.github.ytg1234.backgroundcatkotlin

import io.github.ytg1234.backgroundcatkotlin.util.internal.ConfigHolder
import io.github.ytg1234.backgroundcatkotlin.util.internal.logger
import io.github.ytg1234.backgroundcatkotlin.util.log.Log
import io.github.ytg1234.backgroundcatkotlin.util.log.LogProcessorOption
import io.github.ytg1234.backgroundcatkotlin.util.log.LogProcessorWithOptions
import io.github.ytg1234.backgroundcatkotlin.util.log.LogSource
import io.github.ytg1234.backgroundcatkotlin.util.log.Mistake

private fun sourceFromLog(text: String): LogSource {
    return if (text.startsWith("MultiMC version")) LogSource.MultiMc
    else LogSource.NotMultiMc
}

val processors = mutableMapOf<String, LogProcessorWithOptions>()

fun addProcessor(id: String, logProcessor: LogProcessorWithOptions) {
    if (id == "") throw IllegalArgumentException("Tried to add a processor for empty ID!")
    if (processors[id] != null) throw IllegalArgumentException("Tried to add a processor for ID $id which was already added!")

    if (!ConfigHolder.isParserEnabled(id)) {
        logger.debug("Not adding processor with ID $id because it is not enabled.")
        return
    }

    processors[id] = logProcessor
}

fun withProcessor(id: String, options: Set<LogProcessorOption> = setOf(), parser: Log.() -> Mistake?) =
    addProcessor(id, LogProcessorWithOptions.of(options, parser))

fun withProcessor(id: String, option: LogProcessorOption, parser: Log.() -> Mistake?) =
    withProcessor(id, setOf(option), parser)

fun mistakesFromLog(text: String): List<Mistake> {
    val mistakes = mutableListOf<Mistake>()
    val source = sourceFromLog(text)
    val log = Log(source, text)

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
