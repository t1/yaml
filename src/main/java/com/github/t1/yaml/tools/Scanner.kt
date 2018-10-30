package com.github.t1.yaml.tools

open class Scanner(private val reader: CodePointReader) {
    constructor(string: String) : this(CodePointReader(string))

    open fun read(): CodePoint = reader.read()

    private fun <T> mark(body: (CodePointReader.Read) -> T): T = reader.mark(body)

    val isStartOfFile get() = reader.isStartOfFile


    override fun toString() = "${peek().info} at $positionInfo"

    val positionInfo get() = reader.position.info


    fun expect(string: String): Scanner = expect(token(string))

    fun expect(token: Token): Scanner {
        val match = match(token)
        if (!match.matches)
            throw ParseException("expected $token but got ${match.codePoints.joinToString { it.info }} at $positionInfo")
        skip(match.length)
        return this
    }

    fun peek(token: Token): Boolean = match(token).matches

    fun match(token: Token) = token.match(this)

    fun peek(): CodePoint = peek(1)[0]

    fun peek(count: Int): List<CodePoint> = mark { reader.read(count) }

    fun peekWhile(token: Token) = peekUntil(!token) ?: ""

    fun peekUntil(token: Token): String? {
        val out = StringBuilder()
        val found: Boolean = mark {
            while (true) {
                val match = token.match(this)
                if (match.matches) return@mark true
                skip(1)
                if (end()) return@mark false
                match.codePoints.forEach { c -> out.append(c) }
            }
            @Suppress("UNREACHABLE_CODE") throw UnsupportedOperationException("unreachable") // Kotlin 1.2.71 limitation
        }
        return if (found) out.toString() else null
    }

    fun peekAfter(count: Int): CodePoint? {
        val peek = peek(count + 1)
        return if (peek.size < count + 1) null else peek[peek.size - 1]
    }

    fun matchesAfter(count: Int, token: Token): Boolean {
        val then = peekAfter(count) ?: return false
        // TODO there must be a more elegant way
        return token.match(Scanner(CodePointReader(then.toString()))).matches
    }

    fun accept(string: String): Boolean = accept(token(string))

    fun accept(token: Token): Boolean = peek(token) && expect(token).run { true }

    private fun skip(count: Int): Scanner {
        reader.read(count)
        return this
    }

    fun skip(token: Token): Scanner {
        accept(token)
        return this
    }

    fun end(): Boolean = peek().isEof

    open fun more(): Boolean = !end()

    fun readUntil(end: String): String = readUntil(token(end))

    fun readUntil(end: Token): String {
        val builder = StringBuilder()
        while (more() && !peek(end))
            builder.appendCodePoint(read().value)
        return builder.toString()
    }

    fun readUntilAndSkip(end: Token): String {
        val result = readUntil(end)
        if (more())
            expect(end)
        return result
    }

    fun count(token: String): Int = count(token(token))

    fun count(token: Token): Int = readWhile(token).length

    private fun readWhile(token: Token): String {
        val out = StringBuilder()
        while (peek(token))
            out.appendCodePoint(read().value)
        return out.toString()
    }
}
