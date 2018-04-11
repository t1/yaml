package com.github.t1.yaml.parser;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.t1.yaml.parser.Symbol.COLON;
import static com.github.t1.yaml.parser.Symbol.MINUS;
import static com.github.t1.yaml.parser.Symbol.PERIOD;
import static com.github.t1.yaml.parser.Symbol.WS;
import static java.util.Arrays.asList;

public enum Marker implements Token {
    BLOCK_MAPPING_VALUE(COLON, WS),
    DIRECTIVES_END_MARKER(MINUS, MINUS, MINUS),
    DOCUMENT_END_MARKER(PERIOD, PERIOD, PERIOD);

    @Getter final List<Symbol> symbols;
    @Getter @Accessors(fluent = true) final List<Predicate<CodePoint>> predicates;

    Marker(Symbol... symbols) {
        this.symbols = asList(symbols);
        this.predicates = this.symbols.stream()
                .flatMap(symbol -> symbol.predicates().stream())
                .collect(Collectors.toList());
    }

    public int length() { return symbols.size(); }

    public Symbol symbol(int i) { return symbols.get(i); }
}
