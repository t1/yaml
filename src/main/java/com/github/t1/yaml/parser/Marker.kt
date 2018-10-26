package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.YamlTokens.`c-mapping-key`
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.SPACE
import com.github.t1.yaml.tools.Scanner
import com.github.t1.yaml.tools.Symbol
import com.github.t1.yaml.tools.Token
import com.github.t1.yaml.tools.WS
import com.github.t1.yaml.tools.symbol

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
            if (scanner.peek(`c-mapping-key`))
                return true
            val token = scanner.peekUntil(BLOCK_MAPPING_VALUE)
            return token != null && token.isNotEmpty() && !token.contains("\n")
        }
    },
    BLOCK_MAPPING_VALUE(COLON, WS);

    private val symbols: List<Symbol> = listOf(*symbols)
    override val predicates: List<(CodePoint) -> Boolean> = this.symbols.flatMap(Symbol::predicates)
}

private val MINUS = symbol('-') // TODO `c-sequence-entry`
private val COLON = symbol(':') // TODO `c-mapping-value`
val COMMENT = symbol('#') // TODO `c-comment`
