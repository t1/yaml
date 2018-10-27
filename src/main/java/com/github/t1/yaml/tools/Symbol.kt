package com.github.t1.yaml.tools

val NL = symbol('\n').or(symbol('\r'))
val WS = symbol("whitespace") { Character.isWhitespace(it.value) }
val SPACE = symbol("space-char") { Character.isSpaceChar(it.value) }

fun symbol(char: Char): Symbol = symbol(CodePoint.of(char))
fun symbol(string: String): Symbol = symbol(CodePoint.of(string))
fun symbol(codePoint: CodePoint): Symbol = symbol(codePoint.info) { it == codePoint }
fun symbol(range: CodePointRange) = symbol("between ${range.start} and ${range.endInclusive}") { it in range }
fun symbol(description: String, predicate: (CodePoint) -> Boolean) = Symbol(object : (CodePoint) -> Boolean {
    override fun invoke(codePoint: CodePoint) = predicate(codePoint)
    override fun toString(): String = description
})

/** A single-character [Token] */
class Symbol(val predicate: (CodePoint) -> Boolean) : Token {
    override val predicates: List<(CodePoint) -> Boolean> get() = listOf(predicate)
    override fun toString() = "<$predicate>"
}
