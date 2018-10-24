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
}
