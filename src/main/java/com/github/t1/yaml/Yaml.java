package com.github.t1.yaml;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.parser.DocumentParser;
import com.github.t1.yaml.parser.YamlParseException;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.Reader;

public class Yaml {
    public static Document parseFirst(InputStream inputStream) { return parseFirst(new DocumentParser(inputStream)); }

    public static Document parseFirst(Reader reader) { return parseFirst(new DocumentParser(reader)); }

    public static Document parseFirst(String yaml) { return parseFirst(new DocumentParser(yaml)); }

    @NotNull private static Document parseFirst(DocumentParser parser) {
        return parser.document()
                .orElseThrow(() -> new YamlParseException("expected at least one document, but found none"));
    }


    public static Document parseSingle(InputStream inputStream) { return parseSingle(new DocumentParser(inputStream)); }

    public static Document parseSingle(Reader reader) { return parseSingle(new DocumentParser(reader)); }

    public static Document parseSingle(String yaml) { return parseSingle(new DocumentParser(yaml)); }

    @NotNull private static Document parseSingle(DocumentParser parser) {
        Document document = parser.document()
                .orElseThrow(() -> new YamlParseException("expected exactly one document, but found none"));
        if (parser.more())
            throw new YamlParseException("expected exactly one document, but found more: " + parser);
        return document;
    }


    public static Stream parseAll(InputStream inputStream) { return parseAll(new DocumentParser(inputStream)); }

    public static Stream parseAll(String yaml) { return parseAll(new DocumentParser(yaml)); }

    public static Stream parseAll(Reader reader) { return parseAll(new DocumentParser(reader)); }

    @NotNull private static Stream parseAll(DocumentParser parser) {
        Stream stream = new Stream();
        while (parser.more())
            parser.document().ifPresent(stream::document);
        return stream;
    }
}
