package io.github.ytg1234.backgroundcatkotlin.util

import io.github.ytg1234.backgroundcatkotlin.LogSource
import io.github.ytg1234.backgroundcatkotlin.Mistake
import io.github.ytg1234.backgroundcatkotlin.Severity
import io.github.ytg1234.backgroundcatkotlin.withBlocking
import io.github.ytg1234.backgroundcatkotlin.withParser

fun setupDefaultParsers() {
    addBlocking()

    setupCommonErrors()
    setupUncommonErrors()
    setupModSpecificErrors()
    setupMultiMcSpecificErrors()
}

private fun setupCommonErrors() {
    withParser("fabric_api_missing") {
        if (contains("net.fabricmc.loader.discovery.ModResolutionException: Could not find required mod:") && contains("requires {fabric @")) {
            Mistake(
                Severity.Severe,
                "You are missing Fabric API, which is required by a mod. " +
                        "**[Download it here](https://www.curseforge.com/minecraft/mc-mods/fabric-api)**."
            )
        } else null
    }

    withParser("pixel_format_not_accelerated_win10") {
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

    withParser("out_of_memory_error") {
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

private fun setupUncommonErrors() {
    withParser("macos_too_new_java") {
        if (contains("Terminating app due to uncaught exception 'NSInternalInconsistencyException', reason: 'NSWindow drag regions should only be invalidated on the Main Thread!'")) {
            Mistake(
                Severity.Severe,
                "You are using too new a Java version. Please follow the steps on this wiki page to install 8u241: https://github.com/MultiMC/MultiMC5/wiki/Java-on-macOS"
            )
        } else null
    }

    withParser("id_range_exceeded") {
        if (contains("java.lang.RuntimeException: Invalid id 4096 - maximum id range exceeded.")) {
            Mistake(
                Severity.Severe,
                "You've exceeded the hardcoded ID Limit. Remove some mods, or install [this one](https://www.curseforge.com/minecraft/mc-mods/notenoughids)."
            )
        } else null
    }
}

private fun setupModSpecificErrors() { // as expected, only OptiFine is here
    withParser("shadermod_optifine_conflict") {
        if (contains("java.lang.RuntimeException: Shaders Mod detected. Please remove it, OptiFine has built-in support for shaders.")) {
            Mistake(
                Severity.Severe,
                "You've installed Shaders Mod alongside OptiFine. OptiFine has built-in shader support, so you should remove Shaders Mod."
            )
        } else null
    }
}

private fun setupMultiMcSpecificErrors() {
    withParser("server_java") {
        if (contains("-Bit Server VM warning")) {
            Mistake(
                Severity.Severe,
                "You're using the server version of Java. [See here for help installing the correct version.](https://github.com/MultiMC/MultiMC5/wiki/Using-the-right-Java)"
            )
        } else null
    }

    withParser("mmc_program_files") {
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

    withParser("java_architecture") {
        if (source == LogSource.MultiMc && contains("Your Java architecture is not matching your system architecture.")) {
            Mistake(
                Severity.Important,
                "You're using 32-bit Java. " +
                        "[See here for help installing the correct version.](https://github.com/MultiMC/MultiMC5/wiki/Using-the-right-Java)."
            )
        } else null
    }

    withParser("multimc_in_onedrive_managed_folder") {
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

    withParser("ram_amount") {
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

private fun addBlocking() {
    withBlocking("tlauncher") {
        val tlauncher_triggers = listOf(
            Regex("""Starting TLauncher \d+\.\d+"""),
            Regex("""\[Launcher] Running under TLauncher \d+\.\d+""")
        )
        if (tlauncher_triggers.stream().anyMatch(this::contains)) {
            Mistake(
                Severity.Illegal,
                "You are using TLauncher, which is illegal and breaks the Discord TOS. Sorry, we can't help you.\n" +
                        "You can buy Minecraft from the [official website](https://minecraft.net/)."
            )
        } else null
    }

    withBlocking("hacks") {
        val hacks = listOf(
            "wurst",
            "meteor-client"
        )

        if (hacks.stream().anyMatch { contains(Regex("""\[FabricLoader] Loading \d+ mods:.+$it@.+""")) }) {
            Mistake(
                Severity.Illegal,
                "You are using a hacked client, which breaks the Discord TOS. Sorry, we can't help you."
            )
        } else null
    }
}
