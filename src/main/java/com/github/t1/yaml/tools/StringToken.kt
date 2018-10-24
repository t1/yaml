package com.github.t1.yaml.tools

import java.util.stream.Collectors.toList

class StringToken(string: String) : Token {
    override val predicates: List<(CodePoint) -> Boolean>

    init {
        this.predicates = string.codePoints().mapToObj { CodePoint(it) }
            .map { codePoint ->
                object : (CodePoint) -> Boolean {
                    override fun invoke(c: CodePoint): Boolean = c == codePoint

                    override fun toString(): String = codePoint.info
                }
            }
            .collect(toList())
    }

    override fun toString(): String = "StringToken$predicates"
}
