package spec.generator

import com.github.t1.yaml.tools.CodePoint
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.nodes.Element
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
    private val productioncounter: Int
        get() {
            if (!next.more()) return -1
            val tr = next.node().parent { it.name == "tr" }
            val counterNode = tr.child { it.className == "productioncounter" }
            require(counterNode.text.startsWith("[") && counterNode.text.endsWith("]"))
            return Integer.parseInt(counterNode.text.substring(1, counterNode.text.length - 1))
        }

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

    private fun comment(): String? = if (isComment) commentRef().name else null

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

    private fun ref() = Ref.from(next).toExpression()

    data class Ref(
        var name: String,
        val args: MutableList<Pair<String, Expression>>,
        private var closed: Boolean
    ) {
        private constructor(element: Element) : this(element.href) {
            fixName()
            cleanupAndCheck(element)
        }

        /** `s-indent`(<n) -> `s-indent<`(n); same with ≤ */
        private fun fixName() {
            fixName("<")
            fixName("≤")
        }

        private fun fixName(prefix: String) {
            if (args.size == 1) {
                val value = args[0].second
                if (value is VariableExpression && value.name.startsWith(prefix)) {
                    name += value.name.substring(0, 1)
                    assertThat(args[0].first.startsWith(prefix))
                    args[0] = args[0].first.substring(1) to VariableExpression(value.name.substring(1))
                }
            }
        }

        /** also see [fixName] and [readUntilClose] */
        private fun cleanupAndCheck(element: Element) {
            val text = element.text
            if (text.startsWith("“") && text.endsWith("”"))
                assertThat(text.length).isGreaterThan(2)
            else {
                val bodyRef = Ref(text)
                bodyRef.fixName()
                if (args.isEmpty() || bodyRef.closed) {
                    if (fixable(bodyRef, '<') || fixable(bodyRef, '≤')) name = bodyRef.name
                    assertThat(bodyRef.name).isEqualTo(name)
                    assertThat(bodyRef.args.size).isEqualTo(args.size)
                    for (i in 0 until args.size) {
                        bodyRef.setArgName(i, args[i].first)
                        setArgValue(i, bodyRef.args[i].second)
                    }
                    assertThat(bodyRef.args.map { it.first }).isEqualTo(args.map { it.first })
                } else {
                    assertThat(toString()).startsWith("->$text")
                    this.closed = false
                }
            }
        }

        private fun setArgName(i: Int, value: String) {
            args[i] = args[i].copy(first = value)
        }

        private fun setArgValue(i: Int, value: Expression) {
            args[i] = args[i].copy(second = value)
        }

        private fun fixable(bodyRef: Ref, char: Char) = bodyRef.name.endsWith(char) && !name.endsWith(char)

        constructor(text: String) : this(
            name = text.parsedName,
            args = text.parsedArgs,
            closed = text.parseHasClosedArgs)

        companion object {
            fun from(next: NodeExpressionScanner) = Ref(next.readElement()).run {
                if (closed) this else readUntilClose(next)
            }

            private val Element.href: String
                get() {
                    assertThat(name).isEqualTo("a")
                    val href = attr("href")
                    assert(href.startsWith("#"))
                    return href.substring(1)
                }

            private val String.parsedName: String
                get() {
                    val end = indexOf('(')
                    if (end < 0) return this
                    return substring(0, end)
                }
            /** see [readUntilClose] */
            private val String.parsedArgs: MutableList<Pair<String, Expression>>
                get() {
                    val start = indexOf('(')
                    if (start < 0) return mutableListOf()
                    val text = substring(start + 1, length - if (parseHasClosedArgs) 1 else 0)
                    return text.split(",").map { it to VariableExpression(it) }.toMutableList()
                }
            /** see [cleanupAndCheck] */
            private val String.parseHasClosedArgs: Boolean get() = !contains('(') || last() == ')'
        }

        /** Some refs have a ref as arg; they open with only opening parentheses, continue with the ref-arg, and close with another ref with ')' */
        private fun readUntilClose(next: NodeExpressionScanner): Ref {
            val argRef = Ref(next.readElement())
            val close = next.readElement()
            assertThat("->${close.href}").isEqualTo("$this")
            assertThat(close.text).isEqualTo(")")
            setArgValue(args.size - 1, argRef.toExpression())
            return Ref(name = name, args = args, closed = true)
        }

        override fun toString() = toExpression().toString()
        fun toExpression() = ReferenceExpression(name, args)
    }

    private fun plainRef(): ReferenceExpression {
        val out = StringBuilder()
        while (!isEnd && !next.peek().isWhitespace && next.isText)
            next.read().appendTo(out)
        return ReferenceExpression(out.toString())
    }

    private fun commentRef(): ReferenceExpression {
        next.expect("/*")
        var comment = next.readUntilAndSkip("*/").trim()
        if (productioncounter == 126) { // BUG-IN-SPEC
            assertThat(comment).endsWith(")")
            comment = comment.substring(0, comment.length - 1).trim()
        }
        return ReferenceExpression(comment)
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
