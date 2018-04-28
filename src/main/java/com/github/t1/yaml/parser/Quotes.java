package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Scalar.Style;
import lombok.RequiredArgsConstructor;

import static com.github.t1.yaml.parser.Symbol.SCALAR_END;
import static com.github.t1.yaml.parser.Symbol.DOUBLE_QUOTE;
import static com.github.t1.yaml.parser.Symbol.SINGLE_QUOTE;

@RequiredArgsConstructor
public enum Quotes {
    PLAIN(SCALAR_END, Style.PLAIN),
    SINGLE(SINGLE_QUOTE, Style.SINGLE_QUOTED),
    DOUBLE(DOUBLE_QUOTE, Style.DOUBLE_QUOTED);

    public static Quotes recognize(Scanner scanner) {
        if (scanner.accept(SINGLE_QUOTE))
            return SINGLE;
        if (scanner.accept(DOUBLE_QUOTE))
            return DOUBLE;
        return PLAIN;
    }

    public final Symbol symbol;
    public final Style style;

    public String scan(Scanner scanner) {
        String string = scanner.readUntilAndSkip(symbol);
        if (string == null)
            throw new YamlParseException("expected " + style + " string to end with " + symbol + " but found " + scanner);
        return string;
    }
}
