package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE
import com.github.t1.yaml.parser.Marker.BLOCK_SEQUENCE_START
import com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER
import com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER
import com.github.t1.yaml.parser.YamlSymbol.BOM
import com.github.t1.yaml.parser.YamlSymbol.COMMENT
import com.github.t1.yaml.parser.YamlSymbol.FLOW_MAPPING_START
import com.github.t1.yaml.parser.YamlSymbol.FLOW_SEQUENCE_START
import com.github.t1.yaml.parser.YamlSymbol.NL_OR_COMMENT
import com.github.t1.yaml.parser.YamlSymbol.QUESTION_MARK
import com.github.t1.yaml.parser.YamlSymbol.SPACE
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.Scanner
import java.io.Reader

internal class YamlScanner(lookAheadLimit: Int, reader: Reader) : Scanner(lookAheadLimit, reader) {

    override fun read(): CodePoint {
        val start = isStartOfFile // before the read
        val codePoint = super.read()
        if (!start && BOM.test(codePoint))
            throw YamlParseException("A BOM must not appear inside a document")
        return codePoint
    }

    val isIndentedComment: Boolean
        get() {
            val spaces = peekUntil(NL_OR_COMMENT)
            return (spaces != null
                && CodePoint.stream(spaces).allMatch(SPACE)
                && peekAfter(spaces.length)?.`is`(COMMENT) == true)
        }

    val isFlowSequence: Boolean get() = `is`(FLOW_SEQUENCE_START)

    val isBlockSequence: Boolean get() = `is`(BLOCK_SEQUENCE_START)

    val isFlowMapping: Boolean get() = `is`(FLOW_MAPPING_START)

    val isBlockMapping: Boolean
        get() {
            if (`is`(QUESTION_MARK))
                return true
            val token = peekUntil(BLOCK_MAPPING_VALUE)
            return token != null && token.isNotEmpty() && !token.contains("\n")
        }

    override fun more(): Boolean {
        return (anyMore()
            && !`is`(DOCUMENT_END_MARKER)
            && !`is`(DIRECTIVES_END_MARKER)) // of next document
    }

    fun anyMore(): Boolean = super.more()
}
