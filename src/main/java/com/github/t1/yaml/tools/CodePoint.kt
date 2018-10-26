package com.github.t1.yaml.tools

data class CodePoint(val value: Int) {
    override fun toString(): String = if (value < 0) "" else String(Character.toChars(value))

    val info get() = "[$escaped][$name][0x$hex]"

    val escaped
        get() = when {
            value == '\t'.toInt() -> "\\t"
            value == '\n'.toInt() -> "\\n"
            value == '\r'.toInt() -> "\\r"
            value == '\''.toInt() -> "\\'"
            value == '\\'.toInt() -> "\\\\"
            isBom || isNel -> "\\u$HEX"
            isBig -> "\\u${HEX(highSurrogate)}\\u${HEX(lowSurrogate)}"
            isBmpCodePoint -> toString()
            else -> "\\u$HEX"
        }

    private val isBig get() = value > 0xffff
    private val lowSurrogate get() = Character.lowSurrogate(value)
    private val highSurrogate get() = Character.highSurrogate(value)

    private val name
        get() = when {
            value < 0 -> "EOF"
            value == 0 -> "NULL"
            else -> Character.getName(value) ?: "?"
        }

    @Suppress("PrivatePropertyName")
    private val HEX: String get() = hex.toUpperCase()
    private val hex: String get() = Integer.toHexString(value)

    @Suppress("FunctionName")
    private fun HEX(char: Char) = Integer.toHexString(char.toInt()).toUpperCase()

    val isHex: Boolean
        get() = isDigit
            || value >= 'A'.toInt() && value <= 'F'.toInt()
            || value >= 'a'.toInt() && value <= 'f'.toInt()

    private val isDigit get() = Character.isDigit(value)
    private val isBmpCodePoint get () = Character.isBmpCodePoint(value)

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

    operator fun invoke(symbol: Symbol): Boolean = symbol.predicate(this)

    fun appendTo(out: StringBuilder) {
        out.appendCodePoint(value)
    }

    companion object {
        val EOF = of(-1)

        fun of(codePoint: Char): CodePoint = of(codePoint.toInt())
        fun of(codePoint: Int): CodePoint = CodePoint(codePoint)
        fun of(text: String): CodePoint {
            assert(count(text) == 1)
            return at(0, text)
        }

        fun decode(text: String): CodePoint = of(Integer.decode(text)!!)

        private fun count(string: String): Int = string.codePointCount(0, string.length)

        private fun at(index: Int, string: String): CodePoint = of(string.codePointAt(index))
    }
}
