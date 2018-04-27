package com.github.t1.yaml.dump;

import lombok.Value;

import java.util.function.Predicate;
import java.util.stream.Stream;

public @Value class CodePoint {
    public static final CodePoint EOF = of(-1);

    public static CodePoint lastCodePoint(String string) { return at(count(string) - 1, string); }

    public static CodePoint at(int index, String string) { return of(string.codePointAt(index)); }

    public static int count(String string) { return string.codePointCount(0, string.length()); }

    public static CodePoint decode(String text) { return of(Integer.decode(text)); }

    public static CodePoint of(int codePoint) { return new CodePoint(codePoint); }

    public final int value;

    public static Stream<CodePoint> stream(String string) { return string.codePoints().mapToObj(CodePoint::new); }

    public static CodePoint of(String text) {
        assert count(text) == 1;
        return at(0, text);
    }

    public String toString() { return (value < 0) ? "" : new String(toChars()); }

    private char[] toChars() { return Character.toChars(value); }

    public String xinfo() { return "[" + escaped() + "][" + info() + "][0x" + hex() + "]"; }

    public String info() { return (value < 0) ? "EOF" : Character.getName(value); }

    private String escaped() {
        switch (value) {
            case '\t':
                return "\\t";
            case '\n':
                return "\\n";
            case '\r':
                return "\\r";
            default:
                return toString();
        }
    }

    public String hex() { return Integer.toHexString(value); }

    public boolean isHex() { return Character.isDigit(value) || value >= 'A' && value <= 'F' || value >= 'a' && value <= 'f'; }

    public boolean isEof() { return value < 0; }

    public boolean is(Predicate<Integer> predicate) { return predicate.test(value); }

    public void appendTo(StringBuilder out) { out.appendCodePoint(value); }
}
