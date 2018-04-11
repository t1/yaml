package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
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
import static com.github.t1.yaml.parser.Symbol.HASH;
import static com.github.t1.yaml.parser.Symbol.MINUS;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.PERCENT;
import static com.github.t1.yaml.parser.Symbol.SPACE;
import static com.github.t1.yaml.parser.Symbol.WS;

@RequiredArgsConstructor public class DocumentParser {
    private final Scanner next;
    private final Document document = new Document();

    public Optional<Document> document() {
        next.acceptBom();

        if (next.end())
            return Optional.empty();

        directives();
        prefixComments();
        node();
        documentEnd();

        return Optional.of(document);
    }

    private void directives() {
        if (next.accept(PERCENT))
            document.directive(directive());

        if (next.accept(DIRECTIVES_END_MARKER)) {
            next.expect(NL);
            document.hasDirectivesEndMarker(true);
        }
    }

    private Directive directive() {
        return new Directive(next.readWord(), next.readLine());
    }


    private void prefixComments() {
        while (next.accept(HASH))
            document.prefixComment(comment());
    }

    private Comment comment() {
        next.skip(WS);
        return new Comment().text(next.readLine());
    }


    private void node() {
        if (more()) {
            Node node;
            if (isBlockSequence())
                node = blockSequence();
            else if (isFlowMapping())
                node = flowMapping();
            else if (isBlockMapping())
                node = blockMapping();
            else
                node = scalarNode();
            document.node(node);
        }
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
        String token = next.peekUntil(BLOCK_MAPPING_VALUE);
        return token != null && token.length() >= 1 && !token.contains("\n");
    }

    private MappingNode blockMapping() {
        MappingNode mappingNode = new MappingNode();
        while (next.more()) {
            ScalarNode key = scalar(BLOCK_MAPPING_VALUE);
            next.expect(BLOCK_MAPPING_VALUE);
            ScalarNode value = scalar(NL);
            mappingNode.entry(key, value);
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


    private void documentEnd() {
        if (next.accept(DOCUMENT_END_MARKER)) {
            document.hasDocumentEndMarker(true);
            if (next.accept(SPACE)) {
                next.expect(HASH);
                document.suffixComment(comment());
            } else {
                next.accept(NL);
            }
        }
    }
}
