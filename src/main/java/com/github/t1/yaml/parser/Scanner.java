package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Symbol;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.t1.yaml.model.Symbol.NL;
import static com.github.t1.yaml.model.Symbol.WS;

@RequiredArgsConstructor class Scanner {
    private static String toString(int codePoint) { return (codePoint < 0) ? "" : new String(Character.toChars(codePoint)); }

    private static String characterName(int codePoint) { return (codePoint < 0) ? "EOF" : Character.getName(codePoint); }

    private final Reader reader;
    private int position = 0;
    private int lineNumber = 1;

    private Supplier<? extends RuntimeException> error(String message) { return () -> new IllegalStateException(message + " but got " + this); }

    Scanner expect(Symbol symbol) { return expect(symbol::matches, symbol.name()); }

    Scanner expect(Predicate<Character> predicate, String description) {
        char next = (char) read();
        if (!predicate.test(next))
            throw error("expected " + description).get();
        return this;
    }

    boolean is(Symbol symbol) { return symbol.matches(peek()); }

    boolean accept(Symbol symbol) {
        if (is(symbol)) {
            expect(symbol);
            return true;
        } else
            return false;
    }

    boolean end() { return !more(); }

    boolean more() { return peek() >= 0; }

    @SneakyThrows(IOException.class)
    int read() {
        position++;
        int codePoint = reader.read();
        if (NL.matches(codePoint)) {
            lineNumber++;
            position = 1;
        }
        return codePoint;
    }

    @SneakyThrows(IOException.class)
    int peek() {
        reader.mark(1);
        int read = reader.read();
        reader.reset();
        return read;
    }

    String readString() { return toString(read()); }

    String readWord() { return readUntil(WS); }

    String readLine() { return readUntil(NL); }

    String readUntil(Symbol symbol) {
        StringBuilder builder = new StringBuilder();
        while (more() && !is(symbol))
            builder.append(readString());
        if (more())
            expect(symbol);
        return builder.toString();
    }

    void skip(Symbol symbol) {
        while (is(symbol))
            expect(symbol);
    }

    @Override public String toString() {
        int c = peek();
        return "[" + toString(c) + "][" + characterName(c) + "][0x" + Integer.toHexString(c) + "] at line " + lineNumber + " char " + position;
    }
}
