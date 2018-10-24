package com.github.t1.yaml.tools

val LF = symbol('\n')
val CR = symbol('\r')
val NL = LF.or(CR)
val WS = symbol { Character.isWhitespace(it.value) }
val SPACE = symbol { Character.isSpaceChar(it.value) }

fun symbol(char: Char): Symbol = symbol { it.value == char.toInt() }
fun symbol(predicate: (CodePoint) -> Boolean): Symbol = object : Symbol {
    override val predicate: (CodePoint) -> Boolean get() = predicate
}

/** A single-character [Token] */
interface Symbol : Token {
    val predicate: (CodePoint) -> Boolean

    override val predicates: List<(CodePoint) -> Boolean> get() = listOf(predicate)

    fun or(that: Symbol): Symbol = or(that.predicate)
    fun or(that: (CodePoint) -> Boolean): Symbol = symbol { it(this) || that(it) }

    operator fun minus(that: Symbol): Symbol = symbol { it(this) && !it(that) }

    operator fun not(): Symbol = symbol { it(this) }

    companion object {
        fun between(min: Char, max: Char): Symbol = between(min.toInt(), max.toInt())

        fun between(min: Int, max: Int): Symbol = symbol { it.value in min..max }
    }
}
