package com.github.t1.yaml.tools

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
        val totalPosition: Long = 0,
        val lineNumber: Long = 1,
        val linePosition: Long = 1
    ) {
        operator fun rem(nl: Boolean): Position = copy(
            totalPosition = this.totalPosition + 1,
            lineNumber = if (nl) lineNumber + 1 else lineNumber,
            linePosition = if (nl) 1 else linePosition + 1
        )

        val info get() = "line $lineNumber char $linePosition"
    }

    var position = Position()
    val isStartOfFile get() = position.totalPosition == 0L

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
        if (!codePoint.isEof)
            position %= codePoint.isNl
        return codePoint
    }

    abstract inner class Read {
        open fun read(): CodePoint =
            if (cache.isEmpty()) CodePoint.of(reader.read())
            else cache.removeFirst()
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
