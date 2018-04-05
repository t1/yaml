package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;

import java.io.Reader;

import static com.github.t1.yaml.parser.Token.HASH;
import static com.github.t1.yaml.parser.Token.NL;
import static com.github.t1.yaml.parser.Token.WS;

public class YamlParser {
    private final Scanner scanner;

    public YamlParser(Reader reader) { this.scanner = new Scanner(reader); }

    public Stream parse() {
        Stream stream = new Stream();
        while (!scanner.end())
            stream.document(document());
        return stream;
    }

    private Document document() {
        Document document = new Document();
        if (scanner.is(HASH))
            document.comment(comment());

        while (!scanner.end())

            scanner.read();

        return document;
    }

    private Comment comment() {
        scanner.expect(HASH);
        scanner.skip(WS);
        return new Comment().text(scanner.readUntil(NL));
    }
}
