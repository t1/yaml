package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Mapping;
import com.github.t1.yaml.model.Mapping.Entry;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.model.Sequence;
import com.github.t1.yaml.model.Sequence.Item;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static com.github.t1.yaml.model.Collection.Style.BLOCK;
import static com.github.t1.yaml.model.Collection.Style.FLOW;
import static com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Marker.BLOCK_SEQUENCE_START;
import static com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER;
import static com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER;
import static com.github.t1.yaml.parser.Symbol.COLON;
import static com.github.t1.yaml.parser.Symbol.COMMENT;
import static com.github.t1.yaml.parser.Symbol.FLOW_MAPPING_START;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_END;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_ENTRY;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_ITEM_END;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_START;
import static com.github.t1.yaml.parser.Symbol.MINUS;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.QUESTION_MARK;
import static com.github.t1.yaml.parser.Symbol.SPACE;
import static com.github.t1.yaml.parser.Symbol.WS;
import static com.github.t1.yaml.tools.Tools.spaces;

@RequiredArgsConstructor
public class NodeParser {
    private final Scanner next;
    private final Nesting nesting = new Nesting();

    class Nesting {
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

    public Node node() {
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

    private boolean isFlowSequence() { return next.is(FLOW_SEQUENCE_START); }

    private Node flowSequence() {
        next.expect(FLOW_SEQUENCE_START);
        Sequence sequence = new Sequence().style(FLOW);
        do
            sequence.item(flowSequenceItem());
        while (more() && next.accept(FLOW_SEQUENCE_ENTRY));
        next.expect(FLOW_SEQUENCE_END);
        next.skip(WS);
        return sequence;
    }

    private Item flowSequenceItem() {
        next.skip(SPACE);
        String line = next.readUntil(FLOW_SEQUENCE_ITEM_END); // TODO this must be a call to node()!
        next.skip(SPACE);
        return new Item().node(new Scalar().line(line));
    }

    private boolean isBlockSequence() { return next.is(BLOCK_SEQUENCE_START); }

    private Sequence blockSequence() {
        Sequence sequence = new Sequence().style(BLOCK);
        do
            sequence.item(blockSequenceItem());
        while (more() && nesting.accept());
        return sequence;
    }

    private Item blockSequenceItem() {
        next.expect(MINUS);
        boolean nlItem = next.accept(NL);
        if (!nlItem) {
            next.expect(SPACE);
            nesting.skipNext(true);
        }
        Item item = new Item().nl(nlItem);
        nesting.up();
        item.node(node());
        nesting.down();
        return item;
    }

    private boolean isBlockMapping() {
        if (next.is(QUESTION_MARK))
            return true;
        String token = next.peekUntil(BLOCK_MAPPING_VALUE);
        return token != null && token.length() >= 1 && !token.contains("\n");
    }

    private Mapping blockMapping() {
        Mapping mapping = new Mapping();
        do
            mapping.entry(blockMappingEntry());
        while (more() && nesting.accept());
        return mapping;
    }

    private Entry blockMappingEntry() {
        Entry entry = new Entry();
        blockMappingKey(entry);
        blockMappingValue(entry);
        return entry;
    }

    private void blockMappingKey(Entry entry) {
        entry.hasMarkedKey(next.accept(QUESTION_MARK));
        if (entry.hasMarkedKey())
            next.expect(SPACE);
        entry.key(scalar()); // TODO key node()
    }

    private void blockMappingValue(Entry entry) {
        next.expect(COLON);
        entry.hasNlAfterKey(next.accept(NL));
        if (!entry.hasNlAfterKey()) {
            next.expect(SPACE);
            nesting.skipNext(true);
        }

        nesting.up();
        Node value = node();
        nesting.down();
        entry.value(value);

        if (value instanceof Scalar && isComment()) // TODO comments for non-scalars
            comment((Scalar) value);
    }

    private boolean isFlowMapping() {
        return next.is(FLOW_MAPPING_START);
    }

    private Mapping flowMapping() {
        throw new YamlParseException("unexpected " + next);
    }

    private Scalar scalar() {
        return ScalarParser.of(next, nesting).scalar();
    }

    private boolean isComment() { return next.accept(COMMENT); }

    private void comment(Scalar scalar) {
        next.accept(SPACE);
        Line line = scalar.lastLine();
        line.comment(new Comment().indent(line.rtrim()).text(next.readLine()));
    }

    boolean more() {
        return next.more()
            && !next.is(DOCUMENT_END_MARKER)
            && !next.is(DIRECTIVES_END_MARKER); // of next document
    }
}
