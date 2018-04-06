package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.ScalarNode;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.github.t1.yaml.model.Symbol.CURLY_OPEN;
import static com.github.t1.yaml.model.Symbol.HASH;
import static com.github.t1.yaml.model.Symbol.MINUS;
import static com.github.t1.yaml.model.Symbol.NL;
import static com.github.t1.yaml.model.Symbol.PERCENT;
import static com.github.t1.yaml.model.Symbol.PERIOD;
import static com.github.t1.yaml.model.Symbol.SPACE;
import static com.github.t1.yaml.model.Symbol.WS;

@RequiredArgsConstructor public class DocumentParser {
    private final Scanner next;
    private final Document document = new Document();

    public Optional<Document> document() {
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

        if (next.accept(MINUS)) {
            next.expect(MINUS).expect(MINUS).expect(NL);
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
            if (next.is(CURLY_OPEN))
                throw new YamlParseException("unexpected " + next);
            ScalarNode node = new ScalarNode();
            while (more())
                node.line(next.readLine());
            document.node(node);
        }
    }

    private boolean more() {
        return next.more() && !next.is(MINUS) && !next.is(PERIOD);
    }


    private void documentEnd() {
        if (next.accept(PERIOD)) {
            next.expect(PERIOD).expect(PERIOD);
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
