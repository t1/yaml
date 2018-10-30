@file:Suppress("ObjectPropertyName")

package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.YamlTokens.`c-comment`
import com.github.t1.yaml.parser.YamlTokens.`c-mapping-key`
import com.github.t1.yaml.parser.YamlTokens.`c-mapping-value`
import com.github.t1.yaml.parser.YamlTokens.`c-sequence-entry`
import com.github.t1.yaml.parser.YamlTokens.`s-space`
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.Match
import com.github.t1.yaml.tools.Scanner
import com.github.t1.yaml.tools.Symbol
import com.github.t1.yaml.tools.Token

val WS = Symbol("whitespace") { it: CodePoint -> it.isWhitespace }

val INDENTED_COMMENT = object : Token {
    override fun toString() = "indented comment"
    override fun match(scanner: Scanner): Match {
        val spaces = scanner.peekWhile(`s-space`)
        return if (scanner.matchesAfter(spaces.length, `c-comment`)) Match(true, CodePoint.allOf("$spaces#")) else Match(false)
    }
}

val BLOCK_SEQUENCE_START = `c-sequence-entry` + WS describedAs "BLOCK_SEQUENCE_START"

val BLOCK_MAPPING_START = object : Token {
    override fun toString() = "BLOCK_MAPPING_START"
    override fun match(scanner: Scanner): Match {
        val mappingKey = scanner.match(`c-mapping-key`)
        if (mappingKey.matches)
            return mappingKey
        val string = scanner.peekUntil(BLOCK_MAPPING_VALUE)
        return if (string == null || string.isEmpty() || string.contains("\n"))
            Match(false)
        else
            Match(true, CodePoint.allOf(string))
    }
}

val BLOCK_MAPPING_VALUE = `c-mapping-value` + WS describedAs "BLOCK_MAPPING_VALUE"
