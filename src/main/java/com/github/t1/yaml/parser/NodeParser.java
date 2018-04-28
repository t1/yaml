package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.MappingNode;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.ScalarNode.Line;
import com.github.t1.yaml.model.ScalarNode.Style;
import com.github.t1.yaml.model.SequenceNode;
import com.github.t1.yaml.model.SequenceNode.Item;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Optional;

import static com.github.t1.yaml.dump.Tools.spaces;
import static com.github.t1.yaml.model.CollectionNode.Style.BLOCK;
import static com.github.t1.yaml.model.CollectionNode.Style.FLOW;
import static com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Marker.BLOCK_SEQUENCE_ENTRY;
import static com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER;
import static com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER;
import static com.github.t1.yaml.parser.Quotes.PLAIN;
import static com.github.t1.yaml.parser.Symbol.CURLY_OPEN;
import static com.github.t1.yaml.parser.Symbol.C_COLLECT_ENTRY;
import static com.github.t1.yaml.parser.Symbol.C_COMMENT;
import static com.github.t1.yaml.parser.Symbol.C_MAPPING_KEY;
import static com.github.t1.yaml.parser.Symbol.C_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Symbol.C_SEQUENCE_END;
import static com.github.t1.yaml.parser.Symbol.C_SEQUENCE_ENTRY;
import static com.github.t1.yaml.parser.Symbol.C_SEQUENCE_START;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_ITEM_END;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.SCALAR_END;
import static com.github.t1.yaml.parser.Symbol.SPACE;

@RequiredArgsConstructor
public class NodeParser {
    private final Scanner next;
    private final Nesting nesting = new Nesting();

    private class Nesting {
        private int level;
        @Setter private boolean skipNext;

        @Override public String toString() { return "Nesting:" + level + (skipNext ? " skip next" : ""); }

        void up() { level++; }

        void down() { level--; }

        void expect() {
            next.expect(indent());
            skipNext = false;
        }

        boolean accept() {
            boolean accepted = next.accept(indent());
            if (accepted)
                skipNext = false;
            return accepted;
        }

        private String indent() { return skipNext ? "" : spaces(level * 2); }
    }

    @Override public String toString() { return "NodeParser " + next + " nesting: " + nesting; }

    public Optional<Node> node() {
        return more() ? Optional.of(node_()) : Optional.empty();
    }

    private Node node_() {
        nesting.expect();
        if (isFlowSequence())
            return flowSequence();
        if (isBlockSequence())
            return blockSequence();
        if (isFlowMapping())
            return flowMapping();
        if (isBlockMapping())
            return blockMapping();
        return scalar();
    }

    private boolean isFlowSequence() { return next.is(C_SEQUENCE_START); }

    private Node flowSequence() {
        next.expect(C_SEQUENCE_START);
        SequenceNode sequence = new SequenceNode().style(FLOW);
        do
            sequence.item(flowSequenceItem());
        while (more() && next.accept(C_COLLECT_ENTRY));
        next.expect(C_SEQUENCE_END);
        return sequence;
    }

    private Item flowSequenceItem() {
        next.skip(SPACE);
        String line = next.readUntil(FLOW_SEQUENCE_ITEM_END); // TODO this must be a call to node()!
        next.skip(SPACE);
        return new Item().node(new ScalarNode().line(line));
    }

    private boolean isBlockSequence() { return next.is(BLOCK_SEQUENCE_ENTRY); }

    private SequenceNode blockSequence() {
        SequenceNode sequence = new SequenceNode().style(BLOCK);
        do
            sequence.item(blockSequenceItem());
        while (more() && nesting.accept());
        return sequence;
    }

    private Item blockSequenceItem() {
        next.expect(C_SEQUENCE_ENTRY);
        boolean nlItem = next.accept(NL);
        if (!nlItem) {
            next.expect(SPACE);
            nesting.skipNext(true);
        }
        Item item = new Item().nl(nlItem);
        nesting.up();
        item.node(node_());
        nesting.down();
        return item;
    }

    private boolean isBlockMapping() {
        if (next.is(C_MAPPING_KEY))
            return true;
        String token = next.peekUntil(BLOCK_MAPPING_VALUE);
        return token != null && token.length() >= 1 && !token.contains("\n");
    }

    private MappingNode blockMapping() {
        MappingNode mapping = new MappingNode();
        while (next.more()) {
            MappingNode.Entry entry = new MappingNode.Entry();
            entry.hasMarkedKey(next.accept(C_MAPPING_KEY));
            if (entry.hasMarkedKey())
                next.count(SPACE);
            entry.key(scalar(BLOCK_MAPPING_VALUE));
            next.expect(C_MAPPING_VALUE);
            if (next.accept(NL))
                entry.hasNlAfterKey(true);
            else
                next.expect(SPACE);
            ScalarNode value = scalar(SCALAR_END);
            entry.value(value);
            if (isComment())
                comment(value, true);
            else if (next.more())
                next.expect(NL);
            mapping.entry(entry);
        }
        return mapping;
    }

    private boolean isFlowMapping() {
        return next.is(CURLY_OPEN);
    }

    private MappingNode flowMapping() {
        throw new YamlParseException("unexpected " + next);
    }

    private ScalarNode scalar() {
        ScalarNode scalar = scalar(SCALAR_END);
        if (scalar.style() == Style.PLAIN) {
            boolean lineContinue = !next.accept(NL);
            while (more() && nesting.accept()) {
                if (isFlowSequence())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found flow sequence " + next);
                if (isBlockSequence())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found block sequence " + next);
                if (isFlowMapping())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found flow mapping " + next);
                if (isBlockMapping())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found block mapping " + next);
                if (isComment()) {
                    comment(scalar, lineContinue);
                    lineContinue = false;
                } else {
                    scalar.line(new Line().indent(next.count(SPACE)).text(next.readUntil(SCALAR_END)));
                    lineContinue = !next.accept(NL);
                }
            }
        }
        return scalar;
    }

    private ScalarNode scalar(Token end) {
        int indent = next.count(SPACE);
        Quotes quotes = Quotes.recognize(next);
        return new ScalarNode()
                .style(quotes.style)
                .line(new Line()
                        .indent(indent)
                        .text((quotes == PLAIN) ? next.readUntil(end) : quotes.scan(next))
                );
    }

    private boolean isComment() { return next.accept(C_COMMENT); }

    private void comment(ScalarNode scalar, boolean lineContinue) {
        next.accept(SPACE);
        Line line = line(scalar, lineContinue);
        line.comment(new Comment().indent(line.rtrim()).text(next.readLine()));
    }

    private Line line(ScalarNode scalar, boolean lineContinue) {
        if (lineContinue)
            return scalar.lastLine();
        Line line = new Line();
        scalar.line(line);
        return line;
    }

    private boolean more() {
        return next.more()
                && !next.is(DOCUMENT_END_MARKER)
                && !next.is(DIRECTIVES_END_MARKER); // of next document
    }
}
