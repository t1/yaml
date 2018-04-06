package com.github.t1.yaml;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.parser.YamlParseException;
import com.github.t1.yaml.parser.YamlParser;

import java.io.StringReader;
import java.util.List;

public class Yaml {
    // TODO only parse first
    public static Document parseSingle(String yaml) {
        List<Document> documents = parseAll(yaml).documents();
        if (documents.size() != 1)
            throw new YamlParseException("expected exactly one document, but found " + documents.size());
        return documents.get(0);
    }

    // TODO only parse first
    public static Document parseFirst(String yaml) {
        List<Document> documents = parseAll(yaml).documents();
        if (documents.size() < 1)
            throw new YamlParseException("expected at least one document, but found none");
        return documents.get(0);
    }

    public static Stream parseAll(String yaml) { return new YamlParser(new StringReader(yaml)).stream(); }
}
