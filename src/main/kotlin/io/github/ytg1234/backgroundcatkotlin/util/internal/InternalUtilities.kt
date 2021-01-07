package io.github.ytg1234.backgroundcatkotlin.util.internal

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.Feature
import com.uchuhimo.konf.source.toml
import io.github.ytg1234.backgroundcatkotlin.config.ParserSpec
import mu.KotlinLogging
import java.io.File

val logger = KotlinLogging.logger("Background Cat")

object ConfigHandler {
    var config = Config {
        addSpec(ParserSpec)
    }
        .from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.resource("backgroundcat/default.toml")
        .from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.resource("backgroundcat/config.toml", optional = true)
    private set

    init {
        if (File("config/ext/backgroundcat.toml").exists()) {
            config = config.from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.watchFile("config/ext/backgroundcat.toml", optional = true)
        }

        config = config
            .from.prefixed("BACKGROUNDCAT").env()
            .from.prefixed("backgroundcat").systemProperties()
    }

    fun isParserEnabled(id: String): Boolean {
        return !config[ParserSpec.disabled].contains(id)
    }
}
