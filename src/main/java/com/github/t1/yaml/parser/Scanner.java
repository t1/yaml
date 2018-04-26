package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import com.github.t1.yaml.parser.CodePointReader.Mark;
import lombok.RequiredArgsConstructor;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.t1.yaml.parser.Symbol.BOM;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.WS;
import static java.util.Collections.singletonList;

@RequiredArgsConstructor class Scanner {
    /** As specified in http://www.yaml.org/spec/1.2/spec.html#id2790832 */
    private static final int MAX_LOOK_AHEAD = 1024;

    private final CodePointReader reader;
    private int position = 1;
    private int lineNumber = 1;

    Scanner(Reader reader) { this(new CodePointReader(reader)); }

    Scanner(String text) { this(new StringReader(text)); }


    Scanner expect(String string) { return expect(new StringToken(string)); }

    Scanner expect(Token token) {
        for (Predicate<CodePoint> predicate : token.predicates()) {
            String info = this.toString();
            if (!predicate.test(read()))
                throw new YamlParseException(("expected " + predicate) + " but got " + info);
        }
        return this;
    }

    boolean is(CodePoint codePoint) { return codePoint.equals(peek()); }

    boolean is(String string) {
        return is(new StringToken(string));
    }

    boolean is(Token token) {
        List<Predicate<CodePoint>> predicates = token.predicates();
        List<CodePoint> codePoints = peek(predicates.size());
        assert predicates.size() == codePoints.size();
        for (int i = 0; i < predicates.size(); i++)
            if (!predicates.get(i).test(codePoints.get(i)))
                return false;
        return true;
    }

    boolean accept(String string) { return accept(new StringToken(string)); }

    boolean accept(Token token) {
        if (!is(token))
            return false;
        expect(token);
        return true;
    }

    boolean end() { return peek().isEof(); }

    boolean more() { return !end(); }

    void acceptBom() {
        if (is(BOM))
            reader.read();
    }

    CodePoint peek() { return peek(1).get(0); }

    private List<CodePoint> peek(int count) {
        try (Mark mark = reader.mark(count)) {
            return reader.read(count);
        }
    }

    String peekUntil(Token token) {
        List<Predicate<CodePoint>> predicates = token.predicates();
        try (Mark mark = reader.mark(MAX_LOOK_AHEAD)) {
            StringBuilder out = new StringBuilder();
            int matchLength = 0;
            while (true) {
                CodePoint codePoint = reader.read();
                if (codePoint.isEof())
                    return null;
                if (predicates.get(matchLength).test(codePoint)) {
                    if (++matchLength == predicates.size())
                        return out.toString();
                } else {
                    matchLength = 0;
                    codePoint.appendTo(out);
                }
            }
        }
    }

    Optional<CodePoint> peekAfter(int count) {
        List<CodePoint> peek = peek(count + 1);
        return (peek.size() < count + 1) ? Optional.empty() : Optional.of(peek.get(peek.size() - 1));
    }

    CodePoint read() {
        CodePoint codePoint = reader.read();
        if (BOM.test(codePoint))
            throw new YamlParseException("A BOM must not appear inside a document");
        if (NL.test(codePoint)) {
            lineNumber++;
            position = 1;
        } else {
            position++;
        }
        return codePoint;
    }

    String readWord() { return readUntilAndSkip(WS); }

    String readLine() { return readUntilAndSkip(NL); }

    Integer readInteger() { return Integer.decode(readWhile(() -> singletonList(codePoint -> codePoint.is(Character::isDigit)))); }

    String readUntil(String end) { return readUntil(new StringToken(end)); }

    String readUntil(Token end) {
        StringBuilder builder = new StringBuilder();
        while (more() && !is(end))
            builder.appendCodePoint(read().value);
        return builder.toString();
    }

    String readUntilAndSkip(String end) { return readUntilAndSkip(new StringToken(end)); }

    String readUntilAndSkip(Token end) {
        String result = readUntil(end);
        if (more())
            expect(end);
        return result;
    }

    Scanner skip(String token) { return skip(new StringToken(token)); }

    Scanner skip(Token token) {
        accept(token);
        return this;
    }

    int count(String token) { return count(new StringToken(token)); }

    int count(Token token) { return readWhile(token).length(); }

    String readWhile(Token token) {
        StringBuilder out = new StringBuilder();
        while (is(token))
            out.appendCodePoint(read().value);
        return out.toString();
    }

    @Override public String toString() {
        return peek().xinfo() + " at line " + lineNumber + " char " + position;
    }
}
