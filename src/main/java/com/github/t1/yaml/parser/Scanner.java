package com.github.t1.yaml.parser;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Scanner {

    private final Reader reader;
    private int position = -1;

    private Supplier<? extends RuntimeException> error(String message) { return () -> new IllegalStateException(message + " but got [" + readString() + "] at " + position); }

    void expect(Token token) { expect(token::matches, token.name()); }

    char expect(Predicate<Character> predicate, String description) {
        char next = (char) read();
        if (!predicate.test(next))
            throw error("expected " + description).get();
        return next;
    }

    boolean is(Token token) { return token.matches(peek()); }

    boolean accept(Token token) {
        if (is(token)) {
            expect(token);
            return true;
        } else
            return false;
    }

    boolean end() { return peek() < 0; }

    @SneakyThrows(IOException.class)
    int read() {
        position++;
        return reader.read();
    }

    @SneakyThrows(IOException.class)
    private int peek() {
        reader.mark(1);
        int read = reader.read();
        reader.reset();
        return read;
    }

    // TODO allow unicode
    String readString() { return Character.toString((char) read()); }

    void skip(Token token) {
        while (is(token))
            expect(token);
    }
}
