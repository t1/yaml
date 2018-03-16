package com.github.t1.yaml.parser;

import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Token {
    ALPHA(Character::isAlphabetic),
    NUMBER(Character::isDigit),
    WS(Character::isWhitespace),
    HASH(c -> c == '#'),
    EQ(c -> c == '='),
    PLUS(c -> c == '+'),
    MULT(c -> c == '*'),
    NL(c -> c == '\n' || c == '\r');

    private final Predicate<Integer> predicate;

    public boolean matches(int c) { return predicate.test(c); }
}
