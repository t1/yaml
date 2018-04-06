package com.github.t1.yaml.model;

import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Symbol {
    BOM(0xFEFF),
    SPACE(' '),
    WS(Character::isWhitespace),
    HASH('#'),
    PERCENT('%'),
    EQ('='),
    PLUS('+'),
    MINUS('-'),
    MULT('*'),
    PERIOD('.'),
    CURLY_OPEN('{'),
    CURLY_CLOSE('}'),
    NL(c -> c == '\n' || c == '\r'),
    ALPHA(Character::isAlphabetic),
    NUMBER(Character::isDigit);

    private final Predicate<Integer> predicate;

    private Symbol(int codePoint) { this(c -> c == codePoint); }

    public boolean matches(int codePoint) { return predicate.test(codePoint); }
}
