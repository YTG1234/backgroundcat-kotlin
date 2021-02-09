package io.github.ytg1234.kordextbackgroundcat.util.internal

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.Feature
import com.uchuhimo.konf.source.toml
import io.github.ytg1234.kordextbackgroundcat.config.ProcessorSpec
import mu.KotlinLogging
import java.io.File

/**
 * @suppress
 */
internal val logger = KotlinLogging.logger("Background Cat")

/**
 * @suppress
 */
internal object ConfigHolder {
    var config = Config {
        addSpec(ProcessorSpec)
    }
        .from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.resource("backgroundcat/default.toml")
        .from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.resource("backgroundcat/config.toml", optional = true)
        private set

    init {
        if (File("config/ext/backgroundcat.toml").exists()) {
            config = config.from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.watchFile(
                "config/ext/backgroundcat.toml",
                optional = true
            )
        }

        config = config
            .from.prefixed("BACKGROUNDCAT").env()
            .from.prefixed("backgroundcat").systemProperties()
    }

    fun isProcessorEnabled(id: String) = !config[ProcessorSpec.disabled].contains(id)

    val failOnDuplicate get() = config[ProcessorSpec.failOnDuplicate]
}
