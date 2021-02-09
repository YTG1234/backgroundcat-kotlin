package io.github.ytg1234.test.kordextbackgroundcat

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.ytg1234.kordextbackgroundcat.util.backgroundCatDefaults
import io.github.ytg1234.kordextbackgroundcat.util.backgroundcatExt
import java.io.File

val bot = ExtensibleBot(
    token = File("token.txt").readText(),
    prefix = "!",

    addSentryExtension = false
)

suspend fun main() {
    bot.backgroundcatExt()
    bot.backgroundCatDefaults(multiMc = true, nonFabric = true, fabric = true)
    bot.start()
}
