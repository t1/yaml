package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;

import java.io.Reader;

import static com.github.t1.yaml.parser.Token.HASH;
import static com.github.t1.yaml.parser.Token.NL;

public class YamlParser extends Parser {
    public YamlParser(Reader reader) { super(reader); }

    public Stream parse() {
        Stream stream = new Stream();
        while (peek() >= 0) {
            stream.document(document());
        }
        return stream;
    }

    private Document document() {
        Document document = new Document();
        if (is(HASH)) {
            document.comment(comment());
        }

        while (peek() >= 0)
            read();
        return document;
    }

    private Comment comment() {
        StringBuilder builder = new StringBuilder().append(expect(HASH));
        while (!is(NL))
            builder.append(readString());
        return new Comment().text(builder.toString());
    }
}
