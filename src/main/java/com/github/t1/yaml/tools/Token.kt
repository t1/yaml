package com.github.t1.yaml.tools

/** A sequence of [CodePoint]s to be matched in a [Scanner] */
interface Token {
    fun match(scanner: Scanner): Match

    operator fun plus(that: Token) = object : Token {
        override fun toString() = "${this@Token} + $that"
        override fun match(scanner: Scanner): Match {
            // TODO this is too limiting: the right token could be something other than a symbol
            val left = this@Token.match(scanner)
            if (!left.matches) return left
            val thatSymbol = that as Symbol
            val then = scanner.peekAfter(left.length) ?: return left.copy(matches = false)
            return if (thatSymbol.predicate(then))
                left.copy(codePoints = left.codePoints + then)
            else
                left.copy(matches = false)
        }
    }

    infix fun or(that: Token) = object : Token {
        override fun toString(): String = "${this@Token} or $that"
        override fun match(scanner: Scanner) = this@Token.match(scanner) or that.match(scanner)
    }

    // operator fun minus(that: Symbol): Symbol = symbol("${this.predicate} minus ${that.predicate}") { it(this) && !it(that) }

    operator fun not() = object : Token {
        override fun match(scanner: Scanner) = !this@Token.match(scanner)
        override fun toString() = "!${this@Token}"
    }

    infix fun describedAs(description: String) = object : Token {
        override fun match(scanner: Scanner) = this@Token.match(scanner)
        override fun toString() = description
    }
}

data class Match(
    val matches: Boolean,
    /** The code points until the non-match or the total match */
    val codePoints: List<CodePoint> = listOf()
) {
    val length: Int get() = codePoints.size

    infix fun or(that: Match) = when {
        this.matches -> this
        that.matches -> that
        else -> Match(false)
    }

    operator fun not(): Match = this.copy(matches = !matches)
}

fun token(string: String) = token(CodePoint.allOf(string))
fun token(codePoints: List<CodePoint>) = token(
    description = codePoints.joinToString { codePoint -> codePoint.info },
    predicates = codePoints.map { codePoint -> { that: CodePoint -> that == codePoint } }
)
fun token(description: String, predicates: List<(CodePoint) -> Boolean>) = object : Token {
    override fun toString(): String = description

    override fun match(scanner: Scanner): Match {
        val codePoints = scanner.peek(predicates.size)
        assert(predicates.size == codePoints.size)
        for (i in predicates.indices)
            if (!predicates[i](codePoints[i]))
                return Match(matches = false, codePoints = codePoints.subList(0, i + 1))
        return Match(true, codePoints)
    }
}

@Suppress("ClassName") object undefined : Token {
    override fun match(scanner: Scanner): Match = throw UnsupportedOperationException("undefined token")
}

fun symbol(char: Char) = symbol(CodePoint.of(char))
fun symbol(string: String) = symbol(CodePoint.of(string))
fun symbol(codePoint: CodePoint) = Symbol(codePoint.info) { it: CodePoint -> it == codePoint }
fun symbol(range: CodePointRange) = Symbol("between ${range.start} and ${range.endInclusive}") { it: CodePoint -> it in range }

/** A [Token] matching a single [CodePoint] */
data class Symbol(val description: String, val predicate: (CodePoint) -> Boolean) : Token {
    override fun match(scanner: Scanner) = scanner.peek(1)[0].run {
        Match(predicate(this), listOf(this))
    }

    override fun toString() = "<$description>"
}
