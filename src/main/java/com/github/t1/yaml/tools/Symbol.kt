package com.github.t1.yaml.tools

val LF = symbol('\n')
val CR = symbol('\r')
val NL = LF.or(CR)
val WS = symbol("whitespace") { Character.isWhitespace(it.value) }
val SPACE = symbol("space-char") { Character.isSpaceChar(it.value) }

fun symbol(char: Char): Symbol = symbol(CodePoint.of(char).info) { it.value == char.toInt() }

fun symbol(
    description: String,
    predicate: (CodePoint) -> Boolean
): Symbol = object : Symbol {
    override val predicate: (CodePoint) -> Boolean
        get() = object : (CodePoint) -> Boolean {
            override fun invoke(codePoint: CodePoint) = predicate(codePoint)
            override fun toString(): String = description
        }

    override fun toString() = "<$predicate>"
}

/** A single-character [Token] */
interface Symbol : Token {
    val predicate: (CodePoint) -> Boolean

    override val predicates: List<(CodePoint) -> Boolean> get() = listOf(predicate)

    fun or(that: Symbol): Symbol = symbol("${this.predicate} or ${that.predicate}") { it(this) || it(that) }

    operator fun minus(that: Symbol): Symbol = symbol("${this.predicate} minus ${that.predicate}") { it(this) && !it(that) }

    operator fun not(): Symbol = symbol("not ${this.predicate}") { it(this) }

    companion object {
        fun between(min: Char, max: Char): Symbol = between(min.toInt(), max.toInt())

        fun between(min: Int, max: Int): Symbol = symbol("between $min and $max") { it.value in min..max }
    }
}
