package com.github.t1.yaml.model

import com.github.t1.yaml.tools.CodePoint

/**
 * This is not really a part of the `model` package, but it's used by `dump` as well as `parser`,
 * so it's neither of those... and it should not be public.
 */
enum class Escape(val hex: Int, val string: String) {
    NULL(0, ""), // 42 ns-esc-null: #x0
    BELL(0, ""), // 43 ns-esc-bell: #x7
    BACKSPACE(0, ""), // 44 ns-esc-backspace: #x8
    TAB(0, ""), // 45 ns-esc-horizontal-tab: #x9 This is useful at the start or the end of a line to force a leading or trailing tab to become part of the content.
    LF(0, ""), // 46 ns-esc-line-feed: #xA
    VTAB(0, ""), // 47 ns-esc-vertical-tab: #xB
    FF(0, ""), // 48 ns-esc-form-feed: #xC
    CR(0, ""), // 49 ns-esc-carriage-return: #xD
    ESC(0, ""), // 50 ns-esc-escape: #x1B
    SPACE(0, ""), // 51 ns-esc-space: #x20 This is useful at the start or the end of a line to force a leading or trailing space to become part of the content.
    QUOTE(0, ""), // 52 ns-esc-double-quote: #x22
    SLASH(0, ""), // 53 ns-esc-slash: #x2F, for JSON compatibility.
    BACKSLASH(0, ""), // 54 ns-esc-backslash: #x5C
    NEXTLINE(0, ""), // 55 ns-esc-next-line: #x85
    NBSP(0, ""), // 56 ns-esc-non-breaking-space: #xA0
    LINESEP(0, ""), // 57 ns-esc-line-separator: #x2028
    PARSEP(0, ""), // 58 ns-esc-paragraph-separator: #x2029
    U8(0, ""), // 59 ns-esc-8-bit: Escaped 8-bit Unicode
    U16(0, ""), // 60 ns-esc-16-bit: Escaped 16-bit Unicode
    U32(0, "");
    // 61 ns-esc-32-bit: Escaped 32-bit Unicode

    val raw get(): String = CodePoint.of(hex).toString()

    val hexString get(): String = Integer.toHexString(hex)

    companion object {
        fun of(codePoint: CodePoint): Escape? {
            for (escape in values())
                if (codePoint.value == escape.hex)
                    return escape
            return null
        }
    }
}
