package io.github.ytg1234.backgroundcatkotlin

enum class Severity(val s: String) {
    Severe("!!"),
    Important("❗"),
    Warn("⚠")
}

enum class LogSource {
    MultiMc, NotMultiMc
}

private fun sourceFromLog(text: String): LogSource {
    return if (text.startsWith("MultiMC version")) LogSource.MultiMc
    else LogSource.NotMultiMc
}

data class Mistake(val severity: Severity, val message: String)

data class Log(val source: LogSource, val text: String) : CharSequence by text

fun interface Parser {
    fun parse(log: Log): Mistake?
    operator fun invoke(log: Log) = parse(log)
}

val parsers = mutableSetOf<Parser>()

fun addParser(parser: Parser) {
    parsers.add(parser)
}

fun withParser(parser: Log.() -> Mistake?) {
    addParser { it.parser() }
}

fun mistakesFromLog(text: String): List<Mistake> {
    val mistakes = mutableListOf<Mistake>()
    val source = sourceFromLog(text)
    val log = Log(source, text)

    for (parser in parsers) {
        val mistake = parser(log) ?: continue
        mistakes.add(mistake)
    }
    return mistakes
}
