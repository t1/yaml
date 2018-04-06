package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Stream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamParser {
    private final Scanner next;

    public Stream stream() {
        Stream stream = new Stream();
        while (next.more())
            new DocumentParser(next).document().ifPresent(stream::document);
        return stream;
    }
}
