package io.github.ytg1234.backgroundcatkotlin.util.log

enum class Severity(val text: String) {
    NoSupport("❌"),
    Severe("!!"),
    Important("❗"),
    Warn("⚠")
}

data class Mistake(val severity: Severity, val message: String)
