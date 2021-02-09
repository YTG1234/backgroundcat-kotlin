package io.github.ytg1234.kordextbackgroundcat.util.log

/**
 * Represents how bad is a mistake.
 *
 * @param text The text that is in the bot's message for a mistake.
 */
enum class Severity(val text: String) {
    /**
     * A mistake with this severity will not be supported on Discord.
     *
     * Usually means that the user is using a hacked client or pirating
     * the game.
     */
    NoSupport("❌"),

    /**
     * A mistake with this severity MUST be fixed.
     */
    Severe("!!"),

    /**
     * A mistake with this severity *should* be fixed,
     * but doesn't have to be.
     */
    Important("❗"),

    /**
     * A mistake with this severity might cause problems,
     * but shouldn't cause any serious issues.
     */
    Warn("⚠")
}

/**
 * A mistake consists of a [Severity] and a message that will be
 * sent in Discord.
 *
 * @param severity How bad this mistake is.
 * @param message What should be displayed in Discord.
 */
data class Mistake(val severity: Severity, val message: String)
