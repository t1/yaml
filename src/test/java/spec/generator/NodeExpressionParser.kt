package spec.generator

import com.github.t1.yaml.tools.CodePoint
import org.jsoup.nodes.Node
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.LiteralExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.NullExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression

/**
 * A recursive descend parser for the grammar html snippets in the yaml spec.
 * Error messages are generally failed assertions only, so you need the source to see what happened.
 * Some errors in the grammar description are not detected... the result may not be valid.
 */
class NodeExpressionParser(nodes: List<Node>) {
    private val next: NodeExpressionScanner = NodeExpressionScanner(nodes)

    private val isBr: Boolean
        get() = next.isElement("br")

    private val isQuote: Boolean
        get() = next.accept("“")

    private val isHref: Boolean
        get() = next.isElement("a") && next.peekElement().hasAttr("href")

    private val isVar: Boolean
        get() = next.isElement("code") && next.peekElement().className() == "varname"

    fun expression(): Expression {
        skipWhitespaceAndComments()

        var expression = body()

        skipWhitespaceAndComments()

        expression = postfix(expression)

        return expression
    }

    private fun body(): Expression {
        if (next.accept("("))
            return expression()
        if (next.accept("#x"))
            return hex()
        if (next.accept("[#x"))
            return range()
        if (isQuote)
            return quote()
        if (isHref)
            return href()
        if (isVar)
            return switchLabel() // can only be in switch
        if (next.end())
            return NullExpression()
        throw AssertionError("unexpected start $next")
    }

    private fun postfix(expression_: Expression): Expression {
        var expression = expression_
        if (next.accept("-"))
            expression = MinusExpression.of(expression, expression())
        if (next.accept("×"))
            expression = RepeatedExpression(expression, repetitions())
        if (next.accept("+"))
            expression = RepeatedExpression(expression, "+")
        if (next.accept("?"))
            expression = RepeatedExpression(expression, "?")
        if (next.accept("*"))
            expression = RepeatedExpression(expression, "*")

        skipWhitespaceAndComments()

        if (next.accept("|")) {
            skipWhitespaceAndComments()
            if (next.more() && !next.accept(")"))
            // an empty trailing pipe is allowed :(
                expression = AlternativesExpression.of(expression, expression())
        } else if (next.accept("⇒")) {
            expression = switchExpression(expression)
        } else if (next.accept(")"))
        else if (next.more())
            expression = SequenceExpression.of(expression, expression()) // simply return
        return expression
    }

    private fun skipWhitespaceAndComments() {
        var count: Int
        do {
            count = next.count(" ")
            if (next.accept("/*")) {
                next.readUntilAndSkip("*/")
                count++
            }
            if (isBr) {
                next.expectElement("br")
                count++
            }
        } while (count > 0)
    }

    private fun hex(): Expression {
        val hex = StringBuilder()
        while (next.peek().isHex)
            hex.append(next.read())
        return CodePointExpression(CodePoint.decode("0x$hex"))
    }

    private fun range(): Expression {
        val from = hex()
        next.expect("-#x")
        val to = hex()
        next.expect("]")
        return RangeExpression(from, to)
    }

    private fun quote(): Expression {
        val span = next.readElement()
        assert(span.tagName() == "span")
        assert(span.className() == "quote")
        val codePoint = CodePoint.of(span.text())
        next.expect("”")
        return CodePointExpression(codePoint)
    }

    private fun href(): Expression {
        var href = next.readElement().attr("href")
        assert(href.startsWith("#"))
        href = href.substring(1)
        return ReferenceExpression(href)
    }

    private fun repetitions(): String {
        skipWhitespaceAndComments()
        val repetitions = if (next.isText) next.read().toString() else readVar()
        skipWhitespaceAndComments()
        return repetitions
    }

    private fun readVar(): String {
        val element = next.readElement()
        assert(element.tagName() == "code")
        assert(element.className() == "varname")
        return element.text()
    }

    private fun switchExpression(label: Expression): Expression {
        skipWhitespaceAndComments()
        val switchExpression = SwitchExpression()
        switchExpression.addCase(label).merge(switchValue())
        while (next.more()) {
            switchExpression.addCase(switchLabel())
            next.expect("⇒")
            switchExpression.merge(switchValue())
        }
        return switchExpression
    }

    private fun switchLabel(): LiteralExpression {
        return readLiteralUntil { next.`is`("⇒") }
    }

    private fun switchValue(): LiteralExpression {
        return readLiteralUntil { next.isElement("br") || next.end() }
    }

    private fun readLiteralUntil(end: () -> Boolean): LiteralExpression {
        val out = StringBuilder()
        while (!end())
            when {
                isQuote -> out.append(quote()).append(" ")
                isVar -> out.append(readVar()).append(" ")
                isHref -> out.append(href()).append(" ")
                next.isText -> out.appendCodePoint(next.read().value)
                else -> throw AssertionError("unexpected switch literal $next")
            }
        skipWhitespaceAndComments()
        return LiteralExpression(out.toString().trim { it <= ' ' })
    }
}
