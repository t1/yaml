package com.github.t1.yaml.parser;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class Parser {

    private final Reader reader;
    private int position = -1;

    protected Supplier<? extends RuntimeException> error(String message) { return () -> new IllegalStateException(message + " but got [" + readString() + "] at " + position); }

    protected char expect(Token token) { return expect(token::matches, token.name()); }

    protected char expect(Predicate<Character> predicate, String description) {
        char next = (char) read();
        if (!predicate.test(next))
            throw error("expected " + description).get();
        return next;
    }

    protected boolean is(Token token) { return token.matches(peek()); }

    protected boolean accept(Token token) {
        if (is(token)) {
            expect(token);
            return true;
        } else
            return false;
    }

    @SneakyThrows(IOException.class)
    protected int read() {
        position++;
        return reader.read();
    }

    @SneakyThrows(IOException.class)
    protected int peek() {
        reader.mark(1);
        int read = reader.read();
        reader.reset();
        return read;
    }

    // TODO allow unicode
    protected String readString() { return Character.toString((char) read()); }
}
