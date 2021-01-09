package io.github.ytg1234.backgroundcatkotlin.config

import com.uchuhimo.konf.ConfigSpec

object ProcessorSpec : ConfigSpec("processors") {
    val disabled by required<List<String>>()
}
