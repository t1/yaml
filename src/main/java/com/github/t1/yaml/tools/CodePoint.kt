package com.github.t1.yaml.tools

import kotlin.streams.asSequence

data class CodePoint(private val value: Int) : Comparable<CodePoint> {
    override fun compareTo(other: CodePoint) = this.value.compareTo(other.value)

    override fun toString(): String = if (value < 0) "" else String(Character.toChars(value))
    fun toInt() = value

    val info get() = "[$escaped][$name][0x$hex]"

    val escaped
        get() = when {
            value == '\t'.toInt() -> "\\t"
            value == '\n'.toInt() -> "\\n"
            value == '\r'.toInt() -> "\\r"
            value == '\''.toInt() -> "\\'"
            value == '\\'.toInt() -> "\\\\"
            this == EOF -> "-1"
            this == BOM || this == NEL || isSupplementary -> unicodeEscape
            isBmpCodePoint -> toString()
            else -> unicodeEscape
        }

    private val unicodeEscape get() = if (isSupplementary) "\\u${pad(HEX(highSurrogate))}\\u${pad(HEX(lowSurrogate))}" else "\\u${pad(HEX)}"

    val isSupplementary get() = Character.isSupplementaryCodePoint(value)
    private val lowSurrogate get() = Character.lowSurrogate(value)
    private val highSurrogate get() = Character.highSurrogate(value)

    private val name
        get() = when {
            this == EOF -> "EOF"
            value == 0 -> "NULL"
            else -> Character.getName(value) ?: "?"
        }

    @Suppress("PrivatePropertyName")
    private val HEX: String
        get() = hex.toUpperCase()
    private val hex: String get() = hex(value)

    private fun pad(string: String) = "0000".substring(string.length) + string

    val isHex: Boolean
        get() = isDigit
            || value >= 'A'.toInt() && value <= 'F'.toInt()
            || value >= 'a'.toInt() && value <= 'f'.toInt()

    private val isDigit get() = Character.isDigit(value)
    private val isBmpCodePoint get () = Character.isBmpCodePoint(value)
    val isWhitespace get () = Character.isWhitespace(value)
    val isAlphabetic get () = Character.isAlphabetic(value)
    val isSpaceChar get () = Character.isSpaceChar(value)

    /** New-Line */
    val isNl: Boolean get() = value == '\n'.toInt() || value == '\r'.toInt()

    fun appendTo(out: StringBuilder) = out.appendCodePoint(value)!!

    operator fun rangeTo(that: CodePoint) = CodePointRange(this, that)
    infix fun until(that: CodePoint) = CodePointRange(this, that - 1)

    operator fun minus(i: Int) = CodePoint.of(value - i)

    companion object {
        /** End Of File: Typically represented by a -1 integer */
        val EOF = of(-1)

        /**
         * Byte Order Mark: If the first character of a file is a `ZERO WIDTH NO-BREAK SPACE`,
         * it is commonly used as an indicator of the byte-order in the file:
         * little endian vs. big endian
         */
        val BOM = of(0xFEFF)

        /** Next Line */
        val NEL = of(0x0085)

        fun of(codePoint: Char): CodePoint = of(codePoint.toInt())
        fun of(codePoint: Int): CodePoint = CodePoint(codePoint)
        fun of(high: Char, low: Char): CodePoint {
            require(Character.isSurrogatePair(high, low)) { "expected surrogate pair but got ${hex(high)} ${hex(low)}" }
            return CodePoint(Character.toCodePoint(high, low))
        }

        fun of(string: String): CodePoint {
            require(string.codePointCount == 1) { "expected a string with one code point but got \"$string\"" }
            return of(string.codePointAt(0))
        }

        fun allOf(string: String): List<CodePoint> = string.codePoints().asSequence().map(Companion::of).toList()

        fun decode(text: String): CodePoint = of(Integer.decode(text)!!)

        private val String.codePointCount get(): Int = this.codePointCount(0, length)

        @Suppress("FunctionName")
        private fun HEX(char: Char) = hex(char).toUpperCase()

        private fun hex(char: Char) = hex(char.toInt())
        private fun hex(int: Int) = Integer.toHexString(int)
    }
}

data class CodePointRange(
    override val start: CodePoint,
    override val endInclusive: CodePoint
) : ClosedRange<CodePoint>

fun CharRange.toCodePointRange() = CodePoint.of(this.start)..CodePoint.of(this.endInclusive)
