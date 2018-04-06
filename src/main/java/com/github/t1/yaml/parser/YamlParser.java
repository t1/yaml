package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;

import java.io.Reader;
import java.util.Optional;

import static com.github.t1.yaml.model.Symbol.BOM;

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
        while (next.more())
            document_Optional().ifPresent(stream::document);
        return stream;
    }

    private Optional<Document> document_Optional() {
        if (next.end())
            return Optional.empty();

        return Optional.of(new DocumentParser(next).parse());
    }
}
