package io.github.ytg1234.kordextbackgroundcat

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.Color
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import io.github.ytg1234.kordextbackgroundcat.util.internal.logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get

/**
 * The [Extension] itself, which controls the entire project.
 *
 * @constructor Calls the [Extension] constructor.
 *
 * @param bot The [ExtensibleBot] instance that this extension is installed to.
 */
class BackgroundCatExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name = "backgroundcat"

    /**
     * Used for keeping a list of valid sites where logs can
     * be found.
     *
     * @param regex The regex that allows to find a website link inside a string of text.
     */
    private enum class PasteSites(val regex: Regex) {
        PASTEEE(Regex("""https?://paste\.ee/p/[^\s/]+""", RegexOption.IGNORE_CASE)),
        HASTEBIN(Regex("""https?://has?tebin\.com/[^\s/]+""", RegexOption.IGNORE_CASE)),
        PASTEBIN(Regex("""https?://pastebin\.com/[^\s/]+""", RegexOption.IGNORE_CASE)),
        PASTEGG(Regex("""https?://paste\.gg/p/[^\s/]+/[^\s/]+""", RegexOption.IGNORE_CASE));
    }

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check { it.message.author != null && !it.message.author!!.isBot }
            action {
                var rawLink = ""

                PasteSites.values().forEach {
                    val link = it.regex.find(event.message.content)?.value ?: return@forEach
                    rawLink = pasteLinkToRaw(link, it)
                }

                if (rawLink == "") return@action
                logger.debug("Found a valid paste site link $rawLink, inspecting...")

                val client = HttpClient()

                val log = try {
                    String(client.get<ByteArray>(rawLink))
                } catch (t: Throwable) {
                    t.printStackTrace()
                    logger.error("Link $rawLink did not contain anything!")
                    return@action
                }

                val mistakes = mistakesFromLog(log)
                if (mistakes.isEmpty()) return@action
                logger.debug("Log contains mistakes")

                event.message.channel.createEmbed {
                    title = "Automated Response:"
                    color = Color(0x11806A)
                    mistakes.forEach {
                        field {
                            name = it.severity.text
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
        /**
         * Converts a paste site link to a raw link.
         *
         * @param link The website link.
         * @param site The paste site that [link] belongs to.
         */
        private fun pasteLinkToRaw(link: String, site: PasteSites): String {
            return when (site) {
                PasteSites.PASTEEE -> link.replaceFirst("/p/", "/r/")
                PasteSites.HASTEBIN, PasteSites.PASTEBIN -> link.replaceFirst(".com/", ".com/raw/")
                PasteSites.PASTEGG -> "$link/raw"
            }
        }
    }
}
