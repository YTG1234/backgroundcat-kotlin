package io.github.ytg1234.kordextbackgroundcat.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.ytg1234.kordextbackgroundcat.DefaultParsersExtension

/**
 * Adds the [DefaultParsersExtension] to the specified bot, with options for filtering processors.
 *
 * @receiver the bot to add the extension to
 *
 * @param multiMc add MultiMC-specific processors (e.g. `server_java`)
 * @param nonFabric add processors that detect errors that cannot happen in a FabricMC environment
 * @param fabric add processors that detect error that can *only* happen in a FabricMC environment
 *
 * @author YTG1234
 */
fun ExtensibleBot.backgroundCatDefaults(multiMc: Boolean = true, nonFabric: Boolean = true, fabric: Boolean = true) {
    addExtension { DefaultParsersExtension(it, multiMc, nonFabric, fabric) }
}
