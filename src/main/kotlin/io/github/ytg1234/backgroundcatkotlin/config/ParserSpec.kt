package io.github.ytg1234.backgroundcatkotlin.config

import com.uchuhimo.konf.ConfigSpec

object ParserSpec : ConfigSpec("parsers") {
    val disabled by required<List<String>>()
}
