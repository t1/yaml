package spec.generator

import com.github.t1.yaml.tools.CodePoint
import org.jsoup.nodes.Node
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.EqualsExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression
import spec.generator.Expression.VariableExpression
import java.util.Deque
import java.util.LinkedList

/**
 * A recursive descend parser for the grammar html snippets in the yaml spec.
 * Error messages are generally failed assertions only, so you need the source to see what happened.
 * Some errors in the grammar description are not detected... the result may not be valid.
 */
class NodeExpressionParser(nodes: List<Node>) {
    private val next: NodeExpressionScanner = NodeExpressionScanner(nodes)

    private var ends: Deque<() -> Boolean> = LinkedList<() -> Boolean>().apply { push { next.end() } }
    private val isEnd: Boolean get() = ends.peek().invoke()
    private val isBr: Boolean get() = next.isElement("br")
    private val isQuote: Boolean get() = next.peek("“")
    private val isHref: Boolean get() = next.isElement("a") && next.peekElement().hasAttr("href")
    private val isVar: Boolean get() = next.isElement("code") && next.peekElement().className() == "varname"
    private val isAlpha get() = next.peek().isAlphabetic
    private val isComment get() = next.peek("/*")

    private fun until(end: () -> Boolean): Expression {
        ends.push(end)
        val expression = expression()
        ends.pop()
        return expression
    }

    fun expression(): Expression {
        skipSpaces()
        val expression = body()
        skipSpaces()
        if (isEnd) return expression
        return continues(expression)
    }

    private fun skipSpaces() {
        while (next.accept(" "));
    }

    private fun body(): Expression {
        if (next.accept("("))
            return parentheses()
        if (next.accept("#x"))
            return hex()
        if (next.accept("[#x"))
            return range()
        if (isQuote)
            return quote()
        if (isHref)
            return ref()
        if (isVar)
            return variable()
        if (isAlpha)
            return plainRef()
        if (isComment)
            return commentRef()
        throw AssertionError("unexpected start $next")
    }

    private fun continues(expression_: Expression): Expression {
        var expression = expression_
        if (next.accept("-"))
            expression = MinusExpression.of(expression, expression())
        if (next.accept("×"))
            expression = RepeatedExpression(expression, repetitions(), comment())
        if (next.accept("+"))
            expression = RepeatedExpression(expression, "+")
        if (next.accept("?"))
            expression = RepeatedExpression(expression, "?")
        if (next.accept("*"))
            expression = RepeatedExpression(expression, "*")
        if (next.accept("="))
            expression = EqualsExpression(expression, until { skipSpaces(); next.peek("⇒") || next.end() || isBr })
        if (isEnd) return expression // can be true while we scan a switch case or value
        if (next.peek("⇒"))
            return switch(expression)

        skipWhitespace()

        if (next.accept("|")) {
            skipSpaces()
            assert(!isEnd)
            expression = AlternativesExpression.of(expression, expression())
        }
        return if (isEnd) expression
        else SequenceExpression.of(expression, expression())
    }

    private fun comment(): String? = if (isComment) commentRef().ref else null

    private fun skipWhitespace() {
        if (isComment) commentRef()
        if (isBr) next.expectElement("br")
        skipSpaces()
    }

    private fun parentheses(): Expression {
        val expression = until { if (next.end()) error("expected closing bracket before end"); next.peek(")") }
        next.expect(")")
        return expression
    }

    private fun hex(): CodePointExpression {
        val hex = StringBuilder()
        while (next.peek().isHex)
            next.read().appendTo(hex)
        return CodePointExpression(CodePoint.decode("0x$hex"))
    }

    private fun range(): RangeExpression {
        val from = hex()
        next.expect("-#x")
        val to = hex()
        next.expect("]")
        return RangeExpression(from, to)
    }

    private fun quote(): CodePointExpression {
        next.expect("“")
        val span = next.readElement()
        assert(span.tagName() == "span")
        assert(span.className() == "quote")
        val codePoint = CodePoint.of(span.text())
        next.expect("”")
        return CodePointExpression(codePoint)
    }

    private fun ref(): ReferenceExpression {
        var href = next.readElement().attr("href")
        assert(href.startsWith("#"))
        href = href.substring(1)
        return ReferenceExpression(href)
    }

    private fun plainRef(): ReferenceExpression {
        val out = StringBuilder()
        while (!isEnd && !next.peek().isWhitespace && next.isText)
            next.read().appendTo(out)
        return ReferenceExpression(out.toString())
    }

    private fun commentRef(): ReferenceExpression {
        next.expect("/*")
        return ReferenceExpression(next.readUntilAndSkip("*/").trim())
    }

    private fun repetitions(): String {
        skipSpaces()
        val repetitions = if (next.isText) next.read().toString() else readVar()
        skipSpaces()
        return repetitions
    }

    private fun readVar(): String {
        val element = next.readElement()
        assert(element.tagName() == "code")
        assert(element.className() == "varname")
        return element.text()
    }

    private fun variable() = VariableExpression(readVar())

    private fun switch(firstCase: Expression): SwitchExpression {
        val switch = SwitchExpression.of(firstCase, switchValue())
        while (!isEnd)
            switch.addCase(switchCase()).merge(switchValue())
        return switch
    }

    private fun switchCase() = until { next.peek("⇒") }
    private fun switchValue(): Expression {
        next.expect("⇒")
        val expression = until { next.end() || isBr }
        if (!next.end()) next.expectElement("br")
        return expression
    }
}
