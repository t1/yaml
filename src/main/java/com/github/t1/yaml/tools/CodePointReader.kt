package com.github.t1.yaml.tools

import com.github.t1.codepoint.CodePoint
import com.github.t1.codepoint.CodePoint.Companion.BOM
import com.github.t1.codepoint.CodePoint.Companion.EOF
import com.github.t1.yaml.tools.CodePointReader.Mark
import java.io.Reader
import java.io.StringReader
import java.util.ArrayList
import java.util.LinkedList
import java.util.Stack

/**
 * Reads [CodePoint]s from a [Reader]. Supports multiple auto-closeable [Mark]s.
 */
class CodePointReader(private val reader: Reader) {
    constructor(string: String) : this(StringReader(string))

    data class Position(
        /** the zero-based code point position in the stream. BOM counts, too. */
        val totalPosition: Int = 0,
        /** the one-based line number in the reading stream */
        val lineNumber: Int = 1,
        /** the one-based position withing the current line, ignoring BOM in line 1 */
        val linePosition: Int = 1
    ) {
        operator fun rem(codePoint: CodePoint): Position {
            if (codePoint == EOF) return this
            if (codePoint == BOM && totalPosition == 0) return copy(totalPosition = this.totalPosition + 1) // don't increase linePosition!
            val nl = codePoint.isNewLine // TODO only one NL if Windows-style 0D-0A
            return copy(
                totalPosition = this.totalPosition + 1,
                lineNumber = if (nl) lineNumber + 1 else lineNumber,
                linePosition = if (nl) 1 else linePosition + 1
            )
        }

        val info get() = "line $lineNumber char $linePosition"
    }

    var position = Position()
    val isStartOfFile get() = isFirstLine && isStartOfLine
    val isFirstLine get() = position.lineNumber == 1
    val isStartOfLine get() = position.linePosition == 1
    val isEndOfFile get() = mark { read() == EOF }

    private val cache = LinkedList<CodePoint>()
    private val reads = Stack<Read>().apply { add(RealRead()) }

    override fun toString() = "at $position with marks: $reads"

    fun <T> mark(body: (Mark) -> T): T = Mark().use(body)

    fun read(count: Int): List<CodePoint> {
        val out = ArrayList<CodePoint>()
        for (i in 0 until count)
            out.add(read())
        return out
    }

    fun read(): CodePoint {
        val codePoint = reads.peek().read()
        position %= codePoint
        return codePoint
    }

    fun readWhile(predicate: (CodePointReader) -> List<CodePoint>): List<CodePoint> {
        val out = mutableListOf<CodePoint>()
        while (!isEndOfFile) {
            val match = mark { predicate(this) }
            if (match.isEmpty()) break
            read(match.size)
            out.addAll(match)
        }
        return out
    }

    /** Read [CodePoint]s until the predicate returns a match and return the code points before that. If EOF is reached, return an empty list. */
    fun readUntil(predicate: (CodePointReader) -> List<CodePoint>): List<CodePoint> {
        val out = mutableListOf<CodePoint>()
        mark {
            while (!isEndOfFile) {
                val match = mark { predicate(this) }
                if (!match.isEmpty()) break
                out.add(read())
            }
            if (isEndOfFile) out.clear()
        }
        read(out.size)
        return out
    }

    fun expect(expected: List<CodePoint>) {
        val actual = read(expected.size)
        require(actual == expected) { "expected $expected but got $actual at ${position.info}" }
    }

    abstract inner class Read {
        open fun read(): CodePoint =
            if (!cache.isEmpty()) cache.removeFirst() else {
                val c = reader.read()
                when {
                    c < 0 -> EOF
                    Character.isHighSurrogate(c.toChar()) -> CodePoint.of(c.toChar(), reader.read().toChar())
                    else -> CodePoint.of(c)
                }
            }

    }

    inner class RealRead : Read()

    inner class Mark : Read(), AutoCloseable {
        private val lastPosition = position
        private val read = mutableListOf<CodePoint>()

        init {
            reads.push(this)
        }

        override fun read(): CodePoint {
            val codePoint = super.read()
            read.add(codePoint)
            return codePoint
        }

        override fun close() {
            require(reads.pop() === this)
            position = lastPosition
            cache.addAll(0, read)
        }

        override fun toString() = "Mark@$lastPosition"
    }
}
