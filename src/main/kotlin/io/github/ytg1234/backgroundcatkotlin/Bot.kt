package io.github.ytg1234.backgroundcatkotlin

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.ytg1234.backgroundcatkotlin.ext.BackgroundCatExtension
import java.io.File

val bot = ExtensibleBot(
    token = File("token.txt").readText(),
    prefix = "!",

    addSentryExtension = false
)

suspend fun main() {
    bot.addExtension(::BackgroundCatExtension)
    bot.start()
}
