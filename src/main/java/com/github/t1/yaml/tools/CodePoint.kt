package com.github.t1.yaml.tools

import kotlin.streams.asSequence

data class CodePoint(val value: Int) : Comparable<CodePoint> {
    override fun compareTo(other: CodePoint) = this.value.compareTo(other.value)

    override fun toString(): String = if (value < 0) "" else String(Character.toChars(value))

    val info get() = "[$escaped][$name][0x$hex]"

    val escaped
        get() = when {
            value == '\t'.toInt() -> "\\t"
            value == '\n'.toInt() -> "\\n"
            value == '\r'.toInt() -> "\\r"
            value == '\''.toInt() -> "\\'"
            value == '\\'.toInt() -> "\\\\"
            isEof -> "-1"
            isBom || isNel || isBig -> unicodeEscape
            isBmpCodePoint -> toString()
            else -> unicodeEscape
        }

    private val unicodeEscape get() = if (isBig) "\\u${pad(HEX(highSurrogate))}\\u${pad(HEX(lowSurrogate))}" else "\\u${pad(HEX)}"

    val isBig get() = value > 0xffff
    private val lowSurrogate get() = Character.lowSurrogate(value)
    private val highSurrogate get() = Character.highSurrogate(value)

    private val name
        get() = when {
            value < 0 -> "EOF"
            value == 0 -> "NULL"
            else -> Character.getName(value) ?: "?"
        }

    @Suppress("PrivatePropertyName")
    private val HEX: String
        get() = hex.toUpperCase()
    private val hex: String get() = Integer.toHexString(value)

    @Suppress("FunctionName")
    private fun HEX(char: Char) = Integer.toHexString(char.toInt()).toUpperCase()

    private fun pad(string: String) = "0000".substring(string.length) + string

    val isHex: Boolean
        get() = isDigit
            || value >= 'A'.toInt() && value <= 'F'.toInt()
            || value >= 'a'.toInt() && value <= 'f'.toInt()

    private val isDigit get() = Character.isDigit(value)
    private val isBmpCodePoint get () = Character.isBmpCodePoint(value)
    val isWhitespace get () = Character.isWhitespace(value)
    val isSpaceChar get () = Character.isSpaceChar(value)

    /** End Of File */
    val isEof: Boolean get() = value < 0

    /**
     * Byte Order Mark: If the first character of a file is a `ZERO WIDTH NO-BREAK SPACE`,
     * it is commonly used as an indicator of the byte-order in the file:
     * little endian vs. big endian */
    private val isBom get() = value == 0xFEFF

    /** Next Line */
    private val isNel get() = value == 0x0085

    /** New-Line */
    val isNl: Boolean get() = value == '\n'.toInt() || value == '\r'.toInt()

    fun appendTo(out: StringBuilder) {
        out.appendCodePoint(value)
    }

    operator fun rangeTo(that: CodePoint) = CodePointRange(this, that)

    companion object {
        val EOF = of(-1)

        fun of(codePoint: Char): CodePoint = of(codePoint.toInt())
        fun of(codePoint: Int): CodePoint = CodePoint(codePoint)
        fun of(string: String): CodePoint {
            require(string.codePointCount == 1) { "expected a string with one code point but found \"$this\"" }
            return of(string.codePointAt(0))
        }

        fun allOf(string: String): List<CodePoint> = string.codePoints().asSequence().map { operand -> CodePoint.of(operand) }.toList()

        fun decode(text: String): CodePoint = of(Integer.decode(text)!!)

        private val String.codePointCount get(): Int = this.codePointCount(0, length)
    }
}

data class CodePointRange(
    override val start: CodePoint,
    override val endInclusive: CodePoint
) : ClosedRange<CodePoint>

fun CharRange.toCodePointRange() = CodePoint.of(this.start)..CodePoint.of(this.endInclusive)
