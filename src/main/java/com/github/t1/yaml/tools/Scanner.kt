package com.github.t1.yaml.tools

import com.github.t1.yaml.parser.Symbol.BOM
import com.github.t1.yaml.parser.Symbol.NL
import com.github.t1.yaml.parser.Symbol.WS
import com.github.t1.yaml.parser.YamlParseException
import java.io.Reader
import java.io.StringReader
import java.util.Optional

open class Scanner @java.beans.ConstructorProperties("lookAheadLimit", "reader") constructor(private val lookAheadLimit: Int, private val reader: CodePointReader) {

    private var position = 1
    private var lineNumber = 1

    constructor(lookAheadLimit: Int, reader: Reader) : this(lookAheadLimit, CodePointReader(reader))

    constructor(lookAheadLimit: Int, text: String) : this(lookAheadLimit, StringReader(text))


    fun expect(string: String): Scanner = expect(StringToken(string))

    fun expect(token: Token): Scanner {
        for (predicate in token.predicates) {
            val info = this.toString()
            if (!predicate.test(read()))
                throw YamlParseException("expected $predicate but got $info")
        }
        return this
    }

    fun `is`(string: String): Boolean = `is`(StringToken(string))

    fun `is`(token: Token): Boolean {
        val predicates = token.predicates
        val codePoints = peek(predicates.size)
        assert(predicates.size == codePoints.size)
        for (i in predicates.indices)
            if (!predicates[i].test(codePoints[i]))
                return false
        return true
    }

    fun accept(string: String): Boolean = accept(StringToken(string))

    fun accept(token: Token): Boolean {
        if (!`is`(token))
            return false
        expect(token)
        return true
    }

    fun end(): Boolean = peek().isEof

    open fun more(): Boolean = !end()

    fun acceptBom() {
        if (`is`(BOM))
            reader.read()
    }

    fun peek(): CodePoint = peek(1)[0]

    private fun peek(count: Int): List<CodePoint> {
        reader.mark(count).use { return reader.read(count) }
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
                if (predicates[matchLength].test(codePoint)) {
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

    fun peekAfter(count: Int): Optional<CodePoint> {
        val peek = peek(count + 1)
        return if (peek.size < count + 1) Optional.empty() else Optional.of(peek[peek.size - 1])
    }

    fun read(): CodePoint {
        val codePoint = reader.read()
        if (BOM.test(codePoint))
            throw YamlParseException("A BOM must not appear inside a document")
        if (NL.test(codePoint)) {
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
        while (more() && !`is`(end))
            builder.appendCodePoint(read().value)
        return builder.toString()
    }

    private fun readUntilAndSkip(end: Token): String {
        val result = readUntil(end)
        if (more())
            expect(end)
        return result
    }

    fun skip(token: Token): Scanner {
        accept(token)
        return this
    }

    fun count(token: String): Int = count(StringToken(token))

    fun count(token: Token): Int = readWhile(token).length

    private fun readWhile(token: Token): String {
        val out = StringBuilder()
        while (`is`(token))
            out.appendCodePoint(read().value)
        return out.toString()
    }

    override fun toString() = peek().xinfo() + " at line " + lineNumber + " char " + position
}
