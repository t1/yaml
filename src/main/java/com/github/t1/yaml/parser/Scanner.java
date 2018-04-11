package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.CodePointReader.Mark;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.List;
import java.util.function.Predicate;

import static com.github.t1.yaml.parser.Symbol.BOM;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.WS;

@RequiredArgsConstructor public class Scanner {
    /** As specified in http://www.yaml.org/spec/1.2/spec.html#id2790832 */
    private static final int MAX_LOOK_AHEAD = 1024;

    private final CodePointReader reader;
    private int position = 1;
    private int lineNumber = 1;

    public Scanner(Reader reader) { this(new CodePointReader(reader)); }

    private RuntimeException error(String message) { return new YamlParseException(message + " but got " + this); }

    Scanner expect(Token token) {
        for (Predicate<CodePoint> predicate : token.predicates())
            if (!predicate.test(read()))
                throw error("expected " + predicate);
        return this;
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
        if (is(BOM)) {
            //noinspection ResultOfMethodCallIgnored
            reader.skip(1);
            return true;
        }
        return false;
    }

    CodePoint peek() { return peek(1).get(0); }

    List<CodePoint> peek(int count) {
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

    CodePoint read() {
        CodePoint codePoint = reader.read();
        if (BOM.matches(codePoint))
            throw new YamlParseException("A BOM must not appear inside a document");
        if (NL.matches(codePoint)) {
            lineNumber++;
            position = 1;
        } else {
            position++;
        }
        return codePoint;
    }

    String readString() { return read().toString(); }

    String readWord() { return readUntilAndSkip(WS); }

    String readLine() { return readUntilAndSkip(NL); }

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

    Scanner skip(Token token) {
        while (is(token))
            expect(token);
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
