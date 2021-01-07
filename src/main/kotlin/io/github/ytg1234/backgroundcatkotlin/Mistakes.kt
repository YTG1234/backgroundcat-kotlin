package io.github.ytg1234.backgroundcatkotlin

import io.github.ytg1234.backgroundcatkotlin.LogSource.*
import io.github.ytg1234.backgroundcatkotlin.Severity.*

enum class Severity(val s: String) {
    Severe("!!"),
    Important("❗")
}

enum class LogSource {
    Unknown, MultiMc, Vanilla
}

data class Mistake(val severity: Severity, val message: String)

fun interface Parser {
    fun parse(log: String, source: LogSource): Mistake?
    operator fun invoke(log: String, source: LogSource) = parse(log, source)
}

val parsers = mutableSetOf<Parser>()

fun addParser(parser: Parser) {
    parsers.add(parser)
}

fun mistakesFromLog(log: String): List<Mistake> {
    val mistakes = mutableListOf<Mistake>()
    val source = getLogSource(log)
    for (parser in parsers) {
        val mistake = parser(log, source) ?: continue
        mistakes.add(mistake)
    }
    return mistakes
}

private fun getLogSource(log: String): LogSource {
    if (log.startsWith("MultiMC version")) return MultiMc
    return Unknown
}
