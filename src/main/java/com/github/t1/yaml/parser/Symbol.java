package com.github.t1.yaml.parser;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;

/**
 * The character definitions from the spec.
 * <p>
 * Naming conventions (see http://yaml.org/spec/1.2/spec.html#id2770517):
 * <p>
 * E_ = empty
 * A production matching no characters.
 * <p>
 * C_ = special
 * A production starting and ending with a special character.
 * <p>
 * B_ = break
 * A production matching a single line break.
 * <p>
 * NB_ = non-break
 * A production starting and ending with a non-break character.
 * <p>
 * S_ = space
 * A production starting and ending with a white space character.
 * <p>
 * NS_ = non-space
 * A production starting and ending with a non-space character.
 * <p>
 * L_ = line
 * A production matching complete line(s).
 * <p>
 * X_Y_
 * A production starting with an X_ character and ending with a Y_ character, where X_ and Y_ are any of the above prefixes.
 * <p>
 * X+, X_Y+
 * A production as above, with the additional property that the matched content indentation level is greater than the specified n parameter.
 */
@RequiredArgsConstructor
public enum Symbol implements Token {
    SPACE(' '),
    LF('\n'), // B_LINE_FEED
    CR('\r'), // B_CARRIAGE_RETURN
    TAB(' '),
    NEL('\u0085'), // Next Line

    BOM('\uFEFF'), // C_BYTE_ORDER_MARK
    B_CHAR(any(LF, CR)),
    C_PRINTABLE(c -> any(c, TAB, LF, CR) || between(0x20, 0x7E, c) // 8-bit
            || NEL.matches(c) || between(0xA0, 0xD7FF, c) || between(0xE000, 0xFFFD, c) // 16 bit
            || between(0x10000, 0x10FFFF, c) // 32-bit
    ),
    NB_CHAR(C_PRINTABLE.minus(any(B_CHAR, BOM))),

    WHITE(any(SPACE, TAB)),
    WS(Character::isWhitespace),
    HASH('#'),
    PERCENT('%'),
    EQ('='),
    PLUS('+'),
    MINUS('-'),
    MULT('*'),
    PERIOD('.'),
    SINGLE_QUOTE('\''),
    DOUBLE_QUOTE('\"'),
    COLON(':'),
    CURLY_OPEN('{'),
    CURLY_CLOSE('}'),
    NL(c -> c == '\n' || c == '\r'),
    ALPHA(Character::isAlphabetic),
    NUMBER(Character::isDigit),

    INDICATOR(c -> CodePoint.of(c).matchAny("-?:,[]{}#&*!|>'\"%@`”"));

    private Predicate<Integer> minus(Predicate<Integer> not) { return c -> this.predicate.test(c) && !not.test(c); }

    private static boolean between(int min, int max, int c) { return min <= c && c <= max; }

    private static Predicate<Integer> any(Symbol... symbols) { return c -> any(c, symbols); }

    private static boolean any(int c, Symbol... symbols) {
        for (Symbol symbol : symbols) {
            if (symbol.matches(c))
                return true;
        }
        return false;
    }

    private final Predicate<Integer> predicate;

    Symbol(int codePoint) { this(c -> c == codePoint); }

    public boolean matches(CodePoint codePoint) { return predicate.test(codePoint.value); }

    public boolean matches(int codePoint) { return predicate.test(codePoint); }

    @Override public List<Predicate<CodePoint>> predicates() { return singletonList(this::matches); }
}
