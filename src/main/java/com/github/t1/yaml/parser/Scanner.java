package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.CodePointReader.Mark;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.WS;

@RequiredArgsConstructor public class Scanner {
    /** As specified in http://www.yaml.org/spec/1.2/spec.html#id2790832 */
    private static final int MAX_LOOK_AHEAD = 1024;

    private final CodePointReader reader;
    private int position = 1;
    private int lineNumber = 1;

    public Scanner(Reader reader) { this(new CodePointReader(reader)); }

    private Supplier<? extends RuntimeException> error(String message) { return () -> new YamlParseException(message + " but got " + this); }

    Scanner expect(String token) {
        CodePoint.stream(token).forEach(codePoint -> this.expect(codePoint::equals, "codePoint " + codePoint.info()));
        return this;
    }

    Scanner expect(Token token) {
        token.symbols.forEach(this::expect);
        return this;
    }

    Scanner expect(Symbol symbol) { return expect(symbol::matches, symbol.name()); }

    Scanner expect(Predicate<CodePoint> predicate, String description) {
        CodePoint next = read();
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

    boolean end() { return peek().isEof(); }

    public boolean more() { return !end(); }

    boolean acceptBom() {
        if (isBOM(peek())) {
            //noinspection ResultOfMethodCallIgnored
            reader.skip(1);
            return true;
        }
        return false;
    }

    CodePoint peek() {
        try (Mark mark = reader.mark(1)) {
            return reader.read();
        }
    }

    List<CodePoint> peek(int count) {
        try (Mark mark = reader.mark(count)) {
            return reader.read(count);
        }
    }

    String peekUntil(Token token) {
        try (Mark mark = reader.mark(MAX_LOOK_AHEAD)) {
            StringBuilder out = new StringBuilder();
            int matchLength = 0;
            while (true) {
                CodePoint codePoint = reader.read();
                if (codePoint.isEof())
                    return null;
                if (token.symbol(matchLength).matches(codePoint)) {
                    if (++matchLength == token.length())
                        return out.toString();
                } else {
                    matchLength = 0;
                    codePoint.appendTo(out);
                }
            }
        }
    }

    CodePoint read() {
        CodePoint codePoint = reader.read();
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

    private boolean isBOM(CodePoint codePoint) { return codePoint.matches(0xFEFF); }

    String readString() { return read().toString(); }

    String readWord() { return readUntilAndSkip(WS); }

    String readLine() { return readUntilAndSkip(NL); }

    String readUntil(Symbol symbol) {
        StringBuilder builder = new StringBuilder();
        while (more() && !is(symbol))
            builder.append(readString());
        return builder.toString();
    }

    String readUntilAndSkip(Symbol symbol) {
        String result = readUntil(symbol);
        if (more())
            expect(symbol);
        return result;
    }

    String readUntil(Token token) {
        StringBuilder builder = new StringBuilder();
        while (more() && !is(token))
            builder.append(readString());
        return builder.toString();
    }

    String readUntilAndSkip(Token token) {
        String result = readUntil(token);
        if (more())
            expect(token);
        return result;
    }

    Scanner skip(Symbol symbol) {
        while (is(symbol))
            expect(symbol);
        return this;
    }

    @Override public String toString() {
        CodePoint c = peek();
        return "[" + c + "][" + c.info() + "][0x" + hex(c) + "] at line " + lineNumber + " char " + position;
    }

    @NotNull private String hex(CodePoint c) {
        return Integer.toHexString(c.value);
    }
}
