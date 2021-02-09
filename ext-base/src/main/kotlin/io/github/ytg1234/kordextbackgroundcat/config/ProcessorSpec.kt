package io.github.ytg1234.kordextbackgroundcat.config

import com.uchuhimo.konf.ConfigSpec

object ProcessorSpec : ConfigSpec("processors") {
    val disabled by required<List<String>>()
    val failOnDuplicate by required<Boolean>()
}
