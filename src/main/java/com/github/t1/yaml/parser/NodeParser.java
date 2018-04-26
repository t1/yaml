package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.MappingNode;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.ScalarNode.Line;
import com.github.t1.yaml.model.ScalarNode.Style;
import com.github.t1.yaml.model.SequenceNode;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.github.t1.yaml.dump.Tools.spaces;
import static com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Marker.BLOCK_SEQUENCE_ENTRY;
import static com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER;
import static com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER;
import static com.github.t1.yaml.parser.Quotes.PLAIN;
import static com.github.t1.yaml.parser.Symbol.CURLY_OPEN;
import static com.github.t1.yaml.parser.Symbol.C_COMMENT;
import static com.github.t1.yaml.parser.Symbol.C_MAPPING_KEY;
import static com.github.t1.yaml.parser.Symbol.C_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Symbol.C_SEQUENCE_ENTRY;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.SCALAR_END;
import static com.github.t1.yaml.parser.Symbol.SPACE;

@RequiredArgsConstructor
public class NodeParser {
    private final Scanner next;
    private int nesting;

    @Override public String toString() { return "NodeParser " + next + " nesting: " + nesting; }

    public Optional<Node> node() {
        return more() ? Optional.of(node_()) : Optional.empty();
    }

    private Node node_() {
        if (isBlockSequence())
            return blockSequence();
        if (isFlowMapping())
            return flowMapping();
        if (isBlockMapping())
            return blockMapping();
        return scalar();
    }

    private boolean isBlockSequence() { return next.is(BLOCK_SEQUENCE_ENTRY); }

    private SequenceNode blockSequence() {
        SequenceNode node = new SequenceNode();
        while (more()) {
            next.expect(C_SEQUENCE_ENTRY).expect(SPACE);
            nesting++;
            node.entry(scalar());
            nesting--;
        }
        return node;
    }

    private boolean isBlockMapping() {
        if (next.is(C_MAPPING_KEY))
            return true;
        String token = next.peekUntil(BLOCK_MAPPING_VALUE);
        return token != null && token.length() >= 1 && !token.contains("\n");
    }

    private MappingNode blockMapping() {
        MappingNode mappingNode = new MappingNode();
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
            ScalarNode valueNode = scalar(SCALAR_END);
            entry.value(valueNode);
            if (isComment())
                comment(valueNode, true);
            else if (next.more())
                next.expect(NL);
            mappingNode.entry(entry);
        }
        return mappingNode;
    }

    private boolean isFlowMapping() {
        return next.is(CURLY_OPEN);
    }

    private MappingNode flowMapping() {
        throw new YamlParseException("unexpected " + next);
    }

    private ScalarNode scalar() {
        ScalarNode node = scalar(SCALAR_END);
        if (node.style() == Style.PLAIN) {
            boolean lineContinue = !next.accept(NL);
            while (more() && next.accept(indent())) {
                if (isBlockSequence())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found block sequence at " + next);
                if (isFlowMapping())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found flow mapping at " + next);
                if (isBlockMapping())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found block mapping at " + next);
                if (isComment()) {
                    comment(node, lineContinue);
                    lineContinue = false;
                } else {
                    node.line(new Line().indent(nesting * 2 + next.count(SPACE)).text(next.readUntil(SCALAR_END)));
                    lineContinue = !next.accept(NL);
                }
            }
        }
        return node;
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

    private void comment(ScalarNode node, boolean lineContinue) {
        next.accept(SPACE);
        Line line = line(node, lineContinue);
        line.comment(new Comment().indent(line.rtrim()).text(next.readLine()));
    }

    private Line line(ScalarNode node, boolean lineContinue) {
        if (lineContinue)
            return node.lastLine();
        Line line = new Line();
        node.line(line);
        return line;
    }

    private boolean more() {
        return next.more()
                && !next.is(DOCUMENT_END_MARKER)
                && !next.is(DIRECTIVES_END_MARKER); // of next document
    }

    private String indent() { return spaces(nesting * 2); }
}
