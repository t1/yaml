package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.Stream;

import java.io.Reader;
import java.util.Optional;

import static com.github.t1.yaml.model.Symbol.BOM;
import static com.github.t1.yaml.model.Symbol.HASH;
import static com.github.t1.yaml.model.Symbol.MINUS;
import static com.github.t1.yaml.model.Symbol.NL;
import static com.github.t1.yaml.model.Symbol.PERCENT;
import static com.github.t1.yaml.model.Symbol.PERIOD;
import static com.github.t1.yaml.model.Symbol.SPACE;
import static com.github.t1.yaml.model.Symbol.WS;

/**
 * A recursive decent parser producing a YAML {@link Stream}.
 *
 * @implNote Method naming conventions used:
 * Suffix after an underscore:
 * - none: Exactly one instance
 * - `_Optional`: Zero or one instance
 * - `_List`: Zero or more instances
 */
public class YamlParser {
    private final Scanner next;

    public YamlParser(Reader reader) { this.next = new Scanner(reader); }

    public Stream stream() {
        Stream stream = new Stream();
        next.skip(BOM);
        document_Optional().ifPresent(stream::document);
        if (next.more())
            throw new YamlParseException("unexpected characters after end: " + next);
        return stream;
    }

    private Comment comment() {
        next.skip(WS);
        return new Comment().text(next.readLine());
    }

    private Optional<Document> document_Optional() {
        if (next.end())
            return Optional.empty();

        return Optional.of(new DocumentParser().parse());
    }

    private class DocumentParser {
        Document document = new Document();

        private Document parse() {
            directives();
            prefixComments();
            node();
            documentEnd();
            return document;
        }

        private void directives() {
            if (next.accept(PERCENT))
                document.directive(directive());

            if (next.accept(MINUS)) {
                next.expect(MINUS).expect(MINUS).expect(NL);
                document.hasDirectivesEndMarker(true);
            }
        }

        private void prefixComments() {
            while (next.accept(HASH))
                document.prefixComment(comment());
        }

        private void node() {
            if (next.more())
                document.node(new ScalarNode().text(next.readLine()));
        }

        private void documentEnd() {
            if (next.accept(PERIOD)) {
                next.expect(PERIOD).expect(PERIOD);
                document.hasDocumentEndMarker(true);
                if (next.accept(SPACE)) {
                    next.expect(HASH);
                    document.suffixComment(comment());
                }
            }
        }
    }

    private Directive directive() {
        return new Directive(next.readWord(), next.readLine());
    }
}
