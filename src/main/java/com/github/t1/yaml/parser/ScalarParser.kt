package com.github.t1.yaml.parser

import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Scalar.Style

import com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED
import com.github.t1.yaml.model.Scalar.Style.PLAIN
import com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED
import com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE
import com.github.t1.yaml.parser.Symbol.DOUBLE_QUOTE
import com.github.t1.yaml.parser.Symbol.NL
import com.github.t1.yaml.parser.Symbol.NL_OR_COMMENT
import com.github.t1.yaml.parser.Symbol.SINGLE_QUOTE
import com.github.t1.yaml.parser.Symbol.SPACE

internal class ScalarParser private constructor(private val indent: Int, private val next: YamlScanner, private val nesting: Nesting, style: Style) {

    private val scalar: Scalar = Scalar().style(style)

    fun scalar(): Scalar {
        val text = scanLine()
        scalar.line(Line().indent(indent).text(text))
        var lineContinue = next.accept(NL)
        while (lineContinue && next.more() && nesting.accept()) {
            if (next.isFlowSequence)
                throw YamlParseException("Expected a scalar node to continue with scalar values but found flow sequence $next")
            if (next.isBlockSequence)
                throw YamlParseException("Expected a scalar node to continue with scalar values but found block sequence $next")
            if (next.isFlowMapping)
                throw YamlParseException("Expected a scalar node to continue with scalar values but found flow mapping $next")
            if (next.isBlockMapping)
                throw YamlParseException("Expected a scalar node to continue with scalar values but found block mapping $next")
            // if (next.accept(COMMENT)) {
            //     scalar.line(new Line().text(""));
            //     comment(scalar);
            // } else {
            scalar.line(Line().indent(next.count(SPACE)).text(scanLine()))
            // }
            lineContinue = !next.accept(NL)
        }
        return scalar
    }

    private fun scanLine(): String {
        return when (scalar.style) {
            PLAIN -> scanPlain()
            SINGLE_QUOTED -> scanSingleQuoted()
            DOUBLE_QUOTED -> scanDoubleQuoted()
        }
    }

    private fun scanPlain(): String {
        val builder = StringBuilder()
        while (next.more() && !next.`is`(NL_OR_COMMENT) && !next.`is`(BLOCK_MAPPING_VALUE))
            builder.appendCodePoint(next.read().value)
        return builder.toString()
    }

    private fun scanSingleQuoted(): String {
        val out = StringBuilder()
        while (next.more() && !next.`is`(NL) && !next.accept(SINGLE_QUOTE))
            if (next.accept("''"))
                out.append(SINGLE_QUOTE)
            else
                out.appendCodePoint(next.read().value)
        return out.toString()
    }

    private fun scanDoubleQuoted(): String {
        val out = StringBuilder()
        while (next.more() && !next.`is`(DOUBLE_QUOTE)) {
            if (next.accept("\\"))
                out.append("\\")
            out.appendCodePoint(next.read().value)
        }
        if (next.more())
            next.expect(DOUBLE_QUOTE)
        return out.toString()
    }

    companion object {
        fun of(next: YamlScanner, nesting: Nesting): ScalarParser {
            val indent = next.count(SPACE)
            val style = recognize(next)
            return ScalarParser(indent, next, nesting, style)
        }

        private fun recognize(scanner: YamlScanner): Style {
            if (scanner.accept(SINGLE_QUOTE))
                return SINGLE_QUOTED
            return if (scanner.accept(DOUBLE_QUOTE)) DOUBLE_QUOTED else PLAIN
        }
    }
}
