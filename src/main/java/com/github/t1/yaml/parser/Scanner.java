package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Symbol;
import com.github.t1.yaml.model.Token;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.t1.yaml.model.Symbol.NL;
import static com.github.t1.yaml.model.Symbol.WS;

@RequiredArgsConstructor public class Scanner {
    private static final int MAX_PEEK = 1024;

    private static String toString(int codePoint) { return (codePoint < 0) ? "" : new String(Character.toChars(codePoint)); }

    private static String characterName(int codePoint) { return (codePoint < 0) ? "EOF" : Character.getName(codePoint); }

    static int lastChar(String string) { return string.substring(string.length() - 1, string.length()).codePointAt(0); }

    private final Reader reader;
    private int position = 1;
    private int lineNumber = 1;

    private Supplier<? extends RuntimeException> error(String message) { return () -> new YamlParseException(message + " but got " + this); }

    Scanner expect(String token) {
        token.codePoints().forEach(codePoint -> this.expect(c -> c == codePoint, "codePoint " + characterName(codePoint)));
        return this;
    }

    Scanner expect(Token token) {
        token.symbols().forEach(this::expect);
        return this;
    }

    Scanner expect(Symbol symbol) { return expect(symbol::matches, symbol.name()); }

    Scanner expect(Predicate<Character> predicate, String description) {
        char next = (char) read();
        if (!predicate.test(next))
            throw error("expected " + description).get();
        return this;
    }

    boolean is(Symbol symbol) { return symbol.matches(peek()); }

    boolean is(Token token) { return token.matches(peek(token.length())); }

    boolean accept(Symbol symbol) {
        if (is(symbol)) {
            expect(symbol);
            return true;
        } else
            return false;
    }

    boolean accept(Token token) {
        if (is(token)) {
            expect(token);
            return true;
        } else
            return false;
    }

    boolean end() { return !more(); }

    public boolean more() { return peek() >= 0; }

    @SneakyThrows(IOException.class)
    boolean acceptBom() {
        if (isBOM(peek())) {
            //noinspection ResultOfMethodCallIgnored
            reader.skip(1);
            return true;
        }
        return false;
    }

    int peek() { return peek(1)[0]; }

    @SneakyThrows(IOException.class)
    int[] peek(int count) {
        reader.mark(count);
        int[] read = new int[count];
        for (int i = 0; i < count; i++)
            read[i] = reader.read();
        reader.reset();
        return read;
    }

    @SneakyThrows(IOException.class)
    String peekUntil(Symbol symbol) {
        StringBuilder out = new StringBuilder();
        reader.mark(MAX_PEEK);
        while (true) {
            int codePoint = reader.read();
            if (codePoint < 0 || symbol.matches(codePoint))
                break;
            out.appendCodePoint(codePoint);
        }
        reader.reset();
        return out.toString();
    }

    @SneakyThrows(IOException.class)
    int read() {
        int codePoint = reader.read();
        if (isBOM(codePoint))
            throw new YamlParseException("A BOM must not appear inside a document");
        if (NL.matches(codePoint)) {
            lineNumber++;
            position = 1;
        } else {
            position++;
        }
        return codePoint;
    }

    private boolean isBOM(int codePoint) { return codePoint == 0xFEFF; }

    String readString() { return toString(read()); }

    String readWord() { return readUntilAndSkip(WS); }

    String readLine() { return readUntilAndSkip(NL); }

    String readUntilAndSkip(Symbol symbol) {
        String result = readUntil(symbol);
        if (more())
            expect(symbol);
        return result;
    }

    String readUntil(Symbol symbol) {
        StringBuilder builder = new StringBuilder();
        while (more() && !is(symbol))
            builder.append(readString());
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
