package com.github.t1.yaml.parser;

import com.github.t1.yaml.tools.CodePoint;
import com.github.t1.yaml.tools.Token;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.t1.yaml.parser.Symbol.COLON;
import static com.github.t1.yaml.parser.Symbol.DOT;
import static com.github.t1.yaml.parser.Symbol.MINUS;
import static com.github.t1.yaml.parser.Symbol.WS;
import static java.util.Arrays.asList;

/** A Token composed of multiple Symbols */
public enum Marker implements Token {
    BLOCK_SEQUENCE_START(MINUS, WS),
    BLOCK_MAPPING_VALUE(COLON, WS),
    DIRECTIVES_END_MARKER(MINUS, MINUS, MINUS),
    DOCUMENT_END_MARKER(DOT, DOT, DOT);

    final List<Symbol> symbols;
    final List<Predicate<CodePoint>> predicates;

    Marker(Symbol... symbols) {
        this.symbols = asList(symbols);
        this.predicates = this.symbols.stream()
            .flatMap(symbol -> symbol.getPredicates().stream())
            .collect(Collectors.toList());
    }

    @NotNull public List<Predicate<CodePoint>> getPredicates() {return this.predicates;}
}
