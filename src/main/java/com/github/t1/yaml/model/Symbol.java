package com.github.t1.yaml.model;

import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Symbol {
    BOM(c -> c == 0xFEFF),
    WS(Character::isWhitespace),
    HASH(c -> c == '#'),
    EQ(c -> c == '='),
    PLUS(c -> c == '+'),
    MINUS(c -> c == '-'),
    MULT(c -> c == '*'),
    NL(c -> c == '\n' || c == '\r'),
    ALPHA(Character::isAlphabetic),
    NUMBER(Character::isDigit);

    private final Predicate<Integer> predicate;

    public boolean matches(int c) { return predicate.test(c); }
}
