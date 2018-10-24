package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.YamlSymbol.COLON
import com.github.t1.yaml.parser.YamlSymbol.COMMENT
import com.github.t1.yaml.parser.YamlSymbol.DOT
import com.github.t1.yaml.parser.YamlSymbol.MINUS
import com.github.t1.yaml.parser.YamlSymbol.QUESTION_MARK
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.SPACE
import com.github.t1.yaml.tools.Scanner
import com.github.t1.yaml.tools.Symbol
import com.github.t1.yaml.tools.Token
import com.github.t1.yaml.tools.WS

/** A Token composed of multiple Symbols  */
enum class Marker(vararg symbols: Symbol) : Token {
    INDENTED_COMMENT() {
        override fun matches(scanner: Scanner): Boolean {
            val spaces = scanner.peekWhile(SPACE).length
            val then = scanner.peekAfter(spaces)
            return then?.invoke(COMMENT) == true
        }
    },
    BLOCK_SEQUENCE_START(MINUS, WS),
    BLOCK_MAPPING_START() {
        override fun matches(scanner: Scanner): Boolean {
            if (scanner.peek(QUESTION_MARK))
                return true
            val token = scanner.peekUntil(BLOCK_MAPPING_VALUE)
            return token != null && token.isNotEmpty() && !token.contains("\n")
        }
    },
    BLOCK_MAPPING_VALUE(COLON, WS),
    DIRECTIVES_END_MARKER(MINUS, MINUS, MINUS),
    DOCUMENT_END_MARKER(DOT, DOT, DOT);

    private val symbols: List<Symbol> = listOf(*symbols)
    override val predicates: List<(CodePoint) -> Boolean> = this.symbols.flatMap(Symbol::predicates)
}
