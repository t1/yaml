package com.github.t1.yaml.tools

import java.util.function.Predicate

import java.util.stream.Collectors.toList

class StringToken(string: String) : Token {
    override val predicates: List<Predicate<CodePoint>>

    init {
        this.predicates = CodePoint
            .stream(string)
            .map { codePoint ->
                object : Predicate<CodePoint> {
                    override fun test(c: CodePoint): Boolean = c == codePoint

                    override fun toString(): String = codePoint.xinfo()
                }
            }
            .collect(toList())
    }

    override fun toString(): String = "StringToken$predicates"
}
