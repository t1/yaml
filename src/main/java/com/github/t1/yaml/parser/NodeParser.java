package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.MappingNode;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.ScalarNode.Style;
import com.github.t1.yaml.model.SequenceNode;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER;
import static com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER;
import static com.github.t1.yaml.parser.Quotes.PLAIN;
import static com.github.t1.yaml.parser.Symbol.CURLY_OPEN;
import static com.github.t1.yaml.parser.Symbol.C_MAPPING_KEY;
import static com.github.t1.yaml.parser.Symbol.MINUS;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.SPACE;

@RequiredArgsConstructor
public class NodeParser {
    private final Scanner next;

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
        return scalarNode();
    }

    private boolean isBlockSequence() {
        return next.is(MINUS);
    }

    private SequenceNode blockSequence() {
        SequenceNode node = new SequenceNode();
        while (more()) {
            next.expect(MINUS).expect(SPACE);
            node.entry(new ScalarNode().line(next.readLine()));
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
            boolean markedKey = next.accept(C_MAPPING_KEY);
            if (markedKey)
                next.skip(SPACE);
            ScalarNode key = scalar(BLOCK_MAPPING_VALUE);
            next.expect(BLOCK_MAPPING_VALUE);
            ScalarNode value = scalar(NL);
            mappingNode.entry(new MappingNode.Entry().hasMarkedKey(markedKey).key(key).value(value));
        }
        return mappingNode;
    }

    private boolean isFlowMapping() {
        return next.is(CURLY_OPEN);
    }

    private MappingNode flowMapping() {
        throw new YamlParseException("unexpected " + next);
    }

    private ScalarNode scalarNode() {
        ScalarNode node = scalar(NL);
        if (node.style() == Style.PLAIN)
            while (more()) {
                if (isBlockSequence())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found block sequence at " + next);
                if (isFlowMapping())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found flow mapping at " + next);
                if (isBlockMapping())
                    throw new YamlParseException("Expected a scalar node to continue with scalar values but found block mapping at " + next);
                node.line(PLAIN.scan(next));
            }
        return node;
    }

    private ScalarNode scalar(Token end) {
        Quotes quotes = Quotes.recognize(next);
        ScalarNode node = new ScalarNode().style(quotes.style)
                .line((quotes == PLAIN) ? next.readUntil(end) : quotes.scan(next));
        next.skip(NL);
        return node;
    }

    private boolean more() {
        return next.more()
                && !next.is(DOCUMENT_END_MARKER)
                && !next.is(DIRECTIVES_END_MARKER); // of next document
    }
}
