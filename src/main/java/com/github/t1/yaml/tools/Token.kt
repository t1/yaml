package com.github.t1.yaml.tools

/** A sequence of [CodePoint]s to be matched in a [Scanner] */
interface Token {
    val predicates: List<(CodePoint) -> Boolean>

    /** Match a fixed number of predicates */
    fun matches(scanner: Scanner): Boolean {
        val codePoints = scanner.peek(predicates.size)
        assert(predicates.size == codePoints.size)
        for (i in predicates.indices)
            if (!predicates[i](codePoints[i]))
                return false
        return true
    }

    operator fun plus(that: Token) = object : Token {
        override val predicates: List<(CodePoint) -> Boolean> = this@Token.predicates + that.predicates

        override fun toString() = "${this@Token} + $that"
    }

    infix fun or(that: Token): Token = object : Token {
        override val predicates: List<(CodePoint) -> Boolean> = listOf({ it: CodePoint ->
            this@Token.onlyPredicate(it) || that.onlyPredicate(it)
        })

        override fun toString() = "${this@Token} or $that"
    }

    val onlyPredicate
        get(): (CodePoint) -> Boolean {
            require(predicates.size == 1) { "expected the token <$this> to have exactly one predicate, but found $predicates" }
            return predicates[0]
        }

    // operator fun minus(that: Symbol): Symbol = symbol("${this.predicate} minus ${that.predicate}") { it(this) && !it(that) }
    //
    // operator fun not(): Symbol = symbol("not ${this.predicate}") { it(this) }
}
