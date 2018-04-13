package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import com.github.t1.yaml.parser.CodePointReader.Mark;
import lombok.RequiredArgsConstructor;

import java.io.Reader;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.t1.yaml.parser.Symbol.BOM;
import static com.github.t1.yaml.parser.Symbol.NL;
import static com.github.t1.yaml.parser.Symbol.SPACE;
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

    void acceptBom() {
        if (is(BOM))
            reader.read();
    }

    private CodePoint peek() { return peek(1).get(0); }

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

    private CodePoint read() {
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

    String readUntil(Token end) {
        StringBuilder builder = new StringBuilder();
        while (more() && !is(end))
            builder.append(read());
        return builder.toString();
    }

    String readUntilAndSkip(Token end) {
        String result = readUntil(end);
        if (more())
            expect(end);
        return result;
    }

    void skipSpaces() { countSkip(SPACE); }

    int countSkip(Token token) {
        int count = 0;
        while (accept(token))
            count++;
        return count;
    }

    Scanner skipOneSpace() {
        if (is(SPACE))
            expect(SPACE);
        return this;
    }

    @Override public String toString() {
        CodePoint c = peek();
        return "[" + c + "][" + c.info() + "][0x" + c.hex() + "] at line " + lineNumber + " char " + position;
    }
}
