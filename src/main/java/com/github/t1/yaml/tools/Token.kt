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

    operator fun plus(that: Token) = token("${this@Token} + $that", this@Token.predicates + that.predicates)

    infix fun or(that: Token): Token = token("${this@Token} or $that", listOf({ it: CodePoint -> this@Token.onlyPredicate(it) || that.onlyPredicate(it) }))

    val onlyPredicate
        get(): (CodePoint) -> Boolean {
            require(predicates.size == 1) { "expected the token <$this> to have exactly one predicate, but found $predicates" }
            return predicates[0]
        }

    // operator fun minus(that: Symbol): Symbol = symbol("${this.predicate} minus ${that.predicate}") { it(this) && !it(that) }

    operator fun not(): Token = token("not ${this}", predicates.map { predicate: (CodePoint) -> Boolean ->
        object : (CodePoint) -> Boolean {
            override fun invoke(codePoint: CodePoint) = !predicate.invoke(codePoint)
        }
    })
}

fun token(description: String, predicates: List<(CodePoint) -> Boolean>) = object : Token {
    override fun toString(): String = description
    override val predicates: List<(CodePoint) -> Boolean> get() = predicates
}
