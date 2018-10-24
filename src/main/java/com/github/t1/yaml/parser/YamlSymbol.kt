package com.github.t1.yaml.parser

import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.NL
import com.github.t1.yaml.tools.Symbol
import com.github.t1.yaml.tools.Symbol.Companion.between
import com.github.t1.yaml.tools.symbol

/**
 * For reference, the number and name of the projection in the spec is given in a comment
 */
enum class YamlSymbol(override val predicate: (CodePoint) -> Boolean) : Symbol {
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

    TAB('\t'), // 32 s-tab
    // WHITE(SPACE.or(TAB)),      // 33 s-white

    DEC(between('0', '9')),  // 35 ns-dec-digit
    HEX(DEC.or(between('A', 'F')).or(between('a', 'f'))), // 36 ns-hex-digit

    // BACKSLASH('\\'),           // 41 c-escape

    /////////////////////////// not in spec
    DOT('.'),
    NL_OR_COMMENT(NL.or(COMMENT)),
    FLOW_SEQUENCE_ITEM_END(FLOW_SEQUENCE_ENTRY.or(FLOW_SEQUENCE_END));

    @Suppress("unused")
    constructor(codePoint: Char) : this(symbol(codePoint))

    @Suppress("unused")
    constructor(symbol: Symbol) : this(symbol.predicate)
}
