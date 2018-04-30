package com.github.t1.yaml.parser;

import com.github.t1.yaml.tools.CodePoint;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.github.t1.yaml.parser.Symbol.P.not;
import static java.util.Collections.singletonList;

/**
 * A single-character Token.
 * For reference, the number and name of the projection in the spec is given in a comment
 */
@RequiredArgsConstructor
public enum Symbol implements Token, Predicate<CodePoint> {
    BOM('\uFEFF'),             // 3 c-byte-order-mark
    MINUS('-'),                // 4 c-sequence-entry
    QUESTION_MARK('?'),        // 5 c-mapping-key
    COLON(':'),                // 6 c-mapping-value
    FLOW_SEQUENCE_ENTRY(','),  // 7 c-collect-entry
    FLOW_SEQUENCE_START('['),  // 8 c-sequence-start
    FLOW_SEQUENCE_END(']'),    // 9 c-sequence-end
    FLOW_MAPPING_START('{'),   // 10 c-mapping-start
    // FLOW_MAPPING_END('}'),     // 11 c-mapping-end
    COMMENT('#'),              // 12 c-comment

    DIRECTIVE('%'),            // 20 c-directive

    LF('\n'),                  // 24 b-line-feed
    CR('\r'),                  // 25 b-carriage-return
    NL(LF.or(CR)),             // 26 b-char

    SPACE(' '),                // 31 s-space
    // TAB('\t'),                 // 32 s-tab
    // WHITE(SPACE.or(TAB)),      // 33 s-white
    SINGLE_QUOTE('\''),        // 18 c-single-quote
    DOUBLE_QUOTE('\"'),        // 19 c-double-quote

    /////////////////////////// not in spec
    DOT('.'),
    WS(Character::isWhitespace),
    SCALAR_END(NL.or(COMMENT)),
    FLOW_SEQUENCE_ITEM_END(FLOW_SEQUENCE_ENTRY.or(FLOW_SEQUENCE_END));

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
