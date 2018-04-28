package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.github.t1.yaml.parser.Symbol.P.between;
import static com.github.t1.yaml.parser.Symbol.P.not;
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
public enum Symbol implements Token, Predicate<CodePoint> {
    TAB('\t'),
    LF('\n'), // B_LINE_FEED
    CR('\r'), // B_CARRIAGE_RETURN
    NEL('\u0085'), // Next Line
    SPACE(' '),

    C_PRINTABLE(TAB.or(LF).or(CR).or(between(0x20, 0x7E)) // 8-bit
            .or(NEL).or(between(0xA0, 0xD7FF)).or(between(0xE000, 0xFFFD)) // 16 bit
            .or(between(0x10000, 0x10FFFF)) // 32-bit
    ), // 1
    NB_JSON(TAB.or(between(0x20, 0x10FFFF))), // 2
    BOM('\uFEFF'), // 3 C_BYTE_ORDER_MARK
    C_SEQUENCE_ENTRY('-'), // 4
    C_MAPPING_KEY('?'), // 5
    C_MAPPING_VALUE(':'), // 6
    C_COLLECT_ENTRY(','), // 7
    C_SEQUENCE_START('['), // 8
    C_SEQUENCE_END(']'), // 9

    C_COMMENT('#'), // 12

    NL(LF.or(CR)), // 26 B_CHAR
    NB_CHAR(C_PRINTABLE.minus(NL).minus(BOM)), // 27
    // same as NL/B_CHAR: B_BREAK(CR.or(LF)), // 28

    ////////////////////////////////// compounds
    SCALAR_END(NL.or(C_COMMENT)),
    FLOW_SEQUENCE_ITEM_END(C_COLLECT_ENTRY.or(C_SEQUENCE_END)),

    // WHITE(SPACE.or(TAB)),
    WS(Character::isWhitespace),
    PERCENT('%'),
    PERIOD('.'),
    SINGLE_QUOTE('\''),
    DOUBLE_QUOTE('\"'),
    CURLY_OPEN('{'),
    // CURLY_CLOSE('}'),
    // ALPHA(Character::isAlphabetic),
    // NUMBER(Character::isDigit),

    // INDICATOR(c -> CodePoint.of(c).matchAny("-?:,[]{}#&*!|>'\"%@`”")),
    ;

    private P or(Symbol that) { return or(that.predicate); }

    private P or(P that) { return this.predicate.or(that); }

    private P minus(Symbol that) { return minus(that.predicate); }

    private P minus(P that) { return this.predicate.and(not(that)); }

    private final P predicate;

    Symbol(int codePoint) { this(c -> c == codePoint); }

    Symbol(Predicate<Integer> predicate) { this(new P(predicate)); }

    @Override public boolean test(CodePoint codePoint) { return predicate.test(codePoint.value); }

    @Override public List<Predicate<CodePoint>> predicates() { return singletonList(this); }

    @RequiredArgsConstructor
    static class P {
        static P not(Symbol symbol) { return not(symbol.predicate); }

        static P not(P predicate) { return new P(predicate.predicate.negate()); }

        static P between(int min, int max) { return new P(c -> min <= c && c <= max); }


        private final Predicate<Integer> predicate;

        public boolean test(Integer integer) { return predicate.test(integer); }


        private P or(Symbol that) { return or(that.predicate); }

        private P or(P that) { return new P(this.predicate.or(that.predicate)); }

        private P and(Symbol that) { return and(that.predicate); }

        private P and(P that) { return new P(this.predicate.and(that.predicate)); }

        private P minus(Symbol symbol) { return minus(symbol.predicate); }

        private P minus(P that) { return and(not(that)); }
    }
}
