package com.github.t1.yaml.parser

import com.github.t1.yaml.tools.spaces

internal class Nesting(private val next: YamlScanner) {
    private var level: Int = 0
    private var skipNext: Boolean = false

    override fun toString() = "Nesting:" + level + if (skipNext) " skip next" else ""

    fun up() {
        level++
    }

    fun down() {
        level--
    }

    fun expect() {
        next.expect(indent())
        skipNext = false
    }

    fun accept(): Boolean {
        val accepted = next.accept(indent())
        if (accepted)
            skipNext = false
        return accepted
    }

    private fun indent(): String = if (skipNext) "" else spaces(level * 2)

    fun skipNext(skipNext: Boolean): Nesting {
        this.skipNext = skipNext
        return this
    }
}
