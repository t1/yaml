package com.github.t1.yaml;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.parser.DocumentParser;
import com.github.t1.yaml.parser.Scanner;
import com.github.t1.yaml.parser.StreamParser;
import com.github.t1.yaml.parser.YamlParseException;

import java.io.StringReader;

public class Yaml {
    private static Scanner scanner(String yaml) { return new Scanner(new StringReader(yaml)); }

    public static Document parseSingle(String yaml) { return parseSingle(scanner(yaml)); }

    public static Document parseSingle(Scanner scanner) {
        Document document = parseFirst(scanner, "expected exactly one document, but found none");
        if (scanner.more())
            throw new YamlParseException("expected exactly one document, but found more: " + scanner);
        return document;
    }

    public static Document parseFirst(String yaml) { return parseFirst(scanner(yaml)); }

    public static Document parseFirst(Scanner scanner) {
        return parseFirst(scanner, "expected at least one document, but found none");
    }

    private static Document parseFirst(Scanner scanner, String noneFoundMessage) {
        return new DocumentParser(scanner).document()
                .orElseThrow(() -> new YamlParseException(noneFoundMessage));
    }

    public static Stream parseAll(String yaml) { return parseAll(scanner(yaml)); }

    public static Stream parseAll(Scanner scanner) { return new StreamParser(scanner).stream(); }
}
