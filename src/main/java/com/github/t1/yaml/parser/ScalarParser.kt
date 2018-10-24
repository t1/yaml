package com.github.t1.yaml.parser

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Scalar.Style
import com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED
import com.github.t1.yaml.model.Scalar.Style.PLAIN
import com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED
import com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE
import com.github.t1.yaml.parser.YamlSymbol.COMMENT
import com.github.t1.yaml.parser.YamlSymbol.DOUBLE_QUOTE
import com.github.t1.yaml.parser.YamlSymbol.SINGLE_QUOTE
import com.github.t1.yaml.parser.YamlSymbol.SPACE
import com.github.t1.yaml.tools.NL
import com.github.t1.yaml.tools.spaces

internal class ScalarParser private constructor(
    private val indent: Int,
    private val next: YamlScanner,
    private val nesting: Nesting,
    style: Style
) {
    companion object {
        fun of(next: YamlScanner, nesting: Nesting): ScalarParser {
            val indent = next.count(SPACE)
            val style = recognize(next)
            return ScalarParser(indent, next, nesting, style)
        }

        private fun recognize(scanner: YamlScanner): Style = when {
            scanner.accept(SINGLE_QUOTE) -> SINGLE_QUOTED
            scanner.accept(DOUBLE_QUOTE) -> DOUBLE_QUOTED
            else -> PLAIN
        }
    }

    private val scalar: Scalar = Scalar().style(style)

    fun scalar(): Scalar {
        scalar.line(Line(indent = indent, text = text()))
        if (next.isIndentedComment) comment(scalar)
        if (scalar.style == PLAIN) morePlainLines()
        else next.accept(NL)
        return scalar
    }

    private fun text(): String {
        return when (scalar.style) {
            PLAIN -> plain()
            SINGLE_QUOTED -> singleQuoted()
            DOUBLE_QUOTED -> doubleQuoted()
        }
    }

    private fun plain(): String {
        val builder = StringBuilder()
        while (next.more()) {
            // spaces before a comment or a block mapping value are not part of string scalar
            val spaceCount = next.peekWhile(SPACE).length
            val then = next.peekAfter(spaceCount)
            if (then != null && COMMENT.test(then)) break
            val spaces = spaces(spaceCount)
            next.expect(spaces)
            if (next.`is`(COMMENT) || next.`is`(BLOCK_MAPPING_VALUE)) break
            builder.append(spaces)
            if (!next.more() || next.`is`(NL)) break
            builder.appendCodePoint(next.read().value)
        }
        return builder.toString()
    }

    private fun morePlainLines() {
        // after the NL, there has to be more and it has to be nested
        while (next.accept(NL) && next.more() && nesting.accept()) {
            if (next.isFlowSequence)
                throw YamlParseException("Expected a scalar node to continue with scalar values but found flow sequence $next")
            if (next.isBlockSequence)
                throw YamlParseException("Expected a scalar node to continue with scalar values but found block sequence $next")
            if (next.isFlowMapping)
                throw YamlParseException("Expected a scalar node to continue with scalar values but found flow mapping $next")
            if (next.isBlockMapping)
                throw YamlParseException("Expected a scalar node to continue with scalar values but found block mapping $next")
            scalar.line(Line().indent(next.count(SPACE)).text(plain()))
            if (next.isIndentedComment) comment(scalar)
        }
    }

    private fun singleQuoted(): String {
        val out = StringBuilder()
        while (next.more())
            if (next.accept(SINGLE_QUOTE))
                if (next.accept(SINGLE_QUOTE))
                    out.append("'")
                else
                    return out.toString()
            else
                out.appendCodePoint(next.read().value)
        throw YamlParseException("Expected a single quoted scalar to be closed at ${next.positionInfo}")
    }

    private fun doubleQuoted(): String {
        val out = StringBuilder()
        while (next.more()) {
            if (next.accept(DOUBLE_QUOTE))
                return out.toString()
            if (next.accept("\\"))
                out.append("\\")
            out.appendCodePoint(next.read().value)
        }
        throw YamlParseException("Expected a double quoted scalar to be closed at ${next.positionInfo}")
    }

    private fun comment(scalar: Scalar) {
        val indent = next.count(SPACE)
        next.expect(COMMENT).skip(SPACE)
        scalar.lastLine.comment(Comment(indent = indent, text = next.readUntil(NL)))
    }
}
