package com.github.t1.yaml.tools;

import com.github.t1.yaml.parser.YamlParseException;
import com.github.t1.yaml.tools.CodePointReader.Mark;
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

@RequiredArgsConstructor public class Scanner {
    private final int lookAheadLimit;
    private final CodePointReader reader;

    private int position = 1;
    private int lineNumber = 1;

    public Scanner(int lookAheadLimit, Reader reader) { this(lookAheadLimit, new CodePointReader(reader)); }

    public Scanner(int lookAheadLimit, String text) { this(lookAheadLimit, new StringReader(text)); }


    public Scanner expect(String string) { return expect(new StringToken(string)); }

    public Scanner expect(Token token) {
        for (Predicate<CodePoint> predicate : token.predicates()) {
            String info = this.toString();
            if (!predicate.test(read()))
                throw new YamlParseException(("expected " + predicate) + " but got " + info);
        }
        return this;
    }

    public boolean is(CodePoint codePoint) { return codePoint.equals(peek()); }

    public boolean is(String string) {
        return is(new StringToken(string));
    }

    public boolean is(Token token) {
        List<Predicate<CodePoint>> predicates = token.predicates();
        List<CodePoint> codePoints = peek(predicates.size());
        assert predicates.size() == codePoints.size();
        for (int i = 0; i < predicates.size(); i++)
            if (!predicates.get(i).test(codePoints.get(i)))
                return false;
        return true;
    }

    public boolean accept(String string) { return accept(new StringToken(string)); }

    public boolean accept(Token token) {
        if (!is(token))
            return false;
        expect(token);
        return true;
    }

    public boolean end() { return peek().isEof(); }

    public boolean more() { return !end(); }

    public void acceptBom() {
        if (is(BOM))
            reader.read();
    }

    public CodePoint peek() { return peek(1).get(0); }

    private List<CodePoint> peek(int count) {
        try (Mark mark = reader.mark(count)) {
            return reader.read(count);
        }
    }

    public String peekUntil(Token token) {
        List<Predicate<CodePoint>> predicates = token.predicates();
        try (Mark mark = reader.mark(lookAheadLimit)) {
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

    public Optional<CodePoint> peekAfter(int count) {
        List<CodePoint> peek = peek(count + 1);
        return (peek.size() < count + 1) ? Optional.empty() : Optional.of(peek.get(peek.size() - 1));
    }

    public CodePoint read() {
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

    public String read(int count) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < count; i++)
            out.appendCodePoint(read().value);
        return out.toString();
    }

    public String readWord() { return readUntilAndSkip(WS); }

    public String readLine() { return readUntilAndSkip(NL); }

    public Integer readInteger() { return Integer.decode(readWhile(() -> singletonList(codePoint -> codePoint.is(Character::isDigit)))); }

    public String readUntil(String end) { return readUntil(new StringToken(end)); }

    public String readUntil(Token end) {
        StringBuilder builder = new StringBuilder();
        while (more() && !is(end))
            builder.appendCodePoint(read().value);
        return builder.toString();
    }

    public String readUntilAndSkip(String end) { return readUntilAndSkip(new StringToken(end)); }

    public String readUntilAndSkip(Token end) {
        String result = readUntil(end);
        if (more())
            expect(end);
        return result;
    }

    public Scanner skip(String token) { return skip(new StringToken(token)); }

    public Scanner skip(Token token) {
        accept(token);
        return this;
    }

    public int count(String token) { return count(new StringToken(token)); }

    public int count(Token token) { return readWhile(token).length(); }

    public String readWhile(Token token) {
        StringBuilder out = new StringBuilder();
        while (is(token))
            out.appendCodePoint(read().value);
        return out.toString();
    }

    @Override public String toString() {
        return peek().xinfo() + " at line " + lineNumber + " char " + position;
    }
}
