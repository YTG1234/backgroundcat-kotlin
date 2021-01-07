package io.github.ytg1234.backgroundcatkotlin.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.ytg1234.backgroundcatkotlin.BackgroundCatExtension

fun ExtensibleBot.backgroundcatExt() {
    addExtension(::BackgroundCatExtension)
}
