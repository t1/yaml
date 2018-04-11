package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER;
import static com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER;
import static com.github.t1.yaml.parser.Symbol.HASH;
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
        new NodeParser(next).node().ifPresent(document::node);
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
