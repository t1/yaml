package com.github.t1.yaml.parser

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Scalar.Style
import com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED
import com.github.t1.yaml.model.Scalar.Style.PLAIN
import com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED
import com.github.t1.yaml.parser.ScalarParser.Mode.KEY
import com.github.t1.yaml.parser.ScalarParser.Mode.NORMAL
import com.github.t1.yaml.parser.ScalarParser.Mode.VALUE
import com.github.t1.yaml.tools.CodePoint.Companion.EOF
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.spaces

internal class ScalarParser private constructor(
    private val indent: Int,
    private val next: YamlScanner,
    private val nesting: Nesting,
    private val mode: Mode,
    style: Style
) {
    enum class Mode { NORMAL, KEY, VALUE }

    companion object {
        fun of(next: YamlScanner, nesting: Nesting, mode: Mode = NORMAL): ScalarParser {
            val indent = next.count(`s-space`)
            val style = recognize(next)
            return ScalarParser(indent, next, nesting, mode, style)
        }

        private fun recognize(scanner: YamlScanner): Style = when {
            scanner.accept(`c-single-quote`) -> SINGLE_QUOTED
            scanner.accept(`c-double-quote`) -> DOUBLE_QUOTED
            else -> PLAIN
        }

        fun autoDetectIndentation(reader: CodePointReader) : Int {
            return 0 // TODO
        }
    }

    private val scalar: Scalar = Scalar().style(style)

    fun scalar(): Scalar {
        scalar.line(Line(indent = indent, text = text()))
        if (next.peek(INDENTED_COMMENT)) comment(scalar)
        if (scalar.style == PLAIN && mode == NORMAL) morePlainLines()
        else next.accept(`b-break`)
        return scalar
    }

    private fun text(): String = when (scalar.style) {
        PLAIN -> plain()
        SINGLE_QUOTED -> singleQuoted()
        DOUBLE_QUOTED -> doubleQuoted()
    }

    private fun plain(): String {
        val out = StringBuilder()
        while (next.more() && !next.peek(`b-break`)) {
            // spaces before a comment or a block mapping value are not part of string scalar
            val spaces = spaces(next.peekWhile(`s-space`).size)
            if (next.matchesAfter(spaces.length, `c-comment`)) break
            next.expect(spaces)
            if (next.peek(`c-comment`) || next.peek(BLOCK_MAPPING_VALUE)) break
            out.append(spaces)
            if (next.peek() == EOF) break
            if (mode == KEY && next.peek(`c-mapping-value`)) break
            if (mode == VALUE && (next.peek(`c-collect-entry`) || next.peek(`c-mapping-end`))) break
            next.read().appendTo(out)
        }
        return out.toString()
    }

    private fun morePlainLines() {
        // after the `b-break`, there has to be more and it has to be nested
        while (next.accept(`b-break`) && next.more() && nesting.accept()) {
            checkValidPlainScalarContinue()
            scalar.line(Line().indent(next.count(`s-space`)).text(plain()))
            if (next.peek(INDENTED_COMMENT)) comment(scalar)
        }
    }

    private fun checkValidPlainScalarContinue() {
        for (token in listOf(`c-sequence-start`, BLOCK_SEQUENCE_START, `c-mapping-start`, BLOCK_MAPPING_START))
            if (next.peek(token))
                throw YamlParseException("Expected a scalar node to continue with scalar values but found $token $next")
    }

    private fun singleQuoted(): String {
        val out = StringBuilder()
        while (next.more())
            if (next.accept(`c-single-quote`))
                if (next.accept(`c-single-quote`))
                    out.append("'")
                else
                    return out.toString()
            else
                next.read().appendTo(out)
        throw YamlParseException("Expected a single quoted scalar to be closed at ${next.positionInfo}")
    }

    private fun doubleQuoted(): String {
        val out = StringBuilder()
        while (next.more()) {
            if (next.accept(`c-double-quote`))
                return out.toString()
            if (next.accept("\\"))
                out.append("\\")
            next.read().appendTo(out)
        }
        throw YamlParseException("Expected a double quoted scalar to be closed at ${next.positionInfo}")
    }

    private fun comment(scalar: Scalar) {
        val indent = next.count(`s-space`)
        next.expect(`c-comment`).skip(`s-space`)
        scalar.lastLine.comment(Comment(indent = indent, text = next.readUntil(`b-break`)))
    }
}
