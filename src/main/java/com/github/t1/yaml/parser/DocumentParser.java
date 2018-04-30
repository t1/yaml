package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.tools.CodePoint;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;

import static com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER;
import static com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER;
import static com.github.t1.yaml.parser.Symbol.COMMENT;
import static com.github.t1.yaml.parser.Symbol.DIRECTIVE;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.SCALAR_END;
import static com.github.t1.yaml.parser.Symbol.SPACE;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DocumentParser {
    private final Scanner next;
    private Document document;

    public DocumentParser(String yaml) { this(new StringReader(yaml)); }

    public DocumentParser(InputStream inputStream) { this(new BufferedReader(new InputStreamReader(inputStream, UTF_8))); }

    public DocumentParser(Reader reader) { this.next = new Scanner(reader); }

    public Optional<Document> document() {
        this.document = new Document();

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
        if (next.accept(DIRECTIVE))
            document.directive(directive());

        if (next.accept(DIRECTIVES_END_MARKER)) {
            document.hasDirectivesEndMarker(true);
            next.expect(NL);
        }
    }

    private Directive directive() {
        return new Directive(next.readWord(), next.readLine());
    }


    private void prefixComments() {
        while (isIndentedComment())
            document.prefixComment(comment());
    }

    private boolean isIndentedComment() {
        String spaces = next.peekUntil(SCALAR_END);
        return spaces != null
                && CodePoint.stream(spaces).allMatch(SPACE)
                && next.peekAfter(spaces.length()).map(COMMENT::test).orElse(false);
    }

    private Comment comment() {
        return new Comment().indent(next.count(SPACE))
                .text(next.expect(COMMENT).skip(SPACE).readLine());
    }


    private void node() {
        NodeParser parser = new NodeParser(next);
        if (parser.more())
            document.node(parser.node());
    }

    private void documentEnd() {
        if (next.accept(DOCUMENT_END_MARKER)) {
            document.hasDocumentEndMarker(true);
            if (next.is(SPACE))
                document.suffixComment(comment());
            else
                next.accept(NL);
        }
    }

    public boolean more() { return next.more(); }

    @Override public String toString() { return next.toString(); }
}
