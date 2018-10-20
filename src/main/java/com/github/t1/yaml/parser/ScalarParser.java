package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.tools.Scanner;

import static com.github.t1.yaml.parser.Marker.BLOCK_SEQUENCE_START;
import static com.github.t1.yaml.parser.Symbol.FLOW_MAPPING_START;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_START;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.SPACE;

class ScalarParser {
    public static ScalarParser of(Scanner next, Nesting nesting) {
        int indent = next.count(SPACE);
        Quotes quotes = Quotes.recognize(next);
        return new ScalarParser(quotes, indent, next, nesting);
    }

    private final Quotes quotes;
    private final int indent;
    private final Scanner next;
    private final Nesting nesting;

    private final Scalar scalar;

    private ScalarParser(Quotes quotes, int indent, Scanner next, Nesting nesting) {
        this.quotes = quotes;
        this.indent = indent;
        this.next = next;

        this.scalar = new Scalar().style(quotes.style);
        this.nesting = nesting;
    }

    public Scalar scalar() {
        String text = quotes.scanLine(next);
        scalar.line(new Line().indent(indent).text(text));
        boolean lineContinue = next.accept(NL);
        while (lineContinue && next.more() && nesting.accept()) {
            if (next.is(FLOW_SEQUENCE_START))
                throw new YamlParseException("Expected a scalar node to continue with scalar values but found flow sequence " + next);
            if (next.is(BLOCK_SEQUENCE_START))
                throw new YamlParseException("Expected a scalar node to continue with scalar values but found block sequence " + next);
            if (next.is(FLOW_MAPPING_START))
                throw new YamlParseException("Expected a scalar node to continue with scalar values but found flow mapping " + next);
            // if (isBlockMapping())
            //     throw new YamlParseException("Expected a scalar node to continue with scalar values but found block mapping " + next);
            // if (next.accept(COMMENT)) {
            //     scalar.line(new Line().text(""));
            //     comment(scalar);
            // } else {
            scalar.line(new Line().indent(next.count(SPACE)).text(quotes.scanLine(next)));
            // }
            lineContinue = !next.accept(NL);
        }
        return scalar;
    }
}
