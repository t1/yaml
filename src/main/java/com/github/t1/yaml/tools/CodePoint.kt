package com.github.t1.yaml.tools

import java.lang.Character.getName
import java.lang.Character.isDigit
import java.lang.Character.toChars
import java.util.function.Predicate
import java.util.stream.Stream

data class CodePoint(val value: Int) {
    val isHex: Boolean
        get() = isDigit(value)
            || value >= 'A'.toInt() && value <= 'F'.toInt()
            || value >= 'a'.toInt() && value <= 'f'.toInt()

    val isEof: Boolean get() = value < 0

    override fun toString(): String = if (value < 0) "" else String(toChars())

    private fun toChars(): CharArray = toChars(value)

    fun xinfo(): String = "[" + escaped() + "][" + info() + "][0x" + hex() + "]"

    fun info(): String = when {
        value < 0 -> "EOF"
        value == 0 -> "NULL"
        else -> getName(value) ?: "?"
    }

    private fun escaped(): String =
        when (value.toChar()) {
            '\t' -> "\\t"
            '\n' -> "\\n"
            '\r' -> "\\r"
            else -> toString()
        }

    fun hex(): String = Integer.toHexString(value)

    fun `is`(predicate: Predicate<Int>): Boolean = predicate.test(value)

    fun appendTo(out: StringBuilder) {
        out.appendCodePoint(value)
    }

    companion object {
        val EOF = of(-1)

        fun at(index: Int, string: String): CodePoint = of(string.codePointAt(index))

        fun count(string: String): Int = string.codePointCount(0, string.length)

        fun decode(text: String): CodePoint = of(Integer.decode(text)!!)

        fun of(codePoint: Int): CodePoint = CodePoint(codePoint)

        fun stream(string: String): Stream<CodePoint> = string.codePoints().mapToObj { CodePoint(it) }

        fun of(text: String): CodePoint {
            assert(count(text) == 1)
            return at(0, text)
        }
    }
}
