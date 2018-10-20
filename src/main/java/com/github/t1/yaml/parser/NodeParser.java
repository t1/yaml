package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Mapping;
import com.github.t1.yaml.model.Mapping.Entry;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.model.Sequence;
import com.github.t1.yaml.model.Sequence.Item;

import static com.github.t1.yaml.model.Collection.Style.BLOCK;
import static com.github.t1.yaml.model.Collection.Style.FLOW;
import static com.github.t1.yaml.parser.Symbol.COLON;
import static com.github.t1.yaml.parser.Symbol.COMMENT;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_END;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_ENTRY;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_ITEM_END;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_START;
import static com.github.t1.yaml.parser.Symbol.MINUS;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.QUESTION_MARK;
import static com.github.t1.yaml.parser.Symbol.SPACE;
import static com.github.t1.yaml.parser.Symbol.WS;

class NodeParser {
    private final YamlScanner next;
    private final Nesting nesting;

    NodeParser(YamlScanner next) {
        this.next = next;
        nesting = new Nesting(this.next);
    }

    @Override public String toString() { return "NodeParser " + next + " nesting: " + nesting; }

    Node node() {
        nesting.expect();
        if (next.isFlowSequence())
            return flowSequence();
        if (next.isBlockSequence())
            return blockSequence();
        if (next.isFlowMapping())
            return flowMapping();
        if (next.isBlockMapping())
            return blockMapping();
        return scalar();
    }

    private Node flowSequence() {
        next.expect(FLOW_SEQUENCE_START);
        Sequence sequence = new Sequence().style(FLOW);
        do
            sequence.item(flowSequenceItem());
        while (next.more() && next.accept(FLOW_SEQUENCE_ENTRY));
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

    private Sequence blockSequence() {
        Sequence sequence = new Sequence().style(BLOCK);
        do
            sequence.item(blockSequenceItem());
        while (next.more() && nesting.accept());
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

    private Mapping blockMapping() {
        Mapping mapping = new Mapping();
        do
            mapping.entry(blockMappingEntry());
        while (next.more() && nesting.accept());
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

        if (value instanceof Scalar && next.accept(COMMENT)) // TODO comments for non-scalars
            comment((Scalar) value);
    }

    private Mapping flowMapping() {
        throw new YamlParseException("unexpected " + next);
    }

    private Scalar scalar() {
        return ScalarParser.of(next, nesting).scalar();
    }

    private void comment(Scalar scalar) {
        next.accept(SPACE);
        Line line = scalar.lastLine();
        line.comment(new Comment().indent(line.rtrim()).text(next.readLine()));
    }
}
