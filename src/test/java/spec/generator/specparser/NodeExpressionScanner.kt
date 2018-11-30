package spec.generator.specparser

import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePoint.Companion.EOF
import com.github.t1.yaml.tools.Scanner
import com.github.t1.yaml.tools.token
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Scans an html snippet which includes seamless scanning of text nodes.
 */
class NodeExpressionScanner(private val nodes: List<Node>) {
    private var i = 0
    private var nextText: Scanner? = null

    val isText: Boolean
        get() = more() && node() is TextNode && withText { nextText!!.more() }

    override fun toString(): String {
        return if (more()) "in node " + i + ": [" + (if (nextText == null) node() else nextText) + "]" else "<end>"
    }

    fun node(): Node {
        return nodes[i]
    }

    private operator fun next() {
        nextText = null
        i++
    }

    fun end(): Boolean {
        return !more()
    }

    fun more(): Boolean {
        return i < nodes.size || nextText != null && nextText!!.more()
    }

    fun accept(text: String): Boolean {
        return if (!more() || !isText) false else withText { nextText!!.accept(text) }
    }

    fun peek(text: String): Boolean {
        return nextText?.peek(token(text)) ?: false
    }

    fun peek(): CodePoint {
        return if (nextText == null) EOF else nextText!!.peek()
    }

    fun read(): CodePoint {
        return withText { nextText!!.read() }
    }

    private fun readUntil(end: String): String {
        return withText { nextText!!.readUntil(end) }
    }

    fun readUntilAndSkip(end: String): String {
        val out = StringBuilder()
        while (more() && !accept(end)) {
            if (out.isNotEmpty()) out.append(' ')
            out.append(
                if (isText) readUntil(end)
                else readElement().html()
            )
        }
        return out.toString()
    }

    fun expect(text: String): NodeExpressionScanner {
        assert(more())
        withText { nextText!!.expect(text) }
        return this
    }

    private fun <T> withText(runnable: () -> T): T {
        assert(more())
        assert(node() is TextNode)
        if (nextText == null)
            nextText = Scanner((node() as TextNode).text().trim { it <= ' ' })
        val result = runnable()
        if (nextText!!.end())
            next()
        return result
    }

    fun peekElement(): Element {
        return node() as Element
    }

    fun readElement(): Element {
        val node = node()
        next()
        return node as Element
    }

    fun isElement(tagName: String): Boolean {
        if (isText || end())
            return false
        val node = node()
        return node is Element && node.tagName() == tagName
    }

    fun expectElement(tagName: String) {
        assert(more())
        assert(!isText)
        assert(isElement(tagName))
        readElement()
    }
}

val Node.name: String get() = nodeName()
val Node.className: String get() = attr("class")
val Node.text: String get() = (this as Element).text()

fun Node.parent(predicate: (Node) -> Boolean): Node {
    var node = this
    while (!predicate(node)) node = node.parentNode()
    return node
}

fun Node.child(predicate: (Node) -> Boolean): Node = this.childNodes().first(predicate)
