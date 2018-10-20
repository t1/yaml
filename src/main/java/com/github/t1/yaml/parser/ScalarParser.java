package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.model.Scalar.Style;

import static com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED;
import static com.github.t1.yaml.model.Scalar.Style.PLAIN;
import static com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED;
import static com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Symbol.DOUBLE_QUOTE;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.NL_OR_COMMENT;
import static com.github.t1.yaml.parser.Symbol.SINGLE_QUOTE;
import static com.github.t1.yaml.parser.Symbol.SPACE;

class ScalarParser {
    public static ScalarParser of(YamlScanner next, Nesting nesting) {
        int indent = next.count(SPACE);
        Style style = recognize(next);
        return new ScalarParser(indent, next, nesting, style);
    }

    private static Style recognize(YamlScanner scanner) {
        if (scanner.accept(SINGLE_QUOTE))
            return SINGLE_QUOTED;
        if (scanner.accept(DOUBLE_QUOTE))
            return DOUBLE_QUOTED;
        return PLAIN;
    }

    private final int indent;
    private final YamlScanner next;
    private final Nesting nesting;

    private final Scalar scalar;

    private ScalarParser(int indent, YamlScanner next, Nesting nesting, Style style) {
        this.indent = indent;
        this.next = next;
        this.nesting = nesting;

        this.scalar = new Scalar().style(style);
    }

    public Scalar scalar() {
        String text = scanLine();
        scalar.line(new Line().indent(indent).text(text));
        boolean lineContinue = next.accept(NL);
        while (lineContinue && next.more() && nesting.accept()) {
            if (next.isFlowSequence())
                throw new YamlParseException("Expected a scalar node to continue with scalar values but found flow sequence " + next);
            if (next.isBlockSequence())
                throw new YamlParseException("Expected a scalar node to continue with scalar values but found block sequence " + next);
            if (next.isFlowMapping())
                throw new YamlParseException("Expected a scalar node to continue with scalar values but found flow mapping " + next);
            if (next.isBlockMapping())
                throw new YamlParseException("Expected a scalar node to continue with scalar values but found block mapping " + next);
            // if (next.accept(COMMENT)) {
            //     scalar.line(new Line().text(""));
            //     comment(scalar);
            // } else {
            scalar.line(new Line().indent(next.count(SPACE)).text(scanLine()));
            // }
            lineContinue = !next.accept(NL);
        }
        return scalar;
    }

    private String scanLine() {
        switch (scalar.style()) {
            case PLAIN:
                return scanPlain();
            case SINGLE_QUOTED:
                return scanSingleQuoted();
            case DOUBLE_QUOTED:
                return scanDoubleQuoted();
        }
        throw new UnsupportedOperationException("unreachable");
    }

    private String scanPlain() {
        StringBuilder builder = new StringBuilder();
        while (next.more() && !next.is(NL_OR_COMMENT) && !next.is(BLOCK_MAPPING_VALUE))
            builder.appendCodePoint(next.read().getValue());
        return builder.toString();
    }

    private String scanSingleQuoted() {
        StringBuilder out = new StringBuilder();
        while (next.more() && !next.is(NL) && !next.accept(SINGLE_QUOTE))
            if (next.accept("''"))
                out.append(SINGLE_QUOTE);
            else
                out.appendCodePoint(next.read().getValue());
        return out.toString();
    }

    private String scanDoubleQuoted() {
        StringBuilder out = new StringBuilder();
        while (next.more() && !next.is(DOUBLE_QUOTE)) {
            if (next.accept("\\"))
                out.append("\\");
            out.appendCodePoint(next.read().getValue());
        }
        if (next.more())
            next.expect(DOUBLE_QUOTE);
        return out.toString();
    }
}
