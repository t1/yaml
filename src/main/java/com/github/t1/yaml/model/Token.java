package com.github.t1.yaml.model;

import com.github.t1.yaml.parser.YamlParseException;
import lombok.Getter;

import java.util.List;

import static com.github.t1.yaml.model.Symbol.MINUS;
import static com.github.t1.yaml.model.Symbol.PERIOD;
import static java.util.Arrays.asList;

public enum Token {
    DIRECTIVES_END_MARKER(MINUS, MINUS, MINUS),
    DOCUMENT_END_MARKER(PERIOD, PERIOD, PERIOD);

    @Getter private final List<Symbol> symbols;

    Token(Symbol... symbols) { this.symbols = asList(symbols); }

    public int length() { return symbols.size(); }

    public boolean matches(int[] chars) {
        if (chars.length != length())
            throw new YamlParseException("Token length mismatch");
        for (int i = 0; i < length(); i++)
            if (!symbols.get(i).matches(chars[i]))
                return false;
        return true;
    }
}
