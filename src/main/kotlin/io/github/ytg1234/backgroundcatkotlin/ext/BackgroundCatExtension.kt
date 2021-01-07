package io.github.ytg1234.backgroundcatkotlin.ext

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.Color
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import io.github.ytg1234.backgroundcatkotlin.LogSource
import io.github.ytg1234.backgroundcatkotlin.Mistake
import io.github.ytg1234.backgroundcatkotlin.Severity
import io.github.ytg1234.backgroundcatkotlin.addParser
import io.github.ytg1234.backgroundcatkotlin.mistakesFromLog
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class BackgroundCatExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name = "fabric"

    private enum class PasteSites(val regex: Regex) {
        PASTEEE(Regex("""https?://paste\.ee/p/[^\s/]+""", RegexOption.IGNORE_CASE)),
        HASTEBIN(Regex("""https?://has?tebin\.com/[^\s/]+""", RegexOption.IGNORE_CASE)),
        PASTEBIN(Regex("""https?://pastebin\.com/[^\s/]+""", RegexOption.IGNORE_CASE)),
        PASTEGG(Regex("""https?://paste\.gg/p/[^\s/]+/[^\s/]+""", RegexOption.IGNORE_CASE));
    }

    override suspend fun setup() {
        addParsers()

        event<MessageCreateEvent> {
            check { it.message.author != null && !it.message.author!!.isBot }
            action {
                var rawLink = ""
                PasteSites.values().forEach {
                    val link = it.regex.find(event.message.content)?.value ?: return@forEach
                    rawLink = pasteLinkToRaw(link, it)
                }
                if (rawLink == "") return@action

                val client = HttpClient()
                val log = try {
                    String(client.get<ByteArray>(rawLink))
                } catch (t: Throwable) {
                    t.printStackTrace()
                    return@action
                }

                val mistakes = mistakesFromLog(log)
                if (mistakes.isEmpty()) return@action
                event.message.channel.createEmbed {
                    title = "Automated Response:"
                    color = Color(0x11806A)
                    mistakes.forEach {
                        field {
                            name = it.severity.s
                            value = it.message
                            inline = true
                        }
                    }
                    footer { text = "This might not solve your problem, but it could be worth a try." }
                }
            }
        }
    }

    companion object {
        private fun pasteLinkToRaw(link: String, site: PasteSites): String {
            return when (site) {
                PasteSites.PASTEEE -> link.replaceFirst("/p/", "/r/")
                PasteSites.HASTEBIN, PasteSites.PASTEBIN -> link.replaceFirst(".com/", ".com/raw/")
                PasteSites.PASTEGG -> "$link/raw"
            }
        }
        private fun addParsers() {
            addParser { log, _ ->
                if (log.contains("net.fabricmc.loader.discovery.ModResolutionException: Could not find required mod:") && log.contains("requires {fabric @")) {
                    Mistake(
                        Severity.Severe,
                        "You are missing Fabric API, which is required by a mod. " +
                                "**[Download it here](https://www.curseforge.com/minecraft/mc-mods/fabric-api)**."
                    )
                } else null
            }

            addParser { log, _ ->
                if (log.contains("org.lwjgl.LWJGLException: Pixel format not accelerated") &&
                    log.contains("Operating System: Windows 10")) {
                    Mistake(
                        Severity.Severe,
                        "You seem to be using an Intel GPU that is not supported on Windows 10." +
                                "**You will need to install an older version of Java, [see here for help](https://github.com/MultiMC/MultiMC5/wiki/Unsupported-Intel-GPUs)**."
                    )
                } else null
            }

            addParser { log, source ->
                if (source == LogSource.MultiMc && log.contains("Your Java architecture is not matching your system architecture.")) {
                    Mistake(
                        Severity.Severe,
                        "You're using 32-bit Java. " +
                                "[See here for help installing the correct version.](https://github.com/MultiMC/MultiMC5/wiki/Using-the-right-Java)."
                    )
                } else null
            }

            addParser { log, source ->
                if (source == LogSource.MultiMc && Regex("Minecraft folder is:(\r?)\nC:(\\|/)Program Files").containsMatchIn(log)) {
                    Mistake(
                        Severity.Severe,
                        """
                    |Your MultiMC installation is in Program Files, where MultiMC doesn't have permission to write.
                    |**Move it somewhere else, like your Desktop.**
                """.trimMargin("|")
                    )
                } else null
            }
        }
    }
}
