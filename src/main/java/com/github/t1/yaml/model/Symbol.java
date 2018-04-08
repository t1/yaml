package com.github.t1.yaml.model;

import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Symbol {
    SPACE(' '),
    WS(Character::isWhitespace),
    HASH('#'),
    PERCENT('%'),
    EQ('='),
    PLUS('+'),
    MINUS('-'),
    MULT('*'),
    PERIOD('.'),
    COLON(':'),
    CURLY_OPEN('{'),
    CURLY_CLOSE('}'),
    NL(c -> c == '\n' || c == '\r'),
    ALPHA(Character::isAlphabetic),
    NUMBER(Character::isDigit);

    private final Predicate<Integer> predicate;

    Symbol(int codePoint) { this(c -> c == codePoint); }

    public boolean matches(int codePoint) { return predicate.test(codePoint); }
}
