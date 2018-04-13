package com.github.t1.yaml;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.parser.DocumentParser;
import com.github.t1.yaml.parser.YamlParseException;

public class Yaml {
    public static Document parseSingle(String yaml) {
        DocumentParser parser = new DocumentParser(yaml);
        Document document = parser.document()
                .orElseThrow(() -> new YamlParseException("expected exactly one document, but found none"));
        if (parser.more())
            throw new YamlParseException("expected exactly one document, but found more: " + parser);
        return document;
    }

    public static Document parseFirst(String yaml) {
        DocumentParser parser = new DocumentParser(yaml);
        return parser.document()
                .orElseThrow(() -> new YamlParseException("expected at least one document, but found none"));
    }

    public static Stream parseAll(String yaml) {
        DocumentParser parser = new DocumentParser(yaml);
        Stream stream = new Stream();
        while (parser.more())
            parser.document().ifPresent(stream::document);
        return stream;
    }
}
