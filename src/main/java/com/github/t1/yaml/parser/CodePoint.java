package com.github.t1.yaml.parser;

import lombok.Value;

import java.util.stream.Stream;

@Value
class CodePoint {
    static CodePoint of(int codePoint) { return new CodePoint(codePoint); }

    public int value;

    public static Stream<CodePoint> stream(String string) { return string.codePoints().mapToObj(CodePoint::new); }

    public String toString() { return (value < 0) ? "" : new String(toChars()); }

    private char[] toChars() { return Character.toChars(value); }

    String info() { return (value < 0) ? "EOF" : Character.getName(value); }

    boolean matches(int codePoint) { return this.value == codePoint; }

    boolean isEof() { return value <0; }

    void appendTo(StringBuilder out) { out.appendCodePoint(value); }

    boolean matchAny(String string) { return CodePoint.stream(string).anyMatch(this::equals); }
}
