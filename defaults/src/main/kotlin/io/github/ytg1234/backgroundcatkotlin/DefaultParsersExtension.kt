package io.github.ytg1234.backgroundcatkotlin

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.gateway.ReadyEvent
import io.github.ytg1234.backgroundcatkotlin.util.setupDefaultParsers

class DefaultParsersExtension(bot: ExtensibleBot, val multiMc: Boolean, val nonFabric: Boolean, val fabric: Boolean) : Extension(bot) {
    override val name: String = "backgroundcat-defaults"

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                setupDefaultParsers(multiMc, nonFabric, fabric)
            }
        }
    }
}
