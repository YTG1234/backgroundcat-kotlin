package io.github.ytg1234.kordextbackgroundcat.util

import io.github.ytg1234.kordextbackgroundcat.util.log.LogProcessorOption
import io.github.ytg1234.kordextbackgroundcat.util.log.LogSource
import io.github.ytg1234.kordextbackgroundcat.util.log.Mistake
import io.github.ytg1234.kordextbackgroundcat.util.log.Severity
import io.github.ytg1234.kordextbackgroundcat.withProcessor

/**
 * Registers processors based on the options provided.
 *
 * @param multiMc register MultiMC-specific processors (e.g. `server_java`)
 * @param nonFabric register processors that detect errors that cannot happen in a FabricMC environment
 * @param fabric register processors that detect error that can *only* happen in a FabricMC environment
 *
 * @author YTG1234
 */
fun setupDefaultProcessors(multiMc: Boolean, nonFabric: Boolean, fabric: Boolean) {
    addCancelling(fabric)

    setupCommonErrors(fabric)
    setupUncommonErrors(nonFabric)
    setupModSpecificErrors(nonFabric)
    if (multiMc) setupMultiMcSpecificErrors()

    if (fabric) setupLater()
}

/**
 * Registers the errors that are the most common to happen.
 *
 * @param fabric register FabricMC-specific processors
 */
private fun setupCommonErrors(fabric: Boolean) {
    if (fabric) {
        withProcessor("fabric_api_missing") {
            if (contains("net.fabricmc.loader.discovery.ModResolutionException: Could not find required mod:") && contains(
                    "requires {fabric @"
                )
            ) {
                Mistake(
                    Severity.Severe,
                    "You are missing Fabric API, which is required by a mod. " +
                            "**[Download it here](https://www.curseforge.com/minecraft/mc-mods/fabric-api)**."
                )
            } else null
        }
    }

    withProcessor("pixel_format_not_accelerated_win10") {
        if (contains("org.lwjgl.LWJGLException: Pixel format not accelerated") &&
            contains("Operating System: Windows 10")
        ) {
            Mistake(
                Severity.Important,
                "You seem to be using an Intel GPU that is not supported on Windows 10." +
                        "**You will need to install an older version of Java, [see here for help](https://github.com/MultiMC/MultiMC5/wiki/Unsupported-Intel-GPUs)**."
            )
        } else null
    }

    withProcessor("out_of_memory_error") {
        if (contains(Regex("java.lang.OutOfMemory(Error|Exception)"))) {
            Mistake(
                Severity.Severe,
                "You've run out of memory. You should allocate more, although the exact value depends on how many mods you have installed. ${
                    if (source == LogSource.MultiMc) { // When you know Kotlin
                        "[Click this link for a guide](https://cdn.discordapp.com/attachments/531598137790562305/575376840173027330/unknown.png)."
                    } else ""
                }"
            )
        } else null
    }
}

/**
 * Registers uncommon errors that people don't usually get.
 *
 * @param nonFabric register processors that will never trigger in a FabricMC environment
 */
private fun setupUncommonErrors(nonFabric: Boolean) {
    if (nonFabric) {
        withProcessor("macos_too_new_java") {
            if (contains("Terminating app due to uncaught exception 'NSInternalInconsistencyException', reason: 'NSWindow drag regions should only be invalidated on the Main Thread!'")) {
                Mistake(
                    Severity.Severe,
                    "You are using too new a Java version. Please follow the steps on this wiki page to install 8u241: https://github.com/MultiMC/MultiMC5/wiki/Java-on-macOS"
                )
            } else null
        }

        withProcessor("id_range_exceeded") {
            if (contains("java.lang.RuntimeException: Invalid id 4096 - maximum id range exceeded.")) {
                Mistake(
                    Severity.Severe,
                    "You've exceeded the hardcoded ID Limit. Remove some mods, or install [this one](https://www.curseforge.com/minecraft/mc-mods/notenoughids)."
                )
            } else null
        }
    }
}

/**
 * Registers processors that trigger when an error occurs involving a known, specific mod.
 *
 * @param nonFabric register processors that cannot trigger in a FabricMC environment (e.g. Shaders Mod)
 */
private fun setupModSpecificErrors(nonFabric: Boolean) {
    if (nonFabric) {
        withProcessor("shadermod_optifine_conflict") {
            if (contains("java.lang.RuntimeException: Shaders Mod detected. Please remove it, OptiFine has built-in support for shaders.")) {
                Mistake(
                    Severity.Severe,
                    "You've installed Shaders Mod alongside OptiFine. OptiFine has built-in shader support, so you should remove Shaders Mod."
                )
            } else null
        }
    }

    withProcessor("malilib") {
        if (contains("net.fabricmc.loader.discovery.ModResolutionException: Could not find required mod:") && contains("requires {malilib @")) {
            Mistake(
                Severity.Severe,
                """
                    |Litematica, Item Scroller, MiniHUD, and Tweakeroo all require MaLiLib to run. If your game is crashing on launch and you have any of those mods but not MaLiLib, you need it.
                    |You can download MaLiLib from [here](https://www.curseforge.com/minecraft/mc-mods/malilib).
                """.trimMargin("|")
            )
        } else null
    }
}

/**
 * Registers processors that can only trigger when the running launcher is [MultiMC](https://multimc.org/).
 */
private fun setupMultiMcSpecificErrors() {
    withProcessor("server_java") {
        if (contains("-Bit Server VM warning")) {
            Mistake(
                Severity.Severe,
                "You're using the server version of Java. [See here for help installing the correct version.](https://github.com/MultiMC/MultiMC5/wiki/Using-the-right-Java)"
            )
        } else null
    }

    withProcessor("mmc_program_files") {
        if (source == LogSource.MultiMc && contains(Regex("Minecraft folder is:\r?\nC:/Program Files"))) {
            Mistake(
                Severity.Severe,
                """
                            |Your MultiMC installation is in Program Files, where MultiMC doesn't have permission to write.
                            |**Move it somewhere else, like your Desktop.**
                        """.trimMargin("|")
            )
        } else null
    }

    withProcessor("java_architecture") {
        if (source == LogSource.MultiMc && contains("Your Java architecture is not matching your system architecture.")) {
            Mistake(
                Severity.Important,
                "You're using 32-bit Java. " +
                        "[See here for help installing the correct version.](https://github.com/MultiMC/MultiMC5/wiki/Using-the-right-Java)."
            )
        } else null
    }

    withProcessor("multimc_in_onedrive_managed_folder") {
        if (source == LogSource.MultiMc && contains(Regex("Minecraft folder is:\r?\nC:/.+/.+/OneDrive"))) {
            Mistake(
                Severity.Important,
                """
                    |MultiMC is located in a folder managed by OneDrive. OneDrive messes with Minecraft folders while the game is running, and this often leads to crashes.
                    |You should move the MultiMC folder to a different folder.
                """.trimMargin()
            )
        } else null
    }

    withProcessor("ram_amount") {
        if (source == LogSource.MultiMc && contains(Regex("-Xmx([0-9]+)m[,\\]]"))) {
            val match = Regex("-Xmx([0-9]+)m[,\\]]").find(text)
            val amount = match!!.groupValues[1].toInt() / 1000.0
            if (amount > 10.0) Mistake(
                Severity.Warn,
                "You have allocated ${amount}GB of RAM to Minecraft. [This is too much and can cause lagspikes](https://vazkii.net/#blog/ram-explanation)." // <-- MCP Names
            ) else null
        } else null
    }
}

/**
 * Registers processors that will cancel other processors from running after them.
 *
 * This should be called first.
 *
 * @param fabric register FabricMC-specific processors
 */
private fun addCancelling(fabric: Boolean) {
    withProcessor("tlauncher", LogProcessorOption.CancelOthers) {
        val tLauncherTriggers = listOf(
            Regex("""Starting TLauncher \d+\.\d+"""),
            Regex("""\[Launcher] Running under TLauncher \d+\.\d+""")
        )
        if (tLauncherTriggers.any(this::contains)) {
            Mistake(
                Severity.NoSupport,
                "You are using TLauncher, which is illegal and breaks the Discord TOS. Sorry, we can't help you.\n" +
                        "You can buy Minecraft from the [official website](https://minecraft.net/)."
            )
        } else null
    }

    if (fabric) {
        withProcessor("hacks", LogProcessorOption.CancelOthers) {
            val hacks = listOf(
                "wurst",
                "meteor-client",
                "inertia",
                "ares"
            )

            if (hacks.any { contains(Regex("""\[FabricLoader] Loading \d+ mods:.+$it@.+""")) }) {
                Mistake(
                    Severity.NoSupport,
                    "You are using a hacked client, which breaks the Discord TOS. Sorry, we can't help you."
                )
            } else null
        }
    }
}

/**
 * Registers other processors not covered by previous methods.
 */
private fun setupLater() {
    withProcessor("dependency", LogProcessorOption.CancelIfRan("fabric_api_missing", "malilib")) {
        val regex = Regex("""requires \{([a-zA-Z0-9_-]+) @ \[(.+)]}""")

        if (contains("net.fabricmc.loader.discovery.ModResolutionException: Could not find required mod:") && contains(regex)) {
            val match = regex.find(this)!!
            val modid = match.groupValues[1]
            val version = match.groupValues[2]

            Mistake(
                Severity.Severe,
                "A mod that you are using requires a mod with id `$modid`, version `$version`."
            )
        } else null
    }
}
