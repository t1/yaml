package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.Stream;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

import static com.github.t1.yaml.model.Symbol.BOM;
import static com.github.t1.yaml.model.Symbol.HASH;
import static com.github.t1.yaml.model.Symbol.NL;
import static com.github.t1.yaml.model.Symbol.WS;
import static java.util.Collections.emptyList;

public class YamlParser {
    private final Scanner next;

    public YamlParser(Reader reader) { this.next = new Scanner(reader); }

    public Stream stream() {
        Stream stream = new Stream();
        next.skip(BOM);
        while (next.accept(HASH))
            stream.prefix().comment(comment());
        implicitDocument_Optional().ifPresent(stream::document);
        explicitDocument_Stream().forEach(stream::document);
        return stream;
    }

    private Optional<Document> implicitDocument_Optional() {
        if (next.end())
            return Optional.empty();

        Document document = new Document();
        if (next.more())
            document.node(node());

        return Optional.of(document);
    }

    private List<Document> explicitDocument_Stream() {
        return emptyList();
    }

    private Comment comment() {
        next.skip(WS);
        return new Comment().text(next.readLine());
    }

    private Node node() {
        return new ScalarNode().text(next.readLine());
    }
}
