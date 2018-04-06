package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.github.t1.yaml.model.Symbol.BOM;

@RequiredArgsConstructor
public class StreamParser {
    private final Scanner next;

    public Stream stream() {
        Stream stream = new Stream();
        next.skip(BOM);
        while (next.more())
            new DocumentParser(next).document().ifPresent(stream::document);
        return stream;
    }
}
