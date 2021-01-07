package io.github.ytg1234.test.backgroundcatkotlin

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.ytg1234.backgroundcatkotlin.util.backgroundcatExt
import java.io.File

val bot = ExtensibleBot(
    token = File("token.txt").readText(),
    prefix = "!",

    addSentryExtension = false
)

suspend fun main() {
    bot.backgroundcatExt()
    bot.start()
}
