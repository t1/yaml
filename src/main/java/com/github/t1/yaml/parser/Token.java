package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.Symbol;
import com.github.t1.yaml.parser.YamlParseException;
import lombok.Getter;

import java.util.List;

import static com.github.t1.yaml.parser.Symbol.COLON;
import static com.github.t1.yaml.parser.Symbol.MINUS;
import static com.github.t1.yaml.parser.Symbol.PERIOD;
import static com.github.t1.yaml.parser.Symbol.WS;
import static java.util.Arrays.asList;

public enum Token {
    BLOCK_MAPPING_VALUE(COLON, WS),
    DIRECTIVES_END_MARKER(MINUS, MINUS, MINUS),
    DOCUMENT_END_MARKER(PERIOD, PERIOD, PERIOD);

    @Getter final List<Symbol> symbols;

    Token(Symbol... symbols) { this.symbols = asList(symbols); }

    public int length() { return symbols.size(); }

    public Symbol symbol(int i) { return symbols.get(i); }

    public boolean matches(int[] chars) {
        if (chars.length != length())
            throw new YamlParseException("Token length mismatch");
        for (int i = 0; i < length(); i++)
            if (!symbols.get(i).matches(chars[i]))
                return false;
        return true;
    }
}
