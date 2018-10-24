package com.github.t1.yaml.tools

import com.github.t1.yaml.tools.Symbol.P.Companion.not
import java.util.function.Predicate

val LF = symbol('\n')
val CR = symbol('\r')
val NL = LF.or(CR)
val WS = symbol(Predicate { Character.isWhitespace(it) })

fun symbol(char: Char): Symbol = symbol(char.toPredicate)
fun symbol(predicate: Predicate<Int>): Symbol = symbol(Symbol.P(predicate))
fun symbol(predicate: Symbol.P): Symbol = predicate.toSymbol

/** A single-character [Token]. */
interface Symbol : Token, Predicate<CodePoint>, (CodePoint) -> Boolean {
    val predicate: P

    override val predicates: List<Predicate<CodePoint>> get() = listOf<Predicate<CodePoint>>(this)

    fun or(that: Symbol): Symbol = or(that.predicate).toSymbol

    fun or(that: P): P = this.predicate.or(that)

    operator fun minus(that: Symbol): P = minus(that.predicate)

    operator fun minus(that: P): P = this.predicate.and(not(that))

    override fun invoke(codePoint: CodePoint): Boolean = test(codePoint)
    override fun test(codePoint: CodePoint): Boolean = predicate.test(codePoint.value)

    class P(val predicate: Predicate<Int>) {
        val toSymbol
            get() = object : Symbol {
                override val predicate: Symbol.P get() = this@P
            }

        fun test(integer: Int): Boolean = predicate.test(integer)

        @Suppress("unused")
        fun or(that: Symbol): P = or(that.predicate)

        fun or(that: P): P = P(this.predicate.or(that.predicate))

        @Suppress("unused")
        fun and(that: Symbol): P = and(that.predicate)

        fun and(that: P): P = P(this.predicate.and(that.predicate))

        operator fun minus(symbol: Symbol): P = minus(symbol.predicate)

        operator fun minus(that: P): P = and(not(that))

        companion object {
            fun not(symbol: Symbol): P = not(symbol.predicate)

            fun not(predicate: P): P = P(predicate.predicate.negate())

            @Suppress("unused")
            fun between(min: Int, max: Int): P = P(Predicate { it in min..max })
        }
    }
}

val Char.toPredicate: Symbol.P get() = Symbol.P(Predicate { it == this.toInt() })
