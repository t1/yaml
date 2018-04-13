package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.t1.yaml.parser.Symbol.C_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Symbol.C_SEQUENCE_ENTRY;
import static com.github.t1.yaml.parser.Symbol.PERIOD;
import static com.github.t1.yaml.parser.Symbol.WS;
import static java.util.Arrays.asList;

public enum Marker implements Token {
    BLOCK_MAPPING_VALUE(C_MAPPING_VALUE, WS),
    DIRECTIVES_END_MARKER(C_SEQUENCE_ENTRY, C_SEQUENCE_ENTRY, C_SEQUENCE_ENTRY),
    DOCUMENT_END_MARKER(PERIOD, PERIOD, PERIOD);

    @Getter final List<Symbol> symbols;
    @Getter @Accessors(fluent = true) final List<Predicate<CodePoint>> predicates;

    Marker(Symbol... symbols) {
        this.symbols = asList(symbols);
        this.predicates = this.symbols.stream()
                .flatMap(symbol -> symbol.predicates().stream())
                .collect(Collectors.toList());
    }
}