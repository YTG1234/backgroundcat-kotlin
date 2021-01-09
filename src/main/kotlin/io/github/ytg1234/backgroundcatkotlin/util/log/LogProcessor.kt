package io.github.ytg1234.backgroundcatkotlin.util.log

fun interface LogProcessor {
    fun process(log: Log): Mistake?
    operator fun invoke(log: Log) = process(log)
}

interface LogProcessorWithOptions : LogProcessor {
    val options: Set<LogProcessorOption>

    companion object {
        @JvmStatic
        fun of(options: Set<LogProcessorOption>, process: Log.() -> Mistake?)  = LogProcessorWithOptionsImpl(options) { it.process() }
    }
}

class LogProcessorWithOptionsImpl(override val options: Set<LogProcessorOption>, private val delegate: LogProcessor) : LogProcessorWithOptions {
    override fun process(log: Log): Mistake? = delegate.process(log)
}

sealed class LogProcessorOption {
    data class CancelIfRan(val processors: Set<String>) : LogProcessorOption() {
        constructor(processor: String) : this(setOf(processor))
        constructor(vararg processors: String) : this(setOf(*processors))
    }

    object CancelOthers : LogProcessorOption()
}
