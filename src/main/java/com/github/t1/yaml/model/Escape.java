package com.github.t1.yaml.model;

import com.github.t1.yaml.tools.CodePoint;
import lombok.RequiredArgsConstructor;

/**
 * This is not really a part of the `model` package, but it's used by `dump` as well as `parser`,
 * so it's neither of those... and it should not be public.
 */
@RequiredArgsConstructor public enum Escape {
    NULL("0", 0x00),      // 42 ns-esc-null: #x0
    BELL("a", 0x07),      // 43 ns-esc-bell: #x7
    BACKSPACE("b", 0x08), // 44 ns-esc-backspace: #x8
    TAB("t", 0x09),       // 45 ns-esc-horizontal-tab: #x9 This is useful at the start or the end of a line to force a leading or trailing tab to become part of the content.
    LF("n", 0x0a),        // 46 ns-esc-line-feed: #xA
    VTAB("v", 0x0b),      // 47 ns-esc-vertical-tab: #xB
    FF("f", 0x0c),        // 48 ns-esc-form-feed: #xC
    CR("r", 0x0d),        // 49 ns-esc-carriage-return: #xD
    ESC("e", 0x1b),       // 50 ns-esc-escape: #x1B
    SPACE(" ", 0x20),     // 51 ns-esc-space: #x20 This is useful at the start or the end of a line to force a leading or trailing space to become part of the content.
    QUOTE("\"", 0x22),    // 52 ns-esc-double-quote: #x22
    SLASH("/", 0x2f),     // 53 ns-esc-slash: #x2F, for JSON compatibility.
    BACKSLASH("\\", 0x5C),// 54 ns-esc-backslash: #x5C
    NEXTLINE("N", 0x85),  // 55 ns-esc-next-line: #x85
    NBSP("_", 0xa0),      // 56 ns-esc-non-breaking-space: #xA0
    LINESEP("L", 0x2028), // 57 ns-esc-line-separator: #x2028
    PARSEP("P", 0x2029),  // 58 ns-esc-paragraph-separator: #x2029
    U8("x", -1),          // 59 ns-esc-8-bit: Escaped 8-bit Unicode
    U16("u", -1),         // 60 ns-esc-16-bit: Escaped 16-bit Unicode
    U32("U", -1);         // 61 ns-esc-32-bit: Escaped 32-bit Unicode

    public final String string;
    public final int hex;

    public static Escape of(CodePoint codePoint) {
        for (Escape escape : values())
            if (codePoint.value == escape.hex)
                return escape;
        return null;
    }

    public String raw() { return CodePoint.of(hex).toString(); }

    public String hexString() { return Integer.toHexString(hex); }
}
