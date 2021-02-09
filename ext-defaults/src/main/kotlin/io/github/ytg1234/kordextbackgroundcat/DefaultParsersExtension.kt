package io.github.ytg1234.kordextbackgroundcat

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.gateway.ReadyEvent
import io.github.ytg1234.kordextbackgroundcat.util.setupDefaultProcessors

/**
 * An extension that registers the default processors based on the options provided.
 *
 * @param multiMc register MultiMC-specific processors
 * @param nonFabric register processors that cannot possibly trigger in a FabricMC environment
 * @param fabric register processors that can only trigger in a Fabric environment
 *
 * @author YTG1234
 *
 * @see setupDefaultProcessors
 */
class DefaultParsersExtension(bot: ExtensibleBot, val multiMc: Boolean, val nonFabric: Boolean, val fabric: Boolean) : Extension(bot) {
    override val name: String = "backgroundcat-defaults"

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                setupDefaultProcessors(multiMc, nonFabric, fabric)
            }
        }
    }
}
