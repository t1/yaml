package spec.generator.specparser

import org.assertj.core.api.Assertions
import org.jsoup.nodes.Element
import spec.generator.Expression

data class ReferenceParser(
    private var name: String,
    private val args: MutableList<Pair<String, Expression>>,
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
            if (value is Expression.VariableExpression && value.name.startsWith(prefix)) {
                name += value.name.substring(0, 1)
                Assertions.assertThat(args[0].first.startsWith(prefix))
                args[0] = args[0].first.substring(1) to Expression.VariableExpression(value.name.substring(1))
            }
        }
    }

    /** also see [fixName] and [readUntilClose] */
    private fun cleanupAndCheck(element: Element) {
        val text = element.text
        if (text.startsWith("“") && text.endsWith("”"))
            Assertions.assertThat(text.length).isGreaterThan(2)
        else {
            val bodyRef = ReferenceParser(text)
            bodyRef.fixName()
            if (args.isEmpty() || bodyRef.closed) {
                if (fixable(bodyRef, '<') || fixable(bodyRef, '≤')) name = bodyRef.name
                Assertions.assertThat(bodyRef.name).isEqualTo(name)
                Assertions.assertThat(bodyRef.args.size).isEqualTo(args.size)
                for (i in 0 until args.size) {
                    bodyRef.setArgName(i, args[i].first)
                    setArgValue(i, bodyRef.args[i].second)
                }
                Assertions.assertThat(bodyRef.args.map { it.first }).isEqualTo(args.map { it.first })
            } else {
                Assertions.assertThat(toString()).startsWith("->$text")
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

    private fun fixable(bodyRef: ReferenceParser, char: Char) = bodyRef.name.endsWith(char) && !name.endsWith(char)

    constructor(text: String) : this(
        name = text.parsedName,
        args = text.parsedArgs,
        closed = text.parseHasClosedArgs)

    companion object {
        fun from(next: NodeExpressionScanner) = ReferenceParser(next.readElement()).run {
            if (closed) this else readUntilClose(next)
        }

        private val Element.href: String
            get() {
                Assertions.assertThat(name).isEqualTo("a")
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
                return text.split(",").map { it to Expression.VariableExpression(it) }.toMutableList()
            }
        /** see [cleanupAndCheck] */
        private val String.parseHasClosedArgs: Boolean get() = !contains('(') || last() == ')'
    }

    /** Some refs have a ref as arg; they open with only opening parentheses, continue with the ref-arg, and close with another ref with ')' */
    private fun readUntilClose(next: NodeExpressionScanner): ReferenceParser {
        val argRef = ReferenceParser(next.readElement())
        val close = next.readElement()
        Assertions.assertThat("->${close.href}").isEqualTo("$this")
        Assertions.assertThat(close.text).isEqualTo(")")
        setArgValue(args.size - 1, argRef.toExpression())
        return ReferenceParser(name = name, args = args, closed = true)
    }

    override fun toString() = toExpression().toString()
    fun toExpression() = Expression.ReferenceExpression(name, args)
}
