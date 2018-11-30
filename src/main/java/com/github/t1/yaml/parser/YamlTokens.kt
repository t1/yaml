@file:Generated("spec.generator.YamlTokenGenerator")
@file:Suppress("unused", "ObjectPropertyName", "FunctionName", "NonAsciiCharacters", "REDUNDANT_ELSE_IN_WHEN")

package com.github.t1.yaml.parser

/**
 * The productions as specified in the YAML spec
 *
 * e-        A production matching no characters.
 * c-        A production starting and ending with a special character.
 * b-        A production matching a single line break.
 * nb-       A production starting and ending with a non-break character.
 * s-        A production starting and ending with a white space character.
 * ns-       A production starting and ending with a non-space character.
 * l-        A production matching complete line(s).
 * X-Y-      A production starting with an X- character and ending with a Y- character, where X- and Y- are any of the above prefixes.
 * X+, X-Y+  A production as above, with the additional property that the matched content indentation level is greater than the specified n parameter.
 */

import com.github.t1.yaml.parser.ChompMode.clip
import com.github.t1.yaml.parser.ChompMode.keep
import com.github.t1.yaml.parser.ChompMode.strip
import com.github.t1.yaml.parser.InOutMode.`block-in`
import com.github.t1.yaml.parser.InOutMode.`block-key`
import com.github.t1.yaml.parser.InOutMode.`block-out`
import com.github.t1.yaml.parser.InOutMode.`flow-in`
import com.github.t1.yaml.parser.InOutMode.`flow-key`
import com.github.t1.yaml.parser.InOutMode.`flow-out`
import com.github.t1.yaml.parser.ScalarParser.Companion.autoDetectIndentation
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Match
import com.github.t1.yaml.tools.Symbol
import com.github.t1.yaml.tools.Token
import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once
import com.github.t1.yaml.tools.empty
import com.github.t1.yaml.tools.endOfFile
import com.github.t1.yaml.tools.startOfLine
import com.github.t1.yaml.tools.symbol
import com.github.t1.yaml.tools.toCodePointRange
import com.github.t1.yaml.tools.token
import com.github.t1.yaml.tools.tokenGenerator
import com.github.t1.yaml.tools.undefined
import javax.annotation.Generated

private infix fun Char.or(that: Char) = symbol(this) or symbol(that)
private infix fun Char.or(that: Token) = symbol(this) or that
private infix fun CharRange.or(that: CharRange): Token = symbol(CodePoint.of(this.first)..CodePoint.of(this.last)) or symbol(CodePoint.of(that.first)..CodePoint.of(that.last))
private infix fun Token.or(that: String): Token = or(symbol(that))
private infix fun Token.or(that: Char): Token = or(symbol(that))
private infix operator fun Char.rangeTo(that: Char) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun Char.rangeTo(that: String) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun String.rangeTo(that: String) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun Char.plus(that: Char) = symbol(this) + symbol(that)
private infix operator fun Char.plus(token: Token) = symbol(this) + token
private infix operator fun Token.plus(that: Char) = this + symbol(that)
private infix fun Token.or(range: CharRange) = this.or(symbol(range.toCodePointRange()))
private fun CodePointReader.accept(char: Char): Boolean = accept(symbol(char))
private fun CodePointReader.accept(symbol: Symbol): Boolean = symbol.match(this).matches
private val anNsCharPreceding = undefined
private val atMost1024CharactersAltogether = undefined
private val excludingCForbiddenContent = undefined
private val followedByAnNsPlainSafe = undefined


/**
 * `1` : c-printable:
 * [<[\t][CHARACTER TABULATION][0x9]> |
 *    <[\n][LINE FEED (LF)][0xa]> |
 *    <[\r][CARRIAGE RETURN (CR)][0xd]> |
 *    [<[ ][SPACE][0x20]>-<[~][TILDE][0x7e]>] |
 *    <[\u0085][NEXT LINE (NEL)][0x85]> |
 *    [<[ ][NO-BREAK SPACE][0xa0]>-<[퟿][?][0xd7ff]>] |
 *    [<[][PRIVATE USE AREA E000][0xe000]>-<[�][REPLACEMENT CHARACTER][0xfffd]>] |
 *    [<[\uD800\uDC00][LINEAR B SYLLABLE B008 A][0x10000]>-<[\uDBFF\uDFFF][?][0x10ffff]>]]
 */
val `c-printable` = token("c-printable", '\t' or '\n' or '\r' or ' '..'~' or '\u0085' or ' '..'퟿' or ''..'�' or "\uD800\uDC00".."\uDBFF\uDFFF")

/**
 * `2` : nb-json:
 * [<[\t][CHARACTER TABULATION][0x9]> |
 *    [<[ ][SPACE][0x20]>-<[\uDBFF\uDFFF][?][0x10ffff]>]]
 */
val `nb-json` = token("nb-json", '\t' or ' '.."\uDBFF\uDFFF")

/**
 * `3` : c-byte-order-mark:
 * <[\uFEFF][ZERO WIDTH NO-BREAK SPACE][0xfeff]>
 */
val `c-byte-order-mark` = token("c-byte-order-mark", '\uFEFF')

/**
 * `4` : c-sequence-entry:
 * <[-][HYPHEN-MINUS][0x2d]>
 */
val `c-sequence-entry` = token("c-sequence-entry", '-')

/**
 * `5` : c-mapping-key:
 * <[?][QUESTION MARK][0x3f]>
 */
val `c-mapping-key` = token("c-mapping-key", '?')

/**
 * `6` : c-mapping-value:
 * <[:][COLON][0x3a]>
 */
val `c-mapping-value` = token("c-mapping-value", ':')

/**
 * `7` : c-collect-entry:
 * <[,][COMMA][0x2c]>
 */
val `c-collect-entry` = token("c-collect-entry", ',')

/**
 * `8` : c-sequence-start:
 * <[[][LEFT SQUARE BRACKET][0x5b]>
 */
val `c-sequence-start` = token("c-sequence-start", '[')

/**
 * `9` : c-sequence-end:
 * <[]][RIGHT SQUARE BRACKET][0x5d]>
 */
val `c-sequence-end` = token("c-sequence-end", ']')

/**
 * `10` : c-mapping-start:
 * <[{][LEFT CURLY BRACKET][0x7b]>
 */
val `c-mapping-start` = token("c-mapping-start", '{')

/**
 * `11` : c-mapping-end:
 * <[}][RIGHT CURLY BRACKET][0x7d]>
 */
val `c-mapping-end` = token("c-mapping-end", '}')

/**
 * `12` : c-comment:
 * <[#][NUMBER SIGN][0x23]>
 */
val `c-comment` = token("c-comment", '#')

/**
 * `13` : c-anchor:
 * <[&][AMPERSAND][0x26]>
 */
val `c-anchor` = token("c-anchor", '&')

/**
 * `14` : c-alias:
 * <[*][ASTERISK][0x2a]>
 */
val `c-alias` = token("c-alias", '*')

/**
 * `15` : c-tag:
 * <[!][EXCLAMATION MARK][0x21]>
 */
val `c-tag` = token("c-tag", '!')

/**
 * `16` : c-literal:
 * <[|][VERTICAL LINE][0x7c]>
 */
val `c-literal` = token("c-literal", '|')

/**
 * `17` : c-folded:
 * <[>][GREATER-THAN SIGN][0x3e]>
 */
val `c-folded` = token("c-folded", '>')

/**
 * `18` : c-single-quote:
 * <[\'][APOSTROPHE][0x27]>
 */
val `c-single-quote` = token("c-single-quote", '\'')

/**
 * `19` : c-double-quote:
 * <["][QUOTATION MARK][0x22]>
 */
val `c-double-quote` = token("c-double-quote", '"')

/**
 * `20` : c-directive:
 * <[%][PERCENT SIGN][0x25]>
 */
val `c-directive` = token("c-directive", '%')

/**
 * `21` : c-reserved:
 * [<[@][COMMERCIAL AT][0x40]> |
 *    <[`][GRAVE ACCENT][0x60]>]
 */
val `c-reserved` = token("c-reserved", '@' or '`')

/**
 * `22` : c-indicator:
 * [->c-sequence-entry |
 *    ->c-mapping-key |
 *    ->c-mapping-value |
 *    ->c-collect-entry |
 *    ->c-sequence-start |
 *    ->c-sequence-end |
 *    ->c-mapping-start |
 *    ->c-mapping-end |
 *    ->c-comment |
 *    ->c-anchor |
 *    ->c-alias |
 *    ->c-tag |
 *    ->c-literal |
 *    ->c-folded |
 *    ->c-single-quote |
 *    ->c-double-quote |
 *    ->c-directive |
 *    ->c-reserved]
 */
val `c-indicator` = token("c-indicator", `c-sequence-entry` or `c-mapping-key` or `c-mapping-value` or `c-collect-entry` or `c-sequence-start` or `c-sequence-end` or `c-mapping-start` or `c-mapping-end` or `c-comment` or `c-anchor` or `c-alias` or `c-tag` or `c-literal` or `c-folded` or `c-single-quote` or `c-double-quote` or `c-directive` or `c-reserved`)

/**
 * `23` : c-flow-indicator:
 * [->c-collect-entry |
 *    ->c-sequence-start |
 *    ->c-sequence-end |
 *    ->c-mapping-start |
 *    ->c-mapping-end]
 */
val `c-flow-indicator` = token("c-flow-indicator", `c-collect-entry` or `c-sequence-start` or `c-sequence-end` or `c-mapping-start` or `c-mapping-end`)

/**
 * `24` : b-line-feed:
 * <[\n][LINE FEED (LF)][0xa]>
 */
val `b-line-feed` = token("b-line-feed", '\n')

/**
 * `25` : b-carriage-return:
 * <[\r][CARRIAGE RETURN (CR)][0xd]>
 */
val `b-carriage-return` = token("b-carriage-return", '\r')

/**
 * `26` : b-char:
 * [->b-line-feed |
 *    ->b-carriage-return]
 */
val `b-char` = token("b-char", `b-line-feed` or `b-carriage-return`)

/**
 * `27` : nb-char:
 * (->c-printable - ->b-char - ->c-byte-order-mark)
 */
val `nb-char` = token("nb-char", `c-printable` - `b-char` - `c-byte-order-mark`)

/**
 * `28` : b-break:
 * [->b-carriage-return + ->b-line-feed |
 *    ->b-carriage-return |
 *    ->b-line-feed]
 */
val `b-break` = token("b-break", (`b-carriage-return` + `b-line-feed`) or `b-carriage-return` or `b-line-feed`)

/**
 * `29` : b-as-line-feed:
 * ->b-break
 */
val `b-as-line-feed` = token("b-as-line-feed", `b-break`)

/**
 * `30` : b-non-content:
 * ->b-break
 */
val `b-non-content` = token("b-non-content", `b-break`)

/**
 * `31` : s-space:
 * <[ ][SPACE][0x20]>
 */
val `s-space` = token("s-space", ' ')

/**
 * `32` : s-tab:
 * <[\t][CHARACTER TABULATION][0x9]>
 */
val `s-tab` = token("s-tab", '\t')

/**
 * `33` : s-white:
 * [->s-space |
 *    ->s-tab]
 */
val `s-white` = token("s-white", `s-space` or `s-tab`)

/**
 * `34` : ns-char:
 * (->nb-char - ->s-white)
 */
val `ns-char` = token("ns-char", `nb-char` - `s-white`)

/**
 * `35` : ns-dec-digit:
 * [<[0][DIGIT ZERO][0x30]>-<[9][DIGIT NINE][0x39]>]
 */
val `ns-dec-digit` = token("ns-dec-digit", '0'..'9')

/**
 * `36` : ns-hex-digit:
 * [->ns-dec-digit |
 *    [<[A][LATIN CAPITAL LETTER A][0x41]>-<[F][LATIN CAPITAL LETTER F][0x46]>] |
 *    [<[a][LATIN SMALL LETTER A][0x61]>-<[f][LATIN SMALL LETTER F][0x66]>]]
 */
val `ns-hex-digit` = token("ns-hex-digit", `ns-dec-digit` or 'A'..'F' or 'a'..'f')

/**
 * `37` : ns-ascii-letter:
 * [[<[A][LATIN CAPITAL LETTER A][0x41]>-<[Z][LATIN CAPITAL LETTER Z][0x5a]>] |
 *    [<[a][LATIN SMALL LETTER A][0x61]>-<[z][LATIN SMALL LETTER Z][0x7a]>]]
 */
val `ns-ascii-letter` = token("ns-ascii-letter", 'A'..'Z' or 'a'..'z')

/**
 * `38` : ns-word-char:
 * [->ns-dec-digit |
 *    ->ns-ascii-letter |
 *    <[-][HYPHEN-MINUS][0x2d]>]
 */
val `ns-word-char` = token("ns-word-char", `ns-dec-digit` or `ns-ascii-letter` or '-')

/**
 * `39` : ns-uri-char:
 * <[%][PERCENT SIGN][0x25]> + ->ns-hex-digit + [->ns-hex-digit |
 *    ->ns-word-char |
 *    <[#][NUMBER SIGN][0x23]> |
 *    <[;][SEMICOLON][0x3b]> |
 *    <[/][SOLIDUS][0x2f]> |
 *    <[?][QUESTION MARK][0x3f]> |
 *    <[:][COLON][0x3a]> |
 *    <[@][COMMERCIAL AT][0x40]> |
 *    <[&][AMPERSAND][0x26]> |
 *    <[=][EQUALS SIGN][0x3d]> |
 *    <[+][PLUS SIGN][0x2b]> |
 *    <[$][DOLLAR SIGN][0x24]> |
 *    <[,][COMMA][0x2c]> |
 *    <[_][LOW LINE][0x5f]> |
 *    <[.][FULL STOP][0x2e]> |
 *    <[!][EXCLAMATION MARK][0x21]> |
 *    <[~][TILDE][0x7e]> |
 *    <[*][ASTERISK][0x2a]> |
 *    <[\'][APOSTROPHE][0x27]> |
 *    <[(][LEFT PARENTHESIS][0x28]> |
 *    <[)][RIGHT PARENTHESIS][0x29]> |
 *    <[[][LEFT SQUARE BRACKET][0x5b]> |
 *    <[]][RIGHT SQUARE BRACKET][0x5d]>]
 */
val `ns-uri-char` = token("ns-uri-char", '%' + `ns-hex-digit` + `ns-hex-digit` or `ns-word-char` or '#' or ';' or '/' or '?' or ':' or '@' or '&' or '=' or '+' or '$' or ',' or '_' or '.' or '!' or '~' or '*' or '\'' or '(' or ')' or '[' or ']')

/**
 * `40` : ns-tag-char:
 * (->ns-uri-char - ->c-tag - ->c-flow-indicator)
 */
val `ns-tag-char` = token("ns-tag-char", `ns-uri-char` - `c-tag` - `c-flow-indicator`)

/**
 * `41` : c-escape:
 * <[\\][REVERSE SOLIDUS][0x5c]>
 */
val `c-escape` = token("c-escape", '\\')

/**
 * `42` : ns-esc-null:
 * <[0][DIGIT ZERO][0x30]>
 */
val `ns-esc-null` = token("ns-esc-null", '0')

/**
 * `43` : ns-esc-bell:
 * <[a][LATIN SMALL LETTER A][0x61]>
 */
val `ns-esc-bell` = token("ns-esc-bell", 'a')

/**
 * `44` : ns-esc-backspace:
 * <[b][LATIN SMALL LETTER B][0x62]>
 */
val `ns-esc-backspace` = token("ns-esc-backspace", 'b')

/**
 * `45` : ns-esc-horizontal-tab:
 * [<[t][LATIN SMALL LETTER T][0x74]> |
 *    <[\t][CHARACTER TABULATION][0x9]>]
 */
val `ns-esc-horizontal-tab` = token("ns-esc-horizontal-tab", 't' or '\t')

/**
 * `46` : ns-esc-line-feed:
 * <[n][LATIN SMALL LETTER N][0x6e]>
 */
val `ns-esc-line-feed` = token("ns-esc-line-feed", 'n')

/**
 * `47` : ns-esc-vertical-tab:
 * <[v][LATIN SMALL LETTER V][0x76]>
 */
val `ns-esc-vertical-tab` = token("ns-esc-vertical-tab", 'v')

/**
 * `48` : ns-esc-form-feed:
 * <[f][LATIN SMALL LETTER F][0x66]>
 */
val `ns-esc-form-feed` = token("ns-esc-form-feed", 'f')

/**
 * `49` : ns-esc-carriage-return:
 * <[r][LATIN SMALL LETTER R][0x72]>
 */
val `ns-esc-carriage-return` = token("ns-esc-carriage-return", 'r')

/**
 * `50` : ns-esc-escape:
 * <[e][LATIN SMALL LETTER E][0x65]>
 */
val `ns-esc-escape` = token("ns-esc-escape", 'e')

/**
 * `51` : ns-esc-space:
 * <[ ][SPACE][0x20]>
 */
val `ns-esc-space` = token("ns-esc-space", ' ')

/**
 * `52` : ns-esc-double-quote:
 * ->c-double-quote
 */
val `ns-esc-double-quote` = token("ns-esc-double-quote", `c-double-quote`)

/**
 * `53` : ns-esc-slash:
 * <[/][SOLIDUS][0x2f]>
 */
val `ns-esc-slash` = token("ns-esc-slash", '/')

/**
 * `54` : ns-esc-backslash:
 * ->c-escape
 */
val `ns-esc-backslash` = token("ns-esc-backslash", `c-escape`)

/**
 * `55` : ns-esc-next-line:
 * <[N][LATIN CAPITAL LETTER N][0x4e]>
 */
val `ns-esc-next-line` = token("ns-esc-next-line", 'N')

/**
 * `56` : ns-esc-non-breaking-space:
 * <[_][LOW LINE][0x5f]>
 */
val `ns-esc-non-breaking-space` = token("ns-esc-non-breaking-space", '_')

/**
 * `57` : ns-esc-line-separator:
 * <[L][LATIN CAPITAL LETTER L][0x4c]>
 */
val `ns-esc-line-separator` = token("ns-esc-line-separator", 'L')

/**
 * `58` : ns-esc-paragraph-separator:
 * <[P][LATIN CAPITAL LETTER P][0x50]>
 */
val `ns-esc-paragraph-separator` = token("ns-esc-paragraph-separator", 'P')

/**
 * `59` : ns-esc-8-bit:
 * <[x][LATIN SMALL LETTER X][0x78]> + (->ns-hex-digit × 2)
 */
val `ns-esc-8-bit` = token("ns-esc-8-bit", 'x' + `ns-hex-digit` * 2)

/**
 * `60` : ns-esc-16-bit:
 * <[u][LATIN SMALL LETTER U][0x75]> + (->ns-hex-digit × 4)
 */
val `ns-esc-16-bit` = token("ns-esc-16-bit", 'u' + `ns-hex-digit` * 4)

/**
 * `61` : ns-esc-32-bit:
 * <[U][LATIN CAPITAL LETTER U][0x55]> + (->ns-hex-digit × 8)
 */
val `ns-esc-32-bit` = token("ns-esc-32-bit", 'U' + `ns-hex-digit` * 8)

/**
 * `62` : c-ns-esc-char:
 * ->c-escape + [->ns-esc-null |
 *    ->ns-esc-bell |
 *    ->ns-esc-backspace |
 *    ->ns-esc-horizontal-tab |
 *    ->ns-esc-line-feed |
 *    ->ns-esc-vertical-tab |
 *    ->ns-esc-form-feed |
 *    ->ns-esc-carriage-return |
 *    ->ns-esc-escape |
 *    ->ns-esc-space |
 *    ->ns-esc-double-quote |
 *    ->ns-esc-slash |
 *    ->ns-esc-backslash |
 *    ->ns-esc-next-line |
 *    ->ns-esc-non-breaking-space |
 *    ->ns-esc-line-separator |
 *    ->ns-esc-paragraph-separator |
 *    ->ns-esc-8-bit |
 *    ->ns-esc-16-bit |
 *    ->ns-esc-32-bit]
 */
val `c-ns-esc-char` = token("c-ns-esc-char", `c-escape` + `ns-esc-null` or `ns-esc-bell` or `ns-esc-backspace` or `ns-esc-horizontal-tab` or `ns-esc-line-feed` or `ns-esc-vertical-tab` or `ns-esc-form-feed` or `ns-esc-carriage-return` or `ns-esc-escape` or `ns-esc-space` or `ns-esc-double-quote` or `ns-esc-slash` or `ns-esc-backslash` or `ns-esc-next-line` or `ns-esc-non-breaking-space` or `ns-esc-line-separator` or `ns-esc-paragraph-separator` or `ns-esc-8-bit` or `ns-esc-16-bit` or `ns-esc-32-bit`)

/**
 * `63` : s-indent(n):
 * (->s-space × n)
 */
fun `s-indent`(n: Int) = `s-space` * n

/**
 * `64` : s-indent<(n):
 * (->s-space × m /* Where m < n */)
 */
fun `s-indent≪`(n: Int) = token("s-indent<(n)") { reader ->
    val match = reader.mark { reader.readWhile { reader -> `s-space`.match(reader).codePoints } }
    if (match.size >= n) return@token Match(matches = false)
    reader.expect(match)
    return@token Match(matches = true, codePoints = match)
}

/**
 * `65` : s-indent≤(n):
 * (->s-space × m /* Where m ≤ n */)
 */
fun `s-indent≤`(n: Int) = token("s-indent≤(n)") { reader ->
    val match = reader.mark { reader.readWhile { reader -> `s-space`.match(reader).codePoints } }
    if (match.size > n) return@token Match(matches = false)
    reader.expect(match)
    return@token Match(matches = true, codePoints = match)
}

/**
 * `66` : s-separate-in-line:
 * [(->s-white × +) |
 *    ->Start of line]
 */
val `s-separate-in-line` = token("s-separate-in-line", `s-white` * once_or_more or startOfLine)

/**
 * `67` : s-line-prefix(n,c):
 * <c> = ->block-out ⇒ ->s-block-line-prefix(n)
 * <c> = ->block-in ⇒ ->s-block-line-prefix(n)
 * <c> = ->flow-out ⇒ ->s-flow-line-prefix(n)
 * <c> = ->flow-in ⇒ ->s-flow-line-prefix(n)
 */
fun `s-line-prefix`(n: Int, c: InOutMode) = tokenGenerator("s-line-prefix") { 
    when (c) {
        `block-out` -> `s-block-line-prefix`(n) named "s-line-prefix($c)"
        `block-in` -> `s-block-line-prefix`(n) named "s-line-prefix($c)"
        `flow-out` -> `s-flow-line-prefix`(n) named "s-line-prefix($c)"
        `flow-in` -> `s-flow-line-prefix`(n) named "s-line-prefix($c)"
        else -> error("unexpected `c` value `$c`")
    }
 }

/**
 * `68` : s-block-line-prefix(n):
 * ->s-indent(n)
 */
fun `s-block-line-prefix`(n: Int) = tokenGenerator("s-block-line-prefix") { `s-indent`(n) }

/**
 * `69` : s-flow-line-prefix(n):
 * ->s-indent(n) + (->s-separate-in-line × ?)
 */
fun `s-flow-line-prefix`(n: Int) = tokenGenerator("s-flow-line-prefix") { `s-indent`(n) + `s-separate-in-line` * zero_or_once }

/**
 * `70` : l-empty(n,c):
 * [->s-line-prefix(n,c) |
 *    ->s-indent<(n)] + ->b-as-line-feed
 */
fun `l-empty`(n: Int, c: InOutMode) = tokenGenerator("l-empty") { `s-line-prefix`(n, c) or `s-indent≪`(n) + `b-as-line-feed` }

/**
 * `71` : b-l-trimmed(n,c):
 * ->b-non-content + (->l-empty(n,c) × +)
 */
fun `b-l-trimmed`(n: Int, c: InOutMode) = tokenGenerator("b-l-trimmed") { `b-non-content` + `l-empty`(n, c) * once_or_more }

/**
 * `72` : b-as-space:
 * ->b-break
 */
val `b-as-space` = token("b-as-space", `b-break`)

/**
 * `73` : b-l-folded(n,c):
 * [->b-l-trimmed(n,c) |
 *    ->b-as-space]
 */
fun `b-l-folded`(n: Int, c: InOutMode) = tokenGenerator("b-l-folded") { `b-l-trimmed`(n, c) or `b-as-space` }

/**
 * `74` : s-flow-folded(n):
 * (->s-separate-in-line × ?) + ->b-l-folded(n,c = <flow-in>) + ->s-flow-line-prefix(n)
 */
fun `s-flow-folded`(n: Int) = tokenGenerator("s-flow-folded") { `s-separate-in-line` * zero_or_once + `b-l-folded`(n, `flow-in`) + `s-flow-line-prefix`(n) }

/**
 * `75` : c-nb-comment-text:
 * ->c-comment + (->nb-char × *)
 */
val `c-nb-comment-text` = token("c-nb-comment-text", `c-comment` + `nb-char` * zero_or_more)

/**
 * `76` : b-comment:
 * [->b-non-content |
 *    ->End of file]
 */
val `b-comment` = token("b-comment", `b-non-content` or endOfFile)

/**
 * `77` : s-b-comment:
 * (->s-separate-in-line + (->c-nb-comment-text × ?) × ?) + ->b-comment
 */
val `s-b-comment` = token("s-b-comment", (`s-separate-in-line` + `c-nb-comment-text` * zero_or_once) * zero_or_once + `b-comment`)

/**
 * `78` : l-comment:
 * ->s-separate-in-line + (->c-nb-comment-text × ?) + ->b-comment
 */
val `l-comment` = token("l-comment", `s-separate-in-line` + `c-nb-comment-text` * zero_or_once + `b-comment`)

/**
 * `79` : s-l-comments:
 * [->s-b-comment |
 *    ->Start of line] + (->l-comment × *)
 */
val `s-l-comments` = token("s-l-comments", `s-b-comment` or startOfLine + `l-comment` * zero_or_more)

/**
 * `80` : s-separate(n,c):
 * <c> = ->block-out ⇒ ->s-separate-lines(n)
 * <c> = ->block-in ⇒ ->s-separate-lines(n)
 * <c> = ->flow-out ⇒ ->s-separate-lines(n)
 * <c> = ->flow-in ⇒ ->s-separate-lines(n)
 * <c> = ->block-key ⇒ ->s-separate-in-line
 * <c> = ->flow-key ⇒ ->s-separate-in-line
 */
fun `s-separate`(n: Int, c: InOutMode) = tokenGenerator("s-separate") { 
    when (c) {
        `block-out` -> `s-separate-lines`(n) named "s-separate($c)"
        `block-in` -> `s-separate-lines`(n) named "s-separate($c)"
        `flow-out` -> `s-separate-lines`(n) named "s-separate($c)"
        `flow-in` -> `s-separate-lines`(n) named "s-separate($c)"
        `block-key` -> `s-separate-in-line` named "s-separate($c)"
        `flow-key` -> `s-separate-in-line` named "s-separate($c)"
        else -> error("unexpected `c` value `$c`")
    }
 }

/**
 * `81` : s-separate-lines(n):
 * [->s-l-comments + ->s-flow-line-prefix(n) |
 *    ->s-separate-in-line]
 */
fun `s-separate-lines`(n: Int) = tokenGenerator("s-separate-lines") { (`s-l-comments` + `s-flow-line-prefix`(n)) or `s-separate-in-line` }

// 82: l-directive -> [83, 86, 88]

// 83: ns-reserved-directive -> [84, 85]

/**
 * `84` : ns-directive-name:
 * (->ns-char × +)
 */
val `ns-directive-name` = token("ns-directive-name", `ns-char` * once_or_more)

/**
 * `85` : ns-directive-parameter:
 * (->ns-char × +)
 */
val `ns-directive-parameter` = token("ns-directive-parameter", `ns-char` * once_or_more)

/**
 * `83` : ns-reserved-directive:
 * ->ns-directive-name + (->s-separate-in-line + ->ns-directive-parameter × *)
 */
val `ns-reserved-directive` = token("ns-reserved-directive", `ns-directive-name` + (`s-separate-in-line` + `ns-directive-parameter`) * zero_or_more)

// 86: ns-yaml-directive -> [87]

/**
 * `87` : ns-yaml-version:
 * (->ns-dec-digit × +) + <[.][FULL STOP][0x2e]> + (->ns-dec-digit × +)
 */
val `ns-yaml-version` = token("ns-yaml-version", `ns-dec-digit` * once_or_more + '.' + `ns-dec-digit` * once_or_more)

/**
 * `86` : ns-yaml-directive:
 * <[Y][LATIN CAPITAL LETTER Y][0x59]> + <[A][LATIN CAPITAL LETTER A][0x41]> + <[M][LATIN CAPITAL LETTER M][0x4d]> + <[L][LATIN CAPITAL LETTER L][0x4c]> + ->s-separate-in-line + ->ns-yaml-version
 */
val `ns-yaml-directive` = token("ns-yaml-directive", 'Y' + 'A' + 'M' + 'L' + `s-separate-in-line` + `ns-yaml-version`)

// 88: ns-tag-directive -> [89, 93]

// 89: c-tag-handle -> [90, 91, 92]

/**
 * `90` : c-primary-tag-handle:
 * ->c-tag
 */
val `c-primary-tag-handle` = token("c-primary-tag-handle", `c-tag`)

/**
 * `91` : c-secondary-tag-handle:
 * ->c-tag + ->c-tag
 */
val `c-secondary-tag-handle` = token("c-secondary-tag-handle", `c-tag` + `c-tag`)

/**
 * `92` : c-named-tag-handle:
 * ->c-tag + (->ns-word-char × +) + ->c-tag
 */
val `c-named-tag-handle` = token("c-named-tag-handle", `c-tag` + `ns-word-char` * once_or_more + `c-tag`)

/**
 * `89` : c-tag-handle:
 * [->c-named-tag-handle |
 *    ->c-secondary-tag-handle |
 *    ->c-primary-tag-handle]
 */
val `c-tag-handle` = token("c-tag-handle", `c-named-tag-handle` or `c-secondary-tag-handle` or `c-primary-tag-handle`)

// 93: ns-tag-prefix -> [94, 95]

/**
 * `94` : c-ns-local-tag-prefix:
 * ->c-tag + (->ns-uri-char × *)
 */
val `c-ns-local-tag-prefix` = token("c-ns-local-tag-prefix", `c-tag` + `ns-uri-char` * zero_or_more)

/**
 * `95` : ns-global-tag-prefix:
 * ->ns-tag-char + (->ns-uri-char × *)
 */
val `ns-global-tag-prefix` = token("ns-global-tag-prefix", `ns-tag-char` + `ns-uri-char` * zero_or_more)

/**
 * `93` : ns-tag-prefix:
 * [->c-ns-local-tag-prefix |
 *    ->ns-global-tag-prefix]
 */
val `ns-tag-prefix` = token("ns-tag-prefix", `c-ns-local-tag-prefix` or `ns-global-tag-prefix`)

/**
 * `88` : ns-tag-directive:
 * <[T][LATIN CAPITAL LETTER T][0x54]> + <[A][LATIN CAPITAL LETTER A][0x41]> + <[G][LATIN CAPITAL LETTER G][0x47]> + ->s-separate-in-line + ->c-tag-handle + ->s-separate-in-line + ->ns-tag-prefix
 */
val `ns-tag-directive` = token("ns-tag-directive", 'T' + 'A' + 'G' + `s-separate-in-line` + `c-tag-handle` + `s-separate-in-line` + `ns-tag-prefix`)

/**
 * `82` : l-directive:
 * ->c-directive + [->ns-yaml-directive |
 *    ->ns-tag-directive |
 *    ->ns-reserved-directive] + ->s-l-comments
 */
val `l-directive` = token("l-directive", `c-directive` + `ns-yaml-directive` or `ns-tag-directive` or `ns-reserved-directive` + `s-l-comments`)

/**
 * `96` : c-ns-properties(n,c):
 * [->c-ns-tag-property + (->s-separate(n,c) + ->c-ns-anchor-property × ?) |
 *    ->c-ns-anchor-property + (->s-separate(n,c) + ->c-ns-tag-property × ?)]
 */
fun `c-ns-properties`(n: Int, c: InOutMode) = tokenGenerator("c-ns-properties") { (`c-ns-tag-property` + (`s-separate`(n, c) + `c-ns-anchor-property`) * zero_or_once) or (`c-ns-anchor-property` + (`s-separate`(n, c) + `c-ns-tag-property`) * zero_or_once) }

// 97: c-ns-tag-property -> [98, 99, 100]

/**
 * `98` : c-verbatim-tag:
 * ->c-tag + <[<][LESS-THAN SIGN][0x3c]> + (->ns-uri-char × +) + <[>][GREATER-THAN SIGN][0x3e]>
 */
val `c-verbatim-tag` = token("c-verbatim-tag", `c-tag` + '<' + `ns-uri-char` * once_or_more + '>')

/**
 * `99` : c-ns-shorthand-tag:
 * ->c-tag-handle + (->ns-tag-char × +)
 */
val `c-ns-shorthand-tag` = token("c-ns-shorthand-tag", `c-tag-handle` + `ns-tag-char` * once_or_more)

/**
 * `100` : c-non-specific-tag:
 * ->c-tag
 */
val `c-non-specific-tag` = token("c-non-specific-tag", `c-tag`)

/**
 * `97` : c-ns-tag-property:
 * [->c-verbatim-tag |
 *    ->c-ns-shorthand-tag |
 *    ->c-non-specific-tag]
 */
val `c-ns-tag-property` = token("c-ns-tag-property", `c-verbatim-tag` or `c-ns-shorthand-tag` or `c-non-specific-tag`)

// 101: c-ns-anchor-property -> [103]

/**
 * `102` : ns-anchor-char:
 * (->ns-char - ->c-flow-indicator)
 */
val `ns-anchor-char` = token("ns-anchor-char", `ns-char` - `c-flow-indicator`)

/**
 * `103` : ns-anchor-name:
 * (->ns-anchor-char × +)
 */
val `ns-anchor-name` = token("ns-anchor-name", `ns-anchor-char` * once_or_more)

/**
 * `101` : c-ns-anchor-property:
 * ->c-anchor + ->ns-anchor-name
 */
val `c-ns-anchor-property` = token("c-ns-anchor-property", `c-anchor` + `ns-anchor-name`)

/**
 * `104` : c-ns-alias-node:
 * ->c-alias + ->ns-anchor-name
 */
val `c-ns-alias-node` = token("c-ns-alias-node", `c-alias` + `ns-anchor-name`)

/**
 * `105` : e-scalar:
 * ->Empty
 */
val `e-scalar` = token("e-scalar", empty)

/**
 * `106` : e-node:
 * ->e-scalar
 */
val `e-node` = token("e-node", `e-scalar`)

/**
 * `107` : nb-double-char:
 * [->c-ns-esc-char |
 *    (->nb-json - ->c-escape - ->c-double-quote)]
 */
val `nb-double-char` = token("nb-double-char", `c-ns-esc-char` or `nb-json` - `c-escape` - `c-double-quote`)

/**
 * `108` : ns-double-char:
 * (->nb-double-char - ->s-white)
 */
val `ns-double-char` = token("ns-double-char", `nb-double-char` - `s-white`)

/**
 * `109` : c-double-quoted(n,c):
 * ->c-double-quote + ->nb-double-text(n,c) + ->c-double-quote
 */
fun `c-double-quoted`(n: Int, c: InOutMode) = tokenGenerator("c-double-quoted") { `c-double-quote` + `nb-double-text`(n, c) + `c-double-quote` }

/**
 * `110` : nb-double-text(n,c):
 * <c> = ->flow-out ⇒ ->nb-double-multi-line(n)
 * <c> = ->flow-in ⇒ ->nb-double-multi-line(n)
 * <c> = ->block-key ⇒ ->nb-double-one-line
 * <c> = ->flow-key ⇒ ->nb-double-one-line
 */
fun `nb-double-text`(n: Int, c: InOutMode) = tokenGenerator("nb-double-text") { 
    when (c) {
        `flow-out` -> `nb-double-multi-line`(n) named "nb-double-text($c)"
        `flow-in` -> `nb-double-multi-line`(n) named "nb-double-text($c)"
        `block-key` -> `nb-double-one-line` named "nb-double-text($c)"
        `flow-key` -> `nb-double-one-line` named "nb-double-text($c)"
        else -> error("unexpected `c` value `$c`")
    }
 }

/**
 * `111` : nb-double-one-line:
 * (->nb-double-char × *)
 */
val `nb-double-one-line` = token("nb-double-one-line", `nb-double-char` * zero_or_more)

/**
 * `112` : s-double-escaped(n):
 * (->s-white × *) + ->c-escape + ->b-non-content + (->l-empty(n,c = <flow-in>) × *) + ->s-flow-line-prefix(n)
 */
fun `s-double-escaped`(n: Int) = tokenGenerator("s-double-escaped") { `s-white` * zero_or_more + `c-escape` + `b-non-content` + `l-empty`(n, `flow-in`) * zero_or_more + `s-flow-line-prefix`(n) }

/**
 * `113` : s-double-break(n):
 * [->s-double-escaped(n) |
 *    ->s-flow-folded(n)]
 */
fun `s-double-break`(n: Int) = tokenGenerator("s-double-break") { `s-double-escaped`(n) or `s-flow-folded`(n) }

/**
 * `114` : nb-ns-double-in-line:
 * ((->s-white × *) + ->ns-double-char × *)
 */
val `nb-ns-double-in-line` = token("nb-ns-double-in-line", `s-white` * zero_or_more + `ns-double-char` * zero_or_more)

/**
 * `115` : s-double-next-line(n):
 * ->s-double-break(n) + (->ns-double-char + ->nb-ns-double-in-line + [->s-double-next-line(n) |
 *    (->s-white × *)] × ?)
 */
fun `s-double-next-line`(n: Int): Token = tokenGenerator("s-double-next-line") { `s-double-break`(n) + (`ns-double-char` + `nb-ns-double-in-line` + `s-double-next-line`(n) or `s-white` * zero_or_more) * zero_or_once }

/**
 * `116` : nb-double-multi-line(n):
 * ->nb-ns-double-in-line + [->s-double-next-line(n) |
 *    (->s-white × *)]
 */
fun `nb-double-multi-line`(n: Int) = tokenGenerator("nb-double-multi-line") { `nb-ns-double-in-line` + `s-double-next-line`(n) or `s-white` * zero_or_more }

/**
 * `117` : c-quoted-quote:
 * ->c-single-quote + ->c-single-quote
 */
val `c-quoted-quote` = token("c-quoted-quote", `c-single-quote` + `c-single-quote`)

/**
 * `118` : nb-single-char:
 * [->c-quoted-quote |
 *    (->nb-json - ->c-single-quote)]
 */
val `nb-single-char` = token("nb-single-char", `c-quoted-quote` or `nb-json` - `c-single-quote`)

/**
 * `119` : ns-single-char:
 * (->nb-single-char - ->s-white)
 */
val `ns-single-char` = token("ns-single-char", `nb-single-char` - `s-white`)

/**
 * `120` : c-single-quoted(n,c):
 * ->c-single-quote + ->nb-single-text(n,c) + ->c-single-quote
 */
fun `c-single-quoted`(n: Int, c: InOutMode) = tokenGenerator("c-single-quoted") { `c-single-quote` + `nb-single-text`(n, c) + `c-single-quote` }

/**
 * `121` : nb-single-text(n,c):
 * <c> = ->flow-out ⇒ ->nb-single-multi-line(n)
 * <c> = ->flow-in ⇒ ->nb-single-multi-line(n)
 * <c> = ->block-key ⇒ ->nb-single-one-line
 * <c> = ->flow-key ⇒ ->nb-single-one-line
 */
fun `nb-single-text`(n: Int, c: InOutMode) = tokenGenerator("nb-single-text") { 
    when (c) {
        `flow-out` -> `nb-single-multi-line`(n) named "nb-single-text($c)"
        `flow-in` -> `nb-single-multi-line`(n) named "nb-single-text($c)"
        `block-key` -> `nb-single-one-line` named "nb-single-text($c)"
        `flow-key` -> `nb-single-one-line` named "nb-single-text($c)"
        else -> error("unexpected `c` value `$c`")
    }
 }

/**
 * `122` : nb-single-one-line:
 * (->nb-single-char × *)
 */
val `nb-single-one-line` = token("nb-single-one-line", `nb-single-char` * zero_or_more)

/**
 * `123` : nb-ns-single-in-line:
 * ((->s-white × *) + ->ns-single-char × *)
 */
val `nb-ns-single-in-line` = token("nb-ns-single-in-line", `s-white` * zero_or_more + `ns-single-char` * zero_or_more)

/**
 * `124` : s-single-next-line(n):
 * ->s-flow-folded(n) + (->ns-single-char + ->nb-ns-single-in-line + [->s-single-next-line(n) |
 *    (->s-white × *)] × ?)
 */
fun `s-single-next-line`(n: Int): Token = tokenGenerator("s-single-next-line") { `s-flow-folded`(n) + (`ns-single-char` + `nb-ns-single-in-line` + `s-single-next-line`(n) or `s-white` * zero_or_more) * zero_or_once }

/**
 * `125` : nb-single-multi-line(n):
 * ->nb-ns-single-in-line + [->s-single-next-line(n) |
 *    (->s-white × *)]
 */
fun `nb-single-multi-line`(n: Int) = tokenGenerator("nb-single-multi-line") { `nb-ns-single-in-line` + `s-single-next-line`(n) or `s-white` * zero_or_more }

/**
 * `126` : ns-plain-first(c):
 * [(->ns-char - ->c-indicator) |
 *    [->c-mapping-key |
 *    ->c-mapping-value |
 *    ->c-sequence-entry] + ->Followed by an ns-plain-safe(c)]
 */
fun `ns-plain-first`(c: InOutMode) = `ns-char` - `c-indicator` or (`c-mapping-key` or `c-mapping-value` or `c-sequence-entry` + followedByAnNsPlainSafe)

/**
 * `127` : ns-plain-safe(c):
 * <c> = ->flow-out ⇒ ->ns-plain-safe-out
 * <c> = ->flow-in ⇒ ->ns-plain-safe-in
 * <c> = ->block-key ⇒ ->ns-plain-safe-out
 * <c> = ->flow-key ⇒ ->ns-plain-safe-in
 */
fun `ns-plain-safe`(c: InOutMode) = when (c) {
    `flow-out` -> `ns-plain-safe-out` named "ns-plain-safe($c)"
    `flow-in` -> `ns-plain-safe-in` named "ns-plain-safe($c)"
    `block-key` -> `ns-plain-safe-out` named "ns-plain-safe($c)"
    `flow-key` -> `ns-plain-safe-in` named "ns-plain-safe($c)"
    else -> error("unexpected `c` value `$c`")
}

/**
 * `128` : ns-plain-safe-out:
 * ->ns-char
 */
val `ns-plain-safe-out` = token("ns-plain-safe-out", `ns-char`)

/**
 * `129` : ns-plain-safe-in:
 * (->ns-char - ->c-flow-indicator)
 */
val `ns-plain-safe-in` = token("ns-plain-safe-in", `ns-char` - `c-flow-indicator`)

/**
 * `130` : ns-plain-char(c):
 * [(->ns-plain-safe(c) - ->c-mapping-value - ->c-comment) |
 *    ->An ns-char preceding + ->c-comment |
 *    ->c-mapping-value]
 */
fun `ns-plain-char`(c: InOutMode) = tokenGenerator("ns-plain-char") { `ns-plain-safe`(c) - `c-mapping-value` - `c-comment` or (anNsCharPreceding + `c-comment`) or `c-mapping-value` }

/**
 * `131` : ns-plain(n,c):
 * <c> = ->flow-out ⇒ ->ns-plain-multi-line(n,c)
 * <c> = ->flow-in ⇒ ->ns-plain-multi-line(n,c)
 * <c> = ->block-key ⇒ ->ns-plain-one-line(c)
 * <c> = ->flow-key ⇒ ->ns-plain-one-line(c)
 */
fun `ns-plain`(n: Int, c: InOutMode) = tokenGenerator("ns-plain") { 
    when (c) {
        `flow-out` -> `ns-plain-multi-line`(n, c) named "ns-plain($c)"
        `flow-in` -> `ns-plain-multi-line`(n, c) named "ns-plain($c)"
        `block-key` -> `ns-plain-one-line`(c) named "ns-plain($c)"
        `flow-key` -> `ns-plain-one-line`(c) named "ns-plain($c)"
        else -> error("unexpected `c` value `$c`")
    }
 }

/**
 * `132` : nb-ns-plain-in-line(c):
 * ((->s-white × *) + ->ns-plain-char(c) × *)
 */
fun `nb-ns-plain-in-line`(c: InOutMode) = tokenGenerator("nb-ns-plain-in-line") { `s-white` * zero_or_more + `ns-plain-char`(c) * zero_or_more }

/**
 * `133` : ns-plain-one-line(c):
 * ->ns-plain-first(c) + ->nb-ns-plain-in-line(c)
 */
fun `ns-plain-one-line`(c: InOutMode) = tokenGenerator("ns-plain-one-line") { `ns-plain-first`(c) + `nb-ns-plain-in-line`(c) }

/**
 * `134` : s-ns-plain-next-line(n,c):
 * ->s-flow-folded(n) + ->ns-plain-char(c) + ->nb-ns-plain-in-line(c)
 */
fun `s-ns-plain-next-line`(n: Int, c: InOutMode) = tokenGenerator("s-ns-plain-next-line") { `s-flow-folded`(n) + `ns-plain-char`(c) + `nb-ns-plain-in-line`(c) }

/**
 * `135` : ns-plain-multi-line(n,c):
 * ->ns-plain-one-line(c) + (->s-ns-plain-next-line(n,c) × *)
 */
fun `ns-plain-multi-line`(n: Int, c: InOutMode) = tokenGenerator("ns-plain-multi-line") { `ns-plain-one-line`(c) + `s-ns-plain-next-line`(n, c) * zero_or_more }

/**
 * `136` : in-flow(c):
 * <c> = ->flow-out ⇒ ->flow-in
 * <c> = ->flow-in ⇒ ->flow-in
 * <c> = ->block-key ⇒ ->flow-key
 * <c> = ->flow-key ⇒ ->flow-key
 */
fun `in-flow`(c: InOutMode) = when (c) {
    `flow-out` -> `flow-in`
    `flow-in` -> `flow-in`
    `block-key` -> `flow-key`
    `flow-key` -> `flow-key`
    else -> error("unexpected `c` value `$c`")
}

/**
 * `137` : c-flow-sequence(n,c):
 * ->c-sequence-start + (->s-separate(n,c) × ?) + (->ns-s-flow-seq-entries(n,c = ->in-flow(c)) × ?) + ->c-sequence-end
 */
fun `c-flow-sequence`(n: Int, c: InOutMode) = tokenGenerator("c-flow-sequence") { `c-sequence-start` + `s-separate`(n, c) * zero_or_once + `ns-s-flow-seq-entries`(n, `in-flow`(c)) * zero_or_once + `c-sequence-end` }

/**
 * `138` : ns-s-flow-seq-entries(n,c):
 * ->ns-flow-seq-entry(n,c) + (->s-separate(n,c) × ?) + (->c-collect-entry + (->s-separate(n,c) × ?) + (->ns-s-flow-seq-entries(n,c) × ?) × ?)
 */
fun `ns-s-flow-seq-entries`(n: Int, c: InOutMode): Token = tokenGenerator("ns-s-flow-seq-entries") { `ns-flow-seq-entry`(n, c) + `s-separate`(n, c) * zero_or_once + (`c-collect-entry` + `s-separate`(n, c) * zero_or_once + `ns-s-flow-seq-entries`(n, c) * zero_or_once) * zero_or_once }

/**
 * `139` : ns-flow-seq-entry(n,c):
 * [->ns-flow-pair(n,c) |
 *    ->ns-flow-node(n,c)]
 */
fun `ns-flow-seq-entry`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-seq-entry") { `ns-flow-pair`(n, c) or `ns-flow-node`(n, c) }

/**
 * `140` : c-flow-mapping(n,c):
 * ->c-mapping-start + (->s-separate(n,c) × ?) + (->ns-s-flow-map-entries(n,c = ->in-flow(c)) × ?) + ->c-mapping-end
 */
fun `c-flow-mapping`(n: Int, c: InOutMode) = tokenGenerator("c-flow-mapping") { `c-mapping-start` + `s-separate`(n, c) * zero_or_once + `ns-s-flow-map-entries`(n, `in-flow`(c)) * zero_or_once + `c-mapping-end` }

/**
 * `141` : ns-s-flow-map-entries(n,c):
 * ->ns-flow-map-entry(n,c) + (->s-separate(n,c) × ?) + (->c-collect-entry + (->s-separate(n,c) × ?) + (->ns-s-flow-map-entries(n,c) × ?) × ?)
 */
fun `ns-s-flow-map-entries`(n: Int, c: InOutMode): Token = tokenGenerator("ns-s-flow-map-entries") { `ns-flow-map-entry`(n, c) + `s-separate`(n, c) * zero_or_once + (`c-collect-entry` + `s-separate`(n, c) * zero_or_once + `ns-s-flow-map-entries`(n, c) * zero_or_once) * zero_or_once }

/**
 * `142` : ns-flow-map-entry(n,c):
 * [->c-mapping-key + ->s-separate(n,c) + ->ns-flow-map-explicit-entry(n,c) |
 *    ->ns-flow-map-implicit-entry(n,c)]
 */
fun `ns-flow-map-entry`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-map-entry") { (`c-mapping-key` + `s-separate`(n, c) + `ns-flow-map-explicit-entry`(n, c)) or `ns-flow-map-implicit-entry`(n, c) }

/**
 * `143` : ns-flow-map-explicit-entry(n,c):
 * [->ns-flow-map-implicit-entry(n,c) |
 *    ->e-node + ->e-node]
 */
fun `ns-flow-map-explicit-entry`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-map-explicit-entry") { `ns-flow-map-implicit-entry`(n, c) or (`e-node` + `e-node`) }

/**
 * `144` : ns-flow-map-implicit-entry(n,c):
 * [->ns-flow-map-yaml-key-entry(n,c) |
 *    ->c-ns-flow-map-empty-key-entry(n,c) |
 *    ->c-ns-flow-map-json-key-entry(n,c)]
 */
fun `ns-flow-map-implicit-entry`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-map-implicit-entry") { `ns-flow-map-yaml-key-entry`(n, c) or `c-ns-flow-map-empty-key-entry`(n, c) or `c-ns-flow-map-json-key-entry`(n, c) }

/**
 * `145` : ns-flow-map-yaml-key-entry(n,c):
 * ->ns-flow-yaml-node(n,c) + [(->s-separate(n,c) × ?) + ->c-ns-flow-map-separate-value(n,c) |
 *    ->e-node]
 */
fun `ns-flow-map-yaml-key-entry`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-map-yaml-key-entry") { `ns-flow-yaml-node`(n, c) + (`s-separate`(n, c) * zero_or_once + `c-ns-flow-map-separate-value`(n, c)) or `e-node` }

/**
 * `146` : c-ns-flow-map-empty-key-entry(n,c):
 * ->e-node + ->c-ns-flow-map-separate-value(n,c)
 */
fun `c-ns-flow-map-empty-key-entry`(n: Int, c: InOutMode) = tokenGenerator("c-ns-flow-map-empty-key-entry") { `e-node` + `c-ns-flow-map-separate-value`(n, c) }

/**
 * `147` : c-ns-flow-map-separate-value(n,c):
 * ->c-mapping-value + [->s-separate(n,c) + ->ns-flow-node(n,c) |
 *    ->e-node]
 */
fun `c-ns-flow-map-separate-value`(n: Int, c: InOutMode) = tokenGenerator("c-ns-flow-map-separate-value") { `c-mapping-value` + (`s-separate`(n, c) + `ns-flow-node`(n, c)) or `e-node` }

/**
 * `148` : c-ns-flow-map-json-key-entry(n,c):
 * ->c-flow-json-node(n,c) + [(->s-separate(n,c) × ?) + ->c-ns-flow-map-adjacent-value(n,c) |
 *    ->e-node]
 */
fun `c-ns-flow-map-json-key-entry`(n: Int, c: InOutMode) = tokenGenerator("c-ns-flow-map-json-key-entry") { `c-flow-json-node`(n, c) + (`s-separate`(n, c) * zero_or_once + `c-ns-flow-map-adjacent-value`(n, c)) or `e-node` }

/**
 * `149` : c-ns-flow-map-adjacent-value(n,c):
 * ->c-mapping-value + [(->s-separate(n,c) × ?) + ->ns-flow-node(n,c) |
 *    ->e-node]
 */
fun `c-ns-flow-map-adjacent-value`(n: Int, c: InOutMode) = tokenGenerator("c-ns-flow-map-adjacent-value") { `c-mapping-value` + (`s-separate`(n, c) * zero_or_once + `ns-flow-node`(n, c)) or `e-node` }

/**
 * `150` : ns-flow-pair(n,c):
 * [->c-mapping-key + ->s-separate(n,c) + ->ns-flow-map-explicit-entry(n,c) |
 *    ->ns-flow-pair-entry(n,c)]
 */
fun `ns-flow-pair`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-pair") { (`c-mapping-key` + `s-separate`(n, c) + `ns-flow-map-explicit-entry`(n, c)) or `ns-flow-pair-entry`(n, c) }

/**
 * `151` : ns-flow-pair-entry(n,c):
 * [->ns-flow-pair-yaml-key-entry(n,c) |
 *    ->c-ns-flow-map-empty-key-entry(n,c) |
 *    ->c-ns-flow-pair-json-key-entry(n,c)]
 */
fun `ns-flow-pair-entry`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-pair-entry") { `ns-flow-pair-yaml-key-entry`(n, c) or `c-ns-flow-map-empty-key-entry`(n, c) or `c-ns-flow-pair-json-key-entry`(n, c) }

/**
 * `152` : ns-flow-pair-yaml-key-entry(n,c):
 * ->ns-s-implicit-yaml-key(c = <flow-key>) + ->c-ns-flow-map-separate-value(n,c)
 */
fun `ns-flow-pair-yaml-key-entry`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-pair-yaml-key-entry") { `ns-s-implicit-yaml-key`(`flow-key`) + `c-ns-flow-map-separate-value`(n, c) }

/**
 * `153` : c-ns-flow-pair-json-key-entry(n,c):
 * ->c-s-implicit-json-key(c = <flow-key>) + ->c-ns-flow-map-adjacent-value(n,c)
 */
fun `c-ns-flow-pair-json-key-entry`(n: Int, c: InOutMode) = tokenGenerator("c-ns-flow-pair-json-key-entry") { `c-s-implicit-json-key`(`flow-key`) + `c-ns-flow-map-adjacent-value`(n, c) }

/**
 * `154` : ns-s-implicit-yaml-key(c):
 * ->ns-flow-yaml-node(n = <n/a>,c) + (->s-separate-in-line × ?) + ->At most 1024 characters altogether
 */
fun `ns-s-implicit-yaml-key`(c: InOutMode) = tokenGenerator("ns-s-implicit-yaml-key") { `ns-flow-yaml-node`(-1, c) + `s-separate-in-line` * zero_or_once + atMost1024CharactersAltogether }

/**
 * `155` : c-s-implicit-json-key(c):
 * ->c-flow-json-node(n = <n/a>,c) + (->s-separate-in-line × ?) + ->At most 1024 characters altogether
 */
fun `c-s-implicit-json-key`(c: InOutMode) = tokenGenerator("c-s-implicit-json-key") { `c-flow-json-node`(-1, c) + `s-separate-in-line` * zero_or_once + atMost1024CharactersAltogether }

/**
 * `156` : ns-flow-yaml-content(n,c):
 * ->ns-plain(n,c)
 */
fun `ns-flow-yaml-content`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-yaml-content") { `ns-plain`(n, c) }

/**
 * `157` : c-flow-json-content(n,c):
 * [->c-flow-sequence(n,c) |
 *    ->c-flow-mapping(n,c) |
 *    ->c-single-quoted(n,c) |
 *    ->c-double-quoted(n,c)]
 */
fun `c-flow-json-content`(n: Int, c: InOutMode) = tokenGenerator("c-flow-json-content") { `c-flow-sequence`(n, c) or `c-flow-mapping`(n, c) or `c-single-quoted`(n, c) or `c-double-quoted`(n, c) }

/**
 * `158` : ns-flow-content(n,c):
 * [->ns-flow-yaml-content(n,c) |
 *    ->c-flow-json-content(n,c)]
 */
fun `ns-flow-content`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-content") { `ns-flow-yaml-content`(n, c) or `c-flow-json-content`(n, c) }

/**
 * `159` : ns-flow-yaml-node(n,c):
 * [->c-ns-alias-node |
 *    ->ns-flow-yaml-content(n,c) |
 *    ->c-ns-properties(n,c) + [->s-separate(n,c) + ->ns-flow-yaml-content(n,c) |
 *    ->e-scalar]]
 */
fun `ns-flow-yaml-node`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-yaml-node") { `c-ns-alias-node` or `ns-flow-yaml-content`(n, c) or (`c-ns-properties`(n, c) + (`s-separate`(n, c) + `ns-flow-yaml-content`(n, c)) or `e-scalar`) }

/**
 * `160` : c-flow-json-node(n,c):
 * (->c-ns-properties(n,c) + ->s-separate(n,c) × ?) + ->c-flow-json-content(n,c)
 */
fun `c-flow-json-node`(n: Int, c: InOutMode) = tokenGenerator("c-flow-json-node") { (`c-ns-properties`(n, c) + `s-separate`(n, c)) * zero_or_once + `c-flow-json-content`(n, c) }

/**
 * `161` : ns-flow-node(n,c):
 * [->c-ns-alias-node |
 *    ->ns-flow-content(n,c) |
 *    ->c-ns-properties(n,c) + [->s-separate(n,c) + ->ns-flow-content(n,c) |
 *    ->e-scalar]]
 */
fun `ns-flow-node`(n: Int, c: InOutMode) = tokenGenerator("ns-flow-node") { `c-ns-alias-node` or `ns-flow-content`(n, c) or (`c-ns-properties`(n, c) + (`s-separate`(n, c) + `ns-flow-content`(n, c)) or `e-scalar`) }

/**
 * `162` : c-b-block-header(m,t):
 * [->c-indentation-indicator(m) + ->c-chomping-indicator(t) |
 *    ->c-chomping-indicator(t) + ->c-indentation-indicator(m)] + ->s-b-comment
 */
fun `c-b-block-header`(reader: CodePointReader): Pair<Int, ChompMode> {
    var t = `c-chomping-indicator`(reader)
    val m = `c-indentation-indicator`(reader)
    if (t == clip) t = `c-chomping-indicator`(reader)
    // TODO `s-b-comment`
    return m to t
}

/**
 * `163` : c-indentation-indicator(m):
 * ->ns-dec-digit ⇒ ->m = (->ns-dec-digit - <[0][DIGIT ZERO][0x30]>)
 * ->Empty ⇒ ->m = ->auto-detect()
 */
fun `c-indentation-indicator`(reader: CodePointReader): Int {
    with(`ns-dec-digit`.match(reader)) { if (matches) return codePoints[0].toInt() - 0x30 }
    return autoDetectIndentation(reader)
}

/**
 * `164` : c-chomping-indicator(t):
 * <[-][HYPHEN-MINUS][0x2d]> ⇒ <t> = ->strip
 * <[+][PLUS SIGN][0x2b]> ⇒ <t> = ->keep
 * ->Empty ⇒ <t> = ->clip
 */
fun `c-chomping-indicator`(reader: CodePointReader): ChompMode = when {
    reader.accept('-') -> strip
    reader.accept('+') -> keep
    else -> clip
}

/**
 * `165` : b-chomped-last(t):
 * <t> = ->strip ⇒ [->b-non-content |
 *    ->End of file]
 * <t> = ->clip ⇒ [->b-as-line-feed |
 *    ->End of file]
 * <t> = ->keep ⇒ [->b-as-line-feed |
 *    ->End of file]
 */
fun `b-chomped-last`(t: ChompMode) = when (t) {
    strip -> `b-non-content` or endOfFile named "b-chomped-last($t)"
    clip -> `b-as-line-feed` or endOfFile named "b-chomped-last($t)"
    keep -> `b-as-line-feed` or endOfFile named "b-chomped-last($t)"
    else -> error("unexpected `t` value `$t`")
}

/**
 * `166` : l-chomped-empty(n,t):
 * <t> = ->strip ⇒ ->l-strip-empty(n)
 * <t> = ->clip ⇒ ->l-strip-empty(n)
 * <t> = ->keep ⇒ ->l-keep-empty(n)
 */
fun `l-chomped-empty`(n: Int, t: ChompMode) = tokenGenerator("l-chomped-empty") { 
    when (t) {
        strip -> `l-strip-empty`(n) named "l-chomped-empty($t)"
        clip -> `l-strip-empty`(n) named "l-chomped-empty($t)"
        keep -> `l-keep-empty`(n) named "l-chomped-empty($t)"
        else -> error("unexpected `t` value `$t`")
    }
 }

/**
 * `167` : l-strip-empty(n):
 * (->s-indent≤(n) + ->b-non-content × *) + (->l-trail-comments(n) × ?)
 */
fun `l-strip-empty`(n: Int) = tokenGenerator("l-strip-empty") { (`s-indent≤`(n) + `b-non-content`) * zero_or_more + `l-trail-comments`(n) * zero_or_once }

/**
 * `168` : l-keep-empty(n):
 * (->l-empty(n,c = <block-in>) × *) + (->l-trail-comments(n) × ?)
 */
fun `l-keep-empty`(n: Int) = tokenGenerator("l-keep-empty") { `l-empty`(n, `block-in`) * zero_or_more + `l-trail-comments`(n) * zero_or_once }

/**
 * `169` : l-trail-comments(n):
 * ->s-indent<(n) + ->c-nb-comment-text + ->b-comment + (->l-comment × *)
 */
fun `l-trail-comments`(n: Int) = tokenGenerator("l-trail-comments") { `s-indent≪`(n) + `c-nb-comment-text` + `b-comment` + `l-comment` * zero_or_more }

/**
 * `170` : c-l+literal(n):
 * ->c-literal + ->c-b-block-header(m,t) + ->l-literal-content(n = <n+m>,t)
 */
fun `c-l+literal`(n: Int) = tokenGenerator("c-l+literal") { undefined /* TODO global variable */ }

/**
 * `171` : l-nb-literal-text(n):
 * (->l-empty(n,c = <block-in>) × *) + ->s-indent(n) + (->nb-char × +)
 */
fun `l-nb-literal-text`(n: Int) = tokenGenerator("l-nb-literal-text") { `l-empty`(n, `block-in`) * zero_or_more + `s-indent`(n) + `nb-char` * once_or_more }

/**
 * `172` : b-nb-literal-next(n):
 * ->b-as-line-feed + ->l-nb-literal-text(n)
 */
fun `b-nb-literal-next`(n: Int) = tokenGenerator("b-nb-literal-next") { `b-as-line-feed` + `l-nb-literal-text`(n) }

/**
 * `173` : l-literal-content(n,t):
 * (->l-nb-literal-text(n) + (->b-nb-literal-next(n) × *) + ->b-chomped-last(t) × ?) + ->l-chomped-empty(n,t)
 */
fun `l-literal-content`(n: Int, t: ChompMode) = tokenGenerator("l-literal-content") { (`l-nb-literal-text`(n) + `b-nb-literal-next`(n) * zero_or_more + `b-chomped-last`(t)) * zero_or_once + `l-chomped-empty`(n, t) }

/**
 * `174` : c-l+folded(n):
 * ->c-folded + ->c-b-block-header(m,t) + ->l-folded-content(n = <n+m>,t)
 */
fun `c-l+folded`(n: Int) = tokenGenerator("c-l+folded") { undefined /* TODO global variable */ }

/**
 * `175` : s-nb-folded-text(n):
 * ->s-indent(n) + ->ns-char + (->nb-char × *)
 */
fun `s-nb-folded-text`(n: Int) = tokenGenerator("s-nb-folded-text") { `s-indent`(n) + `ns-char` + `nb-char` * zero_or_more }

/**
 * `176` : l-nb-folded-lines(n):
 * ->s-nb-folded-text(n) + (->b-l-folded(n,c = <block-in>) + ->s-nb-folded-text(n) × *)
 */
fun `l-nb-folded-lines`(n: Int) = tokenGenerator("l-nb-folded-lines") { `s-nb-folded-text`(n) + (`b-l-folded`(n, `block-in`) + `s-nb-folded-text`(n)) * zero_or_more }

/**
 * `177` : s-nb-spaced-text(n):
 * ->s-indent(n) + ->s-white + (->nb-char × *)
 */
fun `s-nb-spaced-text`(n: Int) = tokenGenerator("s-nb-spaced-text") { `s-indent`(n) + `s-white` + `nb-char` * zero_or_more }

/**
 * `178` : b-l-spaced(n):
 * ->b-as-line-feed + (->l-empty(n,c = <block-in>) × *)
 */
fun `b-l-spaced`(n: Int) = tokenGenerator("b-l-spaced") { `b-as-line-feed` + `l-empty`(n, `block-in`) * zero_or_more }

/**
 * `179` : l-nb-spaced-lines(n):
 * ->s-nb-spaced-text(n) + (->b-l-spaced(n) + ->s-nb-spaced-text(n) × *)
 */
fun `l-nb-spaced-lines`(n: Int) = tokenGenerator("l-nb-spaced-lines") { `s-nb-spaced-text`(n) + (`b-l-spaced`(n) + `s-nb-spaced-text`(n)) * zero_or_more }

/**
 * `180` : l-nb-same-lines(n):
 * (->l-empty(n,c = <block-in>) × *) + [->l-nb-folded-lines(n) |
 *    ->l-nb-spaced-lines(n)]
 */
fun `l-nb-same-lines`(n: Int) = tokenGenerator("l-nb-same-lines") { `l-empty`(n, `block-in`) * zero_or_more + `l-nb-folded-lines`(n) or `l-nb-spaced-lines`(n) }

/**
 * `181` : l-nb-diff-lines(n):
 * ->l-nb-same-lines(n) + (->b-as-line-feed + ->l-nb-same-lines(n) × *)
 */
fun `l-nb-diff-lines`(n: Int) = tokenGenerator("l-nb-diff-lines") { `l-nb-same-lines`(n) + (`b-as-line-feed` + `l-nb-same-lines`(n)) * zero_or_more }

/**
 * `182` : l-folded-content(n,t):
 * (->l-nb-diff-lines(n) + ->b-chomped-last(t) × ?) + ->l-chomped-empty(n,t)
 */
fun `l-folded-content`(n: Int, t: ChompMode) = tokenGenerator("l-folded-content") { (`l-nb-diff-lines`(n) + `b-chomped-last`(t)) * zero_or_once + `l-chomped-empty`(n, t) }

/**
 * `183` : l+block-sequence(n):
 * (->s-indent(n = <n+m>) + ->c-l-block-seq-entry(n = <n+m>) × +) + ->For some fixed auto-detected m > 0
 */
fun `l+block-sequence`(n: Int) = tokenGenerator("l+block-sequence") { undefined /* TODO other */ }

/**
 * `184` : c-l-block-seq-entry(n):
 * ->c-sequence-entry + ->s-l+block-indented(n,c = <block-in>)
 */
fun `c-l-block-seq-entry`(n: Int) = tokenGenerator("c-l-block-seq-entry") { `c-sequence-entry` + `s-l+block-indented`(n, `block-in`) }

/**
 * `185` : s-l+block-indented(n,c):
 * [->s-indent(n = <m>) + [->ns-l-compact-sequence(n = <n+1+m>) |
 *    ->ns-l-compact-mapping(n = <n+1+m>)] |
 *    ->s-l+block-node(n,c) |
 *    ->e-node + ->s-l-comments]
 */
fun `s-l+block-indented`(n: Int, c: InOutMode) = tokenGenerator("s-l+block-indented") { undefined /* TODO global variable */ }

/**
 * `186` : ns-l-compact-sequence(n):
 * ->c-l-block-seq-entry(n) + (->s-indent(n) + ->c-l-block-seq-entry(n) × *)
 */
fun `ns-l-compact-sequence`(n: Int) = tokenGenerator("ns-l-compact-sequence") { `c-l-block-seq-entry`(n) + (`s-indent`(n) + `c-l-block-seq-entry`(n)) * zero_or_more }

/**
 * `187` : l+block-mapping(n):
 * (->s-indent(n = <n+m>) + ->ns-l-block-map-entry(n = <n+m>) × +) + ->For some fixed auto-detected m > 0
 */
fun `l+block-mapping`(n: Int) = tokenGenerator("l+block-mapping") { undefined /* TODO other */ }

/**
 * `188` : ns-l-block-map-entry(n):
 * [->c-l-block-map-explicit-entry(n) |
 *    ->ns-l-block-map-implicit-entry(n)]
 */
fun `ns-l-block-map-entry`(n: Int) = tokenGenerator("ns-l-block-map-entry") { `c-l-block-map-explicit-entry`(n) or `ns-l-block-map-implicit-entry`(n) }

/**
 * `189` : c-l-block-map-explicit-entry(n):
 * ->c-l-block-map-explicit-key(n) + [->l-block-map-explicit-value(n) |
 *    ->e-node]
 */
fun `c-l-block-map-explicit-entry`(n: Int) = tokenGenerator("c-l-block-map-explicit-entry") { `c-l-block-map-explicit-key`(n) + `l-block-map-explicit-value`(n) or `e-node` }

/**
 * `190` : c-l-block-map-explicit-key(n):
 * ->c-mapping-key + ->s-l+block-indented(n,c = <block-out>)
 */
fun `c-l-block-map-explicit-key`(n: Int) = tokenGenerator("c-l-block-map-explicit-key") { `c-mapping-key` + `s-l+block-indented`(n, `block-out`) }

/**
 * `191` : l-block-map-explicit-value(n):
 * ->s-indent(n) + ->c-mapping-value + ->s-l+block-indented(n,c = <block-out>)
 */
fun `l-block-map-explicit-value`(n: Int) = tokenGenerator("l-block-map-explicit-value") { `s-indent`(n) + `c-mapping-value` + `s-l+block-indented`(n, `block-out`) }

/**
 * `192` : ns-l-block-map-implicit-entry(n):
 * [->ns-s-block-map-implicit-key |
 *    ->e-node] + ->c-l-block-map-implicit-value(n)
 */
fun `ns-l-block-map-implicit-entry`(n: Int) = tokenGenerator("ns-l-block-map-implicit-entry") { `ns-s-block-map-implicit-key` or `e-node` + `c-l-block-map-implicit-value`(n) }

/**
 * `193` : ns-s-block-map-implicit-key:
 * [->c-s-implicit-json-key(c = <block-key>) |
 *    ->ns-s-implicit-yaml-key(c = <block-key>)]
 */
val `ns-s-block-map-implicit-key` = token("ns-s-block-map-implicit-key", `c-s-implicit-json-key`(`block-key`) or `ns-s-implicit-yaml-key`(`block-key`))

/**
 * `194` : c-l-block-map-implicit-value(n):
 * ->c-mapping-value + [->s-l+block-node(n,c = <block-out>) |
 *    ->e-node + ->s-l-comments]
 */
fun `c-l-block-map-implicit-value`(n: Int) = tokenGenerator("c-l-block-map-implicit-value") { `c-mapping-value` + `s-l+block-node`(n, `block-out`) or (`e-node` + `s-l-comments`) }

/**
 * `195` : ns-l-compact-mapping(n):
 * ->ns-l-block-map-entry(n) + (->s-indent(n) + ->ns-l-block-map-entry(n) × *)
 */
fun `ns-l-compact-mapping`(n: Int) = tokenGenerator("ns-l-compact-mapping") { `ns-l-block-map-entry`(n) + (`s-indent`(n) + `ns-l-block-map-entry`(n)) * zero_or_more }

/**
 * `196` : s-l+block-node(n,c):
 * [->s-l+block-in-block(n,c) |
 *    ->s-l+flow-in-block(n)]
 */
fun `s-l+block-node`(n: Int, c: InOutMode) = tokenGenerator("s-l+block-node") { `s-l+block-in-block`(n, c) or `s-l+flow-in-block`(n) }

/**
 * `197` : s-l+flow-in-block(n):
 * ->s-separate(n = <n+1>,c = <flow-out>) + ->ns-flow-node(n = <n+1>,c = <flow-out>) + ->s-l-comments
 */
fun `s-l+flow-in-block`(n: Int) = tokenGenerator("s-l+flow-in-block") { `s-separate`(n + 1, `flow-out`) + `ns-flow-node`(n + 1, `flow-out`) + `s-l-comments` }

/**
 * `198` : s-l+block-in-block(n,c):
 * [->s-l+block-scalar(n,c) |
 *    ->s-l+block-collection(n,c)]
 */
fun `s-l+block-in-block`(n: Int, c: InOutMode) = tokenGenerator("s-l+block-in-block") { `s-l+block-scalar`(n, c) or `s-l+block-collection`(n, c) }

/**
 * `199` : s-l+block-scalar(n,c):
 * ->s-separate(n = <n+1>,c) + (->c-ns-properties(n = <n+1>,c) + ->s-separate(n = <n+1>,c) × ?) + [->c-l+literal(n) |
 *    ->c-l+folded(n)]
 */
fun `s-l+block-scalar`(n: Int, c: InOutMode) = tokenGenerator("s-l+block-scalar") { `s-separate`(n + 1, c) + (`c-ns-properties`(n + 1, c) + `s-separate`(n + 1, c)) * zero_or_once + `c-l+literal`(n) or `c-l+folded`(n) }

/**
 * `200` : s-l+block-collection(n,c):
 * (->s-separate(n = <n+1>,c) + ->c-ns-properties(n = <n+1>,c) × ?) + ->s-l-comments + [->l+block-sequence(n = ->seq-spaces(n,c)) |
 *    ->l+block-mapping(n)]
 */
fun `s-l+block-collection`(n: Int, c: InOutMode) = tokenGenerator("s-l+block-collection") { (`s-separate`(n + 1, c) + `c-ns-properties`(n + 1, c)) * zero_or_once + `s-l-comments` + `l+block-sequence`(`seq-spaces`(n,c)) or `l+block-mapping`(n) }

/**
 * `201` : seq-spaces(n,c):
 * <c> = ->block-out ⇒ ->n-1
 * <c> = ->block-in ⇒ ->n
 */
fun `seq-spaces`(n: Int, c: InOutMode) = when (c) {
    `block-out` -> n - 1
    `block-in` -> n
    else -> error("unexpected `c` value `$c`")
}

/**
 * `202` : l-document-prefix:
 * (->c-byte-order-mark × ?) + (->l-comment × *)
 */
val `l-document-prefix` = token("l-document-prefix", `c-byte-order-mark` * zero_or_once + `l-comment` * zero_or_more)

/**
 * `203` : c-directives-end:
 * <[-][HYPHEN-MINUS][0x2d]> + <[-][HYPHEN-MINUS][0x2d]> + <[-][HYPHEN-MINUS][0x2d]>
 */
val `c-directives-end` = token("c-directives-end", '-' + '-' + '-')

/**
 * `204` : c-document-end:
 * <[.][FULL STOP][0x2e]> + <[.][FULL STOP][0x2e]> + <[.][FULL STOP][0x2e]>
 */
val `c-document-end` = token("c-document-end", '.' + '.' + '.')

/**
 * `205` : l-document-suffix:
 * ->c-document-end + ->s-l-comments
 */
val `l-document-suffix` = token("l-document-suffix", `c-document-end` + `s-l-comments`)

/**
 * `206` : c-forbidden:
 * ->Start of line + [->c-directives-end |
 *    ->c-document-end] + [->b-char |
 *    ->s-white |
 *    ->End of file]
 */
val `c-forbidden` = token("c-forbidden", startOfLine + `c-directives-end` or `c-document-end` + `b-char` or `s-white` or endOfFile)

/**
 * `207` : l-bare-document:
 * ->s-l+block-node(n = <-1>,c = <block-in>) + ->Excluding c-forbidden content
 */
val `l-bare-document` = token("l-bare-document", `s-l+block-node`(-1, `block-in`) + excludingCForbiddenContent)

/**
 * `208` : l-explicit-document:
 * ->c-directives-end + [->l-bare-document |
 *    ->e-node + ->s-l-comments]
 */
val `l-explicit-document` = token("l-explicit-document", `c-directives-end` + `l-bare-document` or (`e-node` + `s-l-comments`))

/**
 * `209` : l-directive-document:
 * (->l-directive × +) + ->l-explicit-document
 */
val `l-directive-document` = token("l-directive-document", `l-directive` * once_or_more + `l-explicit-document`)

/**
 * `210` : l-any-document:
 * [->l-directive-document |
 *    ->l-explicit-document |
 *    ->l-bare-document]
 */
val `l-any-document` = token("l-any-document", `l-directive-document` or `l-explicit-document` or `l-bare-document`)

/**
 * `211` : l-yaml-stream:
 * (->l-document-prefix × *) + (->l-any-document × ?) + ((->l-document-suffix × +) + (->l-document-prefix × *) + [(->l-any-document × ?) |
 *    (->l-document-prefix × *) + (->l-explicit-document × ?)] × *)
 */
val `l-yaml-stream` = token("l-yaml-stream", `l-document-prefix` * zero_or_more + `l-any-document` * zero_or_once + (`l-document-suffix` * once_or_more + `l-document-prefix` * zero_or_more + `l-any-document` * zero_or_once or (`l-document-prefix` * zero_or_more + `l-explicit-document` * zero_or_once)) * zero_or_more)
