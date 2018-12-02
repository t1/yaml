@file:Suppress("ObjectPropertyName")

package com.github.t1.yaml.parser

import com.github.t1.codepoint.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Match
import com.github.t1.yaml.tools.Token
import com.github.t1.yaml.tools.token
import com.github.t1.yaml.tools.whitespace

val INDENTED_COMMENT = token("indented comment") { reader ->
    val spaces = reader.readWhile(`s-space`)
    return@token if (`c-comment`.match(reader).matches) Match(true, CodePoint.allOf("$spaces#")) else Match(false)
}

private fun CodePointReader.readWhile(token: Token): List<CodePoint> = this.readWhile {
    val match = token.match(this)
    if (match.matches) match.codePoints else listOf()
}

private fun CodePointReader.readUntil(token: Token): List<CodePoint> = this.readUntil {
    val match = token.match(this)
    if (match.matches) match.codePoints else listOf()
}

val BLOCK_SEQUENCE_START = `c-sequence-entry` + whitespace named "BLOCK_SEQUENCE_START"

val BLOCK_MAPPING_START = token("BLOCK_MAPPING_START") { reader ->
    val mappingKey = reader.mark { `c-mapping-key`.match(reader) } // TODO this is bullshit
    if (mappingKey.matches)
        return@token mappingKey
    val string = reader.mark { reader.readUntil(BLOCK_MAPPING_VALUE) }
    return@token if (string.isEmpty() || string.contains(CodePoint.of("\n")))
        Match(false)
    else
        Match(true, string)
}

val BLOCK_MAPPING_VALUE = `c-mapping-value` + whitespace named "BLOCK_MAPPING_VALUE"
