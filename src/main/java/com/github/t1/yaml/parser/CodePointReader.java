package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
class CodePointReader {
    private final Reader reader;

    Mark mark(int readAheadLimit) { return new Mark(readAheadLimit); }

    List<CodePoint> read(int count) {
        List<CodePoint> out = new ArrayList<>();
        for (int i = 0; i < count; i++)
            out.add(read());
        return out;
    }

    @SneakyThrows(IOException.class) CodePoint read() { return CodePoint.of(reader.read()); }

    class Mark implements AutoCloseable {
        @SneakyThrows(IOException.class) private Mark(int readAheadLimit) { reader.mark(readAheadLimit); }

        @SneakyThrows(IOException.class) @Override public void close() { reader.reset(); }
    }
}
