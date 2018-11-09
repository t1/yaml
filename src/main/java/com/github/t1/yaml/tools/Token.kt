package com.github.t1.yaml.tools

import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once

/** A sequence of [CodePoint]s to be matched in a [Scanner] */
interface Token {
    fun match(reader: CodePointReader): Match

    operator fun plus(that: Token) = token("${this@Token} + $that") { reader ->
        val totalMatch = reader.mark {
            val match = this.match(reader) // the `and` method doesn't short-circuit
            if (!match.matches) match else match and that.match(reader)
        }
        if (totalMatch.matches)
            reader.read(totalMatch.codePoints.size).apply { require(this == totalMatch.codePoints) { "expected ${totalMatch.codePoints} but got $this" } }
        return@token totalMatch
    }

    operator fun times(n: Int): Token {
        var out = this
        repeat(n - 1) { out += this }
        return out
    }

    operator fun times(mode: RepeatMode): Token = when (mode) {
        zero_or_once -> token("${this@Token}?") { TODO() }
        zero_or_more -> token("${this@Token}?") { TODO() }
        once_or_more -> token("${this@Token}?") { TODO() }
    }

    @Suppress("EnumEntryName")
    enum class RepeatMode {
        zero_or_once, zero_or_more, once_or_more
    }

    infix fun or(that: Token) = token("${this@Token} or $that") { reader ->
        val thisMatch = reader.mark { this@Token.match(reader) }
        if (thisMatch.matches) {
            reader.read(thisMatch.codePoints.size)
            return@token thisMatch
        }
        val thatMatch = reader.mark { that.match(reader) }
        if (thatMatch.matches) {
            reader.read(thatMatch.codePoints.size)
            return@token thatMatch
        }
        return@token Match(matches = false)
    }

    operator fun minus(that: Token) = token("$this minus $that") { reader ->
        val match = reader.mark { this@Token.match(reader) }
        if (!match.matches) return@token match
        reader.read(match.codePoints.size)
        match and !that.match(reader)
    }

    operator fun not() = token("!${this@Token}") { !this@Token.match(it) }

    infix fun describedAs(description: String) = token(description) { this@Token.match(it) }
}

data class Match(
    val matches: Boolean,
    /** The code points until the non-match or the total match */
    val codePoints: List<CodePoint> = listOf()
) {
    infix fun or(that: Match) = when {
        this.matches -> this
        that.matches -> that
        else -> Match(false)
    }

    infix fun and(that: Match) = when {
        this.matches && that.matches -> Match(matches = true, codePoints = this.codePoints + that.codePoints)
        else -> Match(false)
    }

    operator fun not(): Match = this.copy(matches = !matches)
}

fun token(string: String) = token(CodePoint.allOf(string))
fun token(codePoints: List<CodePoint>): Token {
    if (codePoints.isEmpty()) return empty
    var out: Token = symbol(codePoints[0])
    for (i in 1 until codePoints.size)
        out += symbol(codePoints[i])
    return out
}

val empty = token("empty") { Match(matches = true) }

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
    override fun match(reader: CodePointReader) = reader.read().run {
        if (predicate(this)) Match(matches = true, codePoints = listOf(this)) else Match(matches = false)
    }
}
