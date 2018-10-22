package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.Symbol.P.Companion.not
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.Token
import java.util.function.Predicate

/**
 * A single-character Token.
 * For reference, the number and name of the projection in the spec is given in a comment
 */
enum class Symbol(private val predicate: P) : Token, Predicate<CodePoint> {
    BOM('\uFEFF'), // 3 c-byte-order-mark
    MINUS('-'), // 4 c-sequence-entry
    QUESTION_MARK('?'), // 5 c-mapping-key
    COLON(':'), // 6 c-mapping-value
    FLOW_SEQUENCE_ENTRY(','), // 7 c-collect-entry
    FLOW_SEQUENCE_START('['), // 8 c-sequence-start
    FLOW_SEQUENCE_END(']'), // 9 c-sequence-end
    FLOW_MAPPING_START('{'), // 10 c-mapping-start
    // FLOW_MAPPING_END('}'),     // 11 c-mapping-end
    COMMENT('#'), // 12 c-comment

    SINGLE_QUOTE('\''), // 18 c-single-quote
    DOUBLE_QUOTE('\"'), // 19 c-double-quote
    PERCENT('%'), // 20 c-directive

    LF('\n'), // 24 b-line-feed
    CR('\r'), // 25 b-carriage-return
    NL(LF.or(CR)), // 26 b-char

    SPACE(' '), // 31 s-space
    TAB('\t'), // 32 s-tab
    // WHITE(SPACE.or(TAB)),      // 33 s-white

    // DEC(between('0', '9')),  // 35 ns-dec-digit
    // HEX(DEC.or(between('A', 'F')).or(between('a', 'f'))), // 36 ns-hex-digit

    // BACKSLASH('\\'),           // 41 c-escape

    /////////////////////////// not in spec
    DOT('.'),
    WS(Predicate<Int> { Character.isWhitespace(it) }),
    NL_OR_COMMENT(NL.or(COMMENT)),
    FLOW_SEQUENCE_ITEM_END(FLOW_SEQUENCE_ENTRY.or(FLOW_SEQUENCE_END));

    @Suppress("unused")
    constructor(codePoint: Char) : this(Predicate { it == codePoint.toInt() })

    constructor(predicate: Predicate<Int>) : this(P(predicate))

    override val predicates: List<Predicate<CodePoint>> get() = listOf<Predicate<CodePoint>>(this)

    private fun or(that: Symbol): P = or(that.predicate)

    private fun or(that: P): P = this.predicate.or(that)

    private operator fun minus(that: Symbol): P = minus(that.predicate)

    private operator fun minus(that: P): P = this.predicate.and(not(that))

    override fun test(codePoint: CodePoint): Boolean = predicate.test(codePoint.value)

    class P(private val predicate: Predicate<Int>) {
        fun test(integer: Int): Boolean = predicate.test(integer)

        @Suppress("unused")
        private fun or(that: Symbol): P = or(that.predicate)

        fun or(that: P): P = P(this.predicate.or(that.predicate))

        @Suppress("unused")
        private fun and(that: Symbol): P = and(that.predicate)

        fun and(that: P): P = P(this.predicate.and(that.predicate))

        private operator fun minus(symbol: Symbol): P = minus(symbol.predicate)

        private operator fun minus(that: P): P = and(not(that))

        companion object {
            fun not(symbol: Symbol): P = not(symbol.predicate)

            fun not(predicate: P): P = P(predicate.predicate.negate())

            @Suppress("unused")
            fun between(min: Int, max: Int): P = P(Predicate { it in min..max })
        }
    }
}
