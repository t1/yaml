package com.github.t1.yaml.parser;

import lombok.Getter;

import java.util.List;

import static com.github.t1.yaml.parser.Symbol.COLON;
import static com.github.t1.yaml.parser.Symbol.MINUS;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.PERIOD;
import static com.github.t1.yaml.parser.Symbol.WS;
import static java.util.Arrays.asList;

public enum Token {
    EOL(NL),
    BLOCK_MAPPING_VALUE(COLON, WS),
    DIRECTIVES_END_MARKER(MINUS, MINUS, MINUS),
    DOCUMENT_END_MARKER(PERIOD, PERIOD, PERIOD);

    @Getter final List<Symbol> symbols;

    Token(Symbol... symbols) { this.symbols = asList(symbols); }

    public int length() { return symbols.size(); }

    public Symbol symbol(int i) { return symbols.get(i); }

    public boolean matches(List<CodePoint> codePoints) {
        if (codePoints == null || codePoints.size() != length())
            return false;
        for (int i = 0; i < length(); i++)
            if (!symbols.get(i).matches(codePoints.get(i)))
                return false;
        return true;
    }
}
