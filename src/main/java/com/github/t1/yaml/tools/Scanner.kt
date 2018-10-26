package com.github.t1.yaml.tools

import java.io.Reader

open class Scanner(
    private val lookAheadLimit: Int,
    reader: Reader
) {
    private var position = 1
    private var lineNumber = 1

    private val reader: CodePointReader = CodePointReader(reader)


    val isStartOfFile get() = position == 1 && lineNumber == 1

    fun expect(string: String): Scanner = expect(StringToken(string))

    fun expect(token: Token): Scanner {
        for (predicate in token.predicates) {
            val info = this.toString()
            if (!predicate(read()))
                throw ParseException("expected $predicate but got $info")
        }
        return this
    }

    fun peek(token: Token): Boolean = token.matches(this)

    fun peek(): CodePoint = peek(1)[0]

    fun peek(count: Int): List<CodePoint> {
        reader.mark(count).use { return reader.read(count) }
    }

    fun peekWhile(symbol: Symbol): String {
        reader.mark(lookAheadLimit).use {
            val out = StringBuilder()
            while (true) {
                val codePoint = reader.read()
                if (codePoint.isEof || !codePoint(symbol))
                    return out.toString()
                codePoint.appendTo(out)
            }
        }
        throw UnsupportedOperationException("unreachable")
    }

    fun peekUntil(token: Token): String? {
        val predicates = token.predicates
        reader.mark(lookAheadLimit).use {
            val out = StringBuilder()
            var matchLength = 0
            while (true) {
                val codePoint = reader.read()
                if (codePoint.isEof)
                    return null
                if (predicates[matchLength](codePoint)) {
                    if (++matchLength == predicates.size)
                        return out.toString()
                } else {
                    @Suppress("UNUSED_VALUE")
                    matchLength = 0
                    codePoint.appendTo(out)
                }
            }
        }
        throw UnsupportedOperationException("unreachable")
    }

    fun peekAfter(count: Int): CodePoint? {
        val peek = peek(count + 1)
        return if (peek.size < count + 1) null else peek[peek.size - 1]
    }

    fun accept(string: String): Boolean = accept(StringToken(string))

    fun accept(token: Token): Boolean = peek(token) && expect(token).run { true }

    fun skip(token: Token): Scanner {
        accept(token)
        return this
    }

    fun end(): Boolean = peek().isEof

    open fun more(): Boolean = !end()

    open fun read(): CodePoint {
        val codePoint = reader.read()
        if (codePoint.isNl) {
            lineNumber++
            position = 1
        } else {
            position++
        }
        return codePoint
    }

    fun readWord(): String = readUntilAndSkip(WS)

    fun readLine(): String = readUntilAndSkip(NL)

    fun readUntil(end: String): String = readUntil(StringToken(end))

    fun readUntil(end: Token): String {
        val builder = StringBuilder()
        while (more() && !peek(end))
            builder.appendCodePoint(read().value)
        return builder.toString()
    }

    private fun readUntilAndSkip(end: Token): String {
        val result = readUntil(end)
        if (more())
            expect(end)
        return result
    }

    fun count(token: String): Int = count(StringToken(token))

    fun count(token: Token): Int = readWhile(token).length

    private fun readWhile(token: Token): String {
        val out = StringBuilder()
        while (peek(token))
            out.appendCodePoint(read().value)
        return out.toString()
    }

    override fun toString() = "${peek().info} at $positionInfo"

    val positionInfo get() = "line $lineNumber char $position"
}
