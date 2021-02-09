package io.github.ytg1234.backgroundcatkotlin.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.ytg1234.backgroundcatkotlin.DefaultParsersExtension

fun ExtensibleBot.backgroundCatDefaults(multiMc: Boolean = true, nonFabric: Boolean = true, fabric: Boolean = true) {
    addExtension { DefaultParsersExtension(it, multiMc, nonFabric, fabric) }
}
