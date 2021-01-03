package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.Symbol.COLON
import com.github.t1.yaml.parser.Symbol.DOT
import com.github.t1.yaml.parser.Symbol.MINUS
import com.github.t1.yaml.parser.Symbol.WS
import com.github.t1.yaml.tools.CodePoint
import java.util.Arrays.asList
import java.util.function.Predicate
import java.util.stream.Collectors

/** A Token composed of multiple Symbols  */
enum class Marker(vararg symbols: Symbol) : Token {
    BLOCK_SEQUENCE_START(MINUS, WS),
    BLOCK_MAPPING_VALUE(COLON, WS),
    DIRECTIVES_END_MARKER(MINUS, MINUS, MINUS),
    DOCUMENT_END_MARKER(DOT, DOT, DOT);

    private val symbols: List<Symbol> = asList(*symbols)
    override val predicates: List<Predicate<CodePoint>> = this.symbols.stream()
        .flatMap { symbol -> symbol.predicates.stream() }
        .collect(Collectors.toList())!!
}
