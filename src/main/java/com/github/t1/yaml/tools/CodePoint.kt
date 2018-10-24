package com.github.t1.yaml.tools

import java.lang.Character.getName
import java.lang.Character.isDigit
import java.lang.Character.toChars

data class CodePoint(val value: Int) {
    val isHex: Boolean
        get() = isDigit(value)
            || value >= 'A'.toInt() && value <= 'F'.toInt()
            || value >= 'a'.toInt() && value <= 'f'.toInt()

    val isEof: Boolean get() = value < 0
    val isNl: Boolean get() = value == '\n'.toInt() || value == '\r'.toInt()

    override fun toString(): String = if (value < 0) "" else String(toChars())

    private fun toChars(): CharArray = toChars(value)

    val info get() = "[$escaped][$char][0x$hex]"

    private val escaped
        get() =
            when (value.toChar()) {
                '\t' -> "\\t"
                '\n' -> "\\n"
                '\r' -> "\\r"
                else -> toString()
            }

    private val char
        get() = when {
            value < 0 -> "EOF"
            value == 0 -> "NULL"
            else -> getName(value) ?: "?"
        }

    val hex: String get() = Integer.toHexString(value)

    operator fun invoke(symbol: Symbol): Boolean = symbol.predicate(this)

    fun appendTo(out: StringBuilder) {
        out.appendCodePoint(value)
    }

    companion object {
        val EOF = of(-1)

        fun decode(text: String): CodePoint = of(Integer.decode(text)!!)

        fun of(codePoint: Int): CodePoint = CodePoint(codePoint)

        fun of(text: String): CodePoint {
            assert(count(text) == 1)
            return at(0, text)
        }

        private fun count(string: String): Int = string.codePointCount(0, string.length)

        private fun at(index: Int, string: String): CodePoint = of(string.codePointAt(index))
    }
}
