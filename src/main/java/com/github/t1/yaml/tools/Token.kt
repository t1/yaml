package com.github.t1.yaml.tools

import com.github.t1.codepoint.CodePoint
import com.github.t1.codepoint.CodePoint.Companion.EOF
import com.github.t1.codepoint.CodePointRange
import com.github.t1.codepoint.toCodePointRange
import com.github.t1.yaml.tools.CodePointReader.Mark
import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once

/**
 * A sequence of [CodePoint]s to be matched in a [CodePointReader].
 * A token can match zero characters
 */
interface Token {
    /** Check if this token comes next in the reader and, if so, advance the reader to after the match */
    fun match(reader: CodePointReader): Match

    /** Check if this token comes next in the reader without moving the reader */
    fun wouldMatch(reader: CodePointReader): Match = reader.mark { this.match(reader) }

    infix fun or(that: Token) = token("${this@Token} or $that") { reader ->
        reader.read { this.match(reader) or { that.match(reader) } }
    }

    infix fun and(that: Token) = token("${this@Token} and $that") { reader ->
        reader.read { this.wouldMatch(reader) and { that.wouldMatch(reader) } }
    }

    operator fun not() = token("!${this@Token}") { reader ->
        val thisMatch = reader.mark { this@Token.match(reader) }
        if (thisMatch.matches) Match(matches = false)
        else Match(matches = true, codePoints = listOf(reader.read()))
    }

    operator fun minus(that: Token) = this and !that named "$this minus $that"

    operator fun plus(that: Token) = token("${this@Token} + $that") { reader ->
        reader.read {
            val thisMatch = this.match(reader)
            if (!thisMatch.matches) return@read thisMatch
            val thatMatch = that.match(reader)
            if (!thatMatch.matches) return@read thatMatch
            return@read Match(matches = true, codePoints = thisMatch.codePoints + thatMatch.codePoints)
        }
    }

    operator fun times(n: Int): Token {
        var out = this
        repeat(n - 1) { out += this }
        return out
    }

    operator fun times(mode: RepeatMode): Token = when (mode) {
        zero_or_once -> token("${this@Token}?") { reader ->
            val match = reader.read { this@Token.match(reader) }
            if (match.matches) match else Match(matches = true) // zero length hit
        }
        zero_or_more -> token("${this@Token}*") { reader ->
            reader.matchMore(mutableListOf())
        }
        once_or_more -> token("${this@Token}+") { reader ->
            val first = this@Token.match(reader)
            if (!first.matches) first
            else reader.matchMore(first.codePoints.toMutableList())
        }
    }

    private fun CodePointReader.matchMore(codePoints: MutableList<CodePoint>): Match {
        while (true) {
            val more = this@Token.match(this)
            if (more.codePoints.isEmpty()) break
            codePoints += more.codePoints
        }
        return Match(matches = true, codePoints = codePoints)
    }

    @Suppress("EnumEntryName")
    enum class RepeatMode {
        zero_or_once, zero_or_more, once_or_more
    }

    infix fun named(description: String) = token(description, this)
}

data class Match(
    val matches: Boolean,
    /** All the code points matching or an empty list, if it doesn't match. Not that there are empty matches, i.e. it matches, but has zero code points */
    val codePoints: List<CodePoint> = listOf()
) {
    init {
        if (!matches) assert(codePoints.isEmpty()) { "unexpected code points for non-match: $codePoints" }
        assert(codePoints.none { it == EOF }) { "unexpected EOF in: $codePoints" }
    }

    val size: Int get() = codePoints.size

    /** short-circuit `or` */
    infix fun or(that: () -> Match): Match = if (this.matches) this else that()

    /** short-circuit `and` */
    infix fun and(thatBlock: () -> Match): Match {
        if (!this.matches) return this
        val that = thatBlock()
        if (!that.matches) return that
        assert(this.codePoints == that.codePoints) { "expected $this and $that to have match the same code point" }
        return this
    }
}

fun token(name: String, char: Char) = symbol(char) named name
fun token(name: String, charRange: CharRange) = symbol(charRange.toCodePointRange()) named name
fun token(string: String) = token(CodePoint.allOf(string))
fun token(codePoints: List<CodePoint>) =
    when (codePoints.size) {
        0 -> empty
        1 -> symbol(codePoints[0])
        else -> token("$codePoints") { reader ->
            reader.read {
                if (codePoints.all { codePoint -> reader.read() == codePoint }) Match(matches = true, codePoints = codePoints) else Match(matches = false)
            }
        }
    }

private fun CodePointReader.read(body: (Mark) -> Match): Match {
    val match = mark(body)
    if (match.matches) expect(match.codePoints)
    return match
}

val empty = token("empty") { Match(matches = true) }
val startOfLine = token("startOfLine") { Match(matches = it.isStartOfLine) }
val whitespace = Symbol("whitespace", CodePoint::isWhitespace)
val endOfFile = token("EOF") { Match(matches = it.isEndOfFile) }

/** Return a token that lazily generates a token when called. This is important to break recursions between tokens. */
fun tokenGenerator(description: String, body: () -> Token) = token(description) { reader -> body().match(reader) }

/** Renames a token */
fun token(description: String, token: Token) = token(description) { token.match(it) }

fun token(description: String, match: (CodePointReader) -> Match) = object : Token {
    override fun toString() = description
    override fun match(reader: CodePointReader): Match = match(reader)
}

@Suppress("ClassName") object undefined : Token {
    override fun match(reader: CodePointReader): Match = throw UnsupportedOperationException("undefined token")
}

fun symbol(char: Char) = symbol(CodePoint.of(char))
fun symbol(string: String) = symbol(CodePoint.of(string))
fun symbol(codePoint: CodePoint) = Symbol(codePoint.info) { it: CodePoint -> it == codePoint }
fun symbol(range: CodePointRange) = Symbol("between ${range.start} and ${range.endInclusive}") { it: CodePoint -> it in range }

/** A [Token] matching a single [CodePoint] */
data class Symbol(val description: String, val predicate: (CodePoint) -> Boolean) : Token {
    override fun toString() = description
    override fun match(reader: CodePointReader): Match {
        val codePoint = reader.mark { reader.read() }
        if (!predicate(codePoint)) return Match(matches = false)
        val codePoints = listOf(codePoint)
        reader.expect(codePoints)
        return Match(matches = true, codePoints = codePoints)
    }
}
