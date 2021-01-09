package io.github.ytg1234.backgroundcatkotlin.util.log

data class Log(val source: LogSource, val text: String) : CharSequence by text

enum class LogSource {
    MultiMc, NotMultiMc
}
