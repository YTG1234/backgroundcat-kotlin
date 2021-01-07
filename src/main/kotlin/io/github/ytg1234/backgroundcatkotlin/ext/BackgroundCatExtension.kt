package io.github.ytg1234.backgroundcatkotlin.ext

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.Color
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import io.github.ytg1234.backgroundcatkotlin.LogSource
import io.github.ytg1234.backgroundcatkotlin.Mistake
import io.github.ytg1234.backgroundcatkotlin.Severity
import io.github.ytg1234.backgroundcatkotlin.withParser
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
            // region Common Errors
            withParser {
                if (contains("net.fabricmc.loader.discovery.ModResolutionException: Could not find required mod:") && contains("requires {fabric @")) {
                    Mistake(
                        Severity.Severe,
                        "You are missing Fabric API, which is required by a mod. " +
                                "**[Download it here](https://www.curseforge.com/minecraft/mc-mods/fabric-api)**."
                    )
                } else null
            }

            withParser {
                if (contains("org.lwjgl.LWJGLException: Pixel format not accelerated") &&
                    contains("Operating System: Windows 10")) {
                    Mistake(
                        Severity.Important,
                        "You seem to be using an Intel GPU that is not supported on Windows 10." +
                                "**You will need to install an older version of Java, [see here for help](https://github.com/MultiMC/MultiMC5/wiki/Unsupported-Intel-GPUs)**."
                    )
                } else null
            }

            withParser {
                if (contains(Regex("java.lang.OutOfMemory(Error|Exception)"))) {
                    Mistake(
                        Severity.Severe,
                        "You've run out of memory. You should allocate more, although the exact value depends on how many mods you have installed. ${
                            if (source == LogSource.MultiMc) { // When you know Kotlin
                                "[Click this link for a guide](https://cdn.discordapp.com/attachments/531598137790562305/575376840173027330/unknown.png)."
                            } else ""
                        }"
                    )
                } else null
            }
            // endregion

            // region Uncommon errors
            withParser {
                if (contains("Terminating app due to uncaught exception 'NSInternalInconsistencyException', reason: 'NSWindow drag regions should only be invalidated on the Main Thread!'")) {
                    Mistake(
                        Severity.Severe,
                        "You are using too new a Java version. Please follow the steps on this wiki page to install 8u241: https://github.com/MultiMC/MultiMC5/wiki/Java-on-macOS"
                    )
                } else null
            }

            withParser {
                if (contains("java.lang.RuntimeException: Invalid id 4096 - maximum id range exceeded.")) {
                    Mistake(
                        Severity.Severe,
                        "You've exceeded the hardcoded ID Limit. Remove some mods, or install [this one](https://www.curseforge.com/minecraft/mc-mods/notenoughids)."
                    )
                } else null
            }
            // endregion

            // region Mod-specific errors
            withParser {
                if (contains("java.lang.RuntimeException: Shaders Mod detected. Please remove it, OptiFine has built-in support for shaders.")) {
                    Mistake(
                        Severity.Severe,
                        "You've installed Shaders Mod alongside OptiFine. OptiFine has built-in shader support, so you should remove Shaders Mod."
                    )
                } else null
            }
            // endregion

            // region MultiMC-specific errors
            withParser {
                if (contains("-Bit Server VM warning")) {
                    Mistake(
                        Severity.Severe,
                        "You're using the server version of Java. [See here for help installing the correct version.](https://github.com/MultiMC/MultiMC5/wiki/Using-the-right-Java)"
                    )
                } else null
            }

            withParser {
                if (source == LogSource.MultiMc && contains(Regex("Minecraft folder is:\r?\nC:/Program Files"))) {
                    Mistake(
                        Severity.Severe,
                        """
                            |Your MultiMC installation is in Program Files, where MultiMC doesn't have permission to write.
                            |**Move it somewhere else, like your Desktop.**
                        """.trimMargin("|")
                    )
                } else null
            }

            withParser {
                if (source == LogSource.MultiMc && contains("Your Java architecture is not matching your system architecture.")) {
                    Mistake(
                        Severity.Important,
                        "You're using 32-bit Java. " +
                                "[See here for help installing the correct version.](https://github.com/MultiMC/MultiMC5/wiki/Using-the-right-Java)."
                    )
                } else null
            }

            withParser {
                if (source == LogSource.MultiMc && contains(Regex("Minecraft folder is:\r?\nC:/.+/.+/OneDrive"))) {
                    Mistake(
                        Severity.Important,
                        """
                            |MultiMC is located in a folder managed by OneDrive. OneDrive messes with Minecraft folders while the game is running, and this often leads to crashes.
                            |You should move the MultiMC folder to a different folder.
                        """.trimMargin()
                    )
                } else null
            }

            withParser {
                if (source == LogSource.MultiMc && contains(Regex("-Xmx([0-9]+)m[,\\]]"))) {
                    val match = Regex("-Xmx([0-9]+)m[,\\]]").find(text)
                    val amount = match!!.groupValues[1].toInt() / 1000.0
                    if (amount > 10.0) Mistake(
                        Severity.Warn,
                        "You have allocated ${amount}GB of RAM to Minecraft. [This is too much and can cause lagspikes](https://vazkii.net/#blog/ram-explanation)." // <-- MCP Names
                    ) else null
                } else null
            }
            // endregion
        }
    }
}
