@file:Suppress("ObjectPropertyName")

package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.YamlTokens.`b-carriage-return`
import com.github.t1.yaml.parser.YamlTokens.`b-line-feed`
import com.github.t1.yaml.parser.YamlTokens.`c-mapping-key`
import com.github.t1.yaml.parser.YamlTokens.`c-mapping-value`
import com.github.t1.yaml.parser.YamlTokens.`c-sequence-entry`
import com.github.t1.yaml.parser.YamlTokens.`s-space`
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.Scanner
import com.github.t1.yaml.tools.Token
import com.github.t1.yaml.tools.symbol

val `b-break` = `b-carriage-return` or `b-line-feed`
val WS = symbol("whitespace") { it.isWhitespace }

/** A Token composed of multiple Tokens  */
enum class Marker(vararg tokens: Token) : Token {
    INDENTED_COMMENT() {
        override fun matches(scanner: Scanner): Boolean {
            val spaces = scanner.peekWhile(`s-space`).length
            val then = scanner.peekAfter(spaces)
            return then?.invoke(COMMENT) == true
        }
    },
    BLOCK_SEQUENCE_START(`c-sequence-entry`, WS),
    BLOCK_MAPPING_START() {
        override fun matches(scanner: Scanner): Boolean {
            if (scanner.peek(`c-mapping-key`))
                return true
            val token = scanner.peekUntil(BLOCK_MAPPING_VALUE)
            return token != null && token.isNotEmpty() && !token.contains("\n")
        }
    },
    BLOCK_MAPPING_VALUE(`c-mapping-value`, WS);

    private val tokens: List<Token> = listOf(*tokens)
    override val predicates: List<(CodePoint) -> Boolean> = this.tokens.flatMap(Token::predicates)
}

val COMMENT = symbol('#') // TODO `c-comment`
