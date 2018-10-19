package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Scalar.Style;
import com.github.t1.yaml.tools.Scanner;
import lombok.RequiredArgsConstructor;

import static com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED;
import static com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED;
import static com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Symbol.DOUBLE_QUOTE;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.NL_OR_COMMENT;
import static com.github.t1.yaml.parser.Symbol.SINGLE_QUOTE;

@RequiredArgsConstructor
public enum Quotes {
    PLAIN(NL_OR_COMMENT, Style.PLAIN) {
        @Override public String scanLine(Scanner next) {
            StringBuilder builder = new StringBuilder();
            while (next.more() && !next.is(NL_OR_COMMENT) && !next.is(BLOCK_MAPPING_VALUE))
                builder.appendCodePoint(next.read().value);
            return builder.toString();
        }
    },

    SINGLE(SINGLE_QUOTE, SINGLE_QUOTED) {
        @Override public String scanLine(Scanner next) {
            StringBuilder out = new StringBuilder();
            while (next.more() && !next.is(NL) && !next.accept(SINGLE_QUOTE))
                if (next.accept("''"))
                    out.append(SINGLE_QUOTE);
                else
                    out.appendCodePoint(next.read().value);
            return out.toString();
        }
    },

    DOUBLE(DOUBLE_QUOTE, DOUBLE_QUOTED) {
        @Override public String scanLine(Scanner next) {
            StringBuilder out = new StringBuilder();
            while (next.more() && !next.is(DOUBLE_QUOTE)) {
                if (next.accept("\\"))
                    out.append("\\");
                out.appendCodePoint(next.read().value);
            }
            if (next.more())
                next.expect(DOUBLE_QUOTE);
            return out.toString();
        }
    };

    public static Quotes recognize(Scanner scanner) {
        if (scanner.accept(SINGLE_QUOTE))
            return SINGLE;
        if (scanner.accept(DOUBLE_QUOTE))
            return DOUBLE;
        return PLAIN;
    }

    public final Symbol symbol;
    public final Style style;

    public abstract String scanLine(Scanner scanner);
}
