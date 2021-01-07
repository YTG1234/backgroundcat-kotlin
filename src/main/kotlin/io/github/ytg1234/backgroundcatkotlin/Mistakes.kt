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

fun interface Parser {
    fun parse(log: String, source: LogSource): Mistake?
    operator fun invoke(log: String, source: LogSource) = parse(log, source)
}

val parsers = setOf(
    Parser { log, _ ->
        if (log.contains("net.fabricmc.loader.discovery.ModResolutionException: Could not find required mod:") && log.contains("requires {fabric @")) {
            Mistake(
                Severe,
                "You are missing Fabric API, which is required by a mod. " +
                        "**[Download it here](https://www.curseforge.com/minecraft/mc-mods/fabric-api)**."
            )
        } else null
    },
    Parser { log, _ ->
        if (log.contains("org.lwjgl.LWJGLException: Pixel format not accelerated") &&
            log.contains("Operating System: Windows 10")) {
            Mistake(
                Severe,
                "You seem to be using an Intel GPU that is not supported on Windows 10." +
                        "**You will need to install an older version of Java, [see here for help](https://github.com/MultiMC/MultiMC5/wiki/Unsupported-Intel-GPUs)**."
            )
        } else null
    },
    Parser { log, source ->
        if (source == MultiMc && log.contains("Your Java architecture is not matching your system architecture.")) {
            Mistake(
                Severe,
                "You're using 32-bit Java. " +
                        "[See here for help installing the correct version.](https://github.com/MultiMC/MultiMC5/wiki/Using-the-right-Java)."
            )
        } else null
    },
    Parser { log, source ->
        if (source == MultiMc && log.contains("Minecraft folder is:\nC:/Program Files")) {
            Mistake(
                Severe,
                """
                    |Your MultiMC installation is in Program Files, where MultiMC doesn't have permission to write.
                    |**Move it somewhere else, like your Desktop.**
                """.trimMargin("|")
            )
        } else null
    }
)
