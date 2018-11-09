@file:Suppress("unused", "FunctionName", "NonAsciiCharacters")

package com.github.t1.yaml.parser

import com.github.t1.yaml.parser.InOutMode.`block-in`
import com.github.t1.yaml.parser.InOutMode.`block-out`
import com.github.t1.yaml.parser.InOutMode.`flow-in`
import com.github.t1.yaml.parser.InOutMode.`flow-out`
import com.github.t1.yaml.parser.YamlTokens.`s-space`
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePointRange
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Match
import com.github.t1.yaml.tools.Token
import com.github.t1.yaml.tools.empty
import com.github.t1.yaml.tools.symbol
import com.github.t1.yaml.tools.toCodePointRange
import com.github.t1.yaml.tools.token
import com.github.t1.yaml.tools.undefined
import javax.annotation.Generated

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
@Generated("spec.generator.YamlSymbolGenerator")
@Suppress("EnumEntryName")
enum class YamlTokens(private val token: Token) : Token {

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
    `c-printable`('\t' or '\n' or '\r' or (' '..'~') or '\u0085' or (' '..'퟿') or (''..'�') or ("\uD800\uDC00".."\uDBFF\uDFFF")),

    /**
     * `2` : nb-json:
     * [<[\t][CHARACTER TABULATION][0x9]> |
     *    [<[ ][SPACE][0x20]>-<[\uDBFF\uDFFF][?][0x10ffff]>]]
     */
    `nb-json`('\t' or (' '.."\uDBFF\uDFFF")),

    /**
     * `3` : c-byte-order-mark:
     * <[\uFEFF][ZERO WIDTH NO-BREAK SPACE][0xfeff]>
     */
    `c-byte-order-mark`('\uFEFF'),

    /**
     * `4` : c-sequence-entry:
     * <[-][HYPHEN-MINUS][0x2d]>
     */
    `c-sequence-entry`('-'),

    /**
     * `5` : c-mapping-key:
     * <[?][QUESTION MARK][0x3f]>
     */
    `c-mapping-key`('?'),

    /**
     * `6` : c-mapping-value:
     * <[:][COLON][0x3a]>
     */
    `c-mapping-value`(':'),

    /**
     * `7` : c-collect-entry:
     * <[,][COMMA][0x2c]>
     */
    `c-collect-entry`(','),

    /**
     * `8` : c-sequence-start:
     * <[[][LEFT SQUARE BRACKET][0x5b]>
     */
    `c-sequence-start`('['),

    /**
     * `9` : c-sequence-end:
     * <[]][RIGHT SQUARE BRACKET][0x5d]>
     */
    `c-sequence-end`(']'),

    /**
     * `10` : c-mapping-start:
     * <[{][LEFT CURLY BRACKET][0x7b]>
     */
    `c-mapping-start`('{'),

    /**
     * `11` : c-mapping-end:
     * <[}][RIGHT CURLY BRACKET][0x7d]>
     */
    `c-mapping-end`('}'),

    /**
     * `12` : c-comment:
     * <[#][NUMBER SIGN][0x23]>
     */
    `c-comment`('#'),

    /**
     * `13` : c-anchor:
     * <[&][AMPERSAND][0x26]>
     */
    `c-anchor`('&'),

    /**
     * `14` : c-alias:
     * <[*][ASTERISK][0x2a]>
     */
    `c-alias`('*'),

    /**
     * `15` : c-tag:
     * <[!][EXCLAMATION MARK][0x21]>
     */
    `c-tag`('!'),

    /**
     * `16` : c-literal:
     * <[|][VERTICAL LINE][0x7c]>
     */
    `c-literal`('|'),

    /**
     * `17` : c-folded:
     * <[>][GREATER-THAN SIGN][0x3e]>
     */
    `c-folded`('>'),

    /**
     * `18` : c-single-quote:
     * <[\'][APOSTROPHE][0x27]>
     */
    `c-single-quote`('\''),

    /**
     * `19` : c-double-quote:
     * <["][QUOTATION MARK][0x22]>
     */
    `c-double-quote`('"'),

    /**
     * `20` : c-directive:
     * <[%][PERCENT SIGN][0x25]>
     */
    `c-directive`('%'),

    /**
     * `21` : c-reserved:
     * [<[@][COMMERCIAL AT][0x40]> |
     *    <[`][GRAVE ACCENT][0x60]>]
     */
    `c-reserved`('@' or '`'),

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
    `c-indicator`(`c-sequence-entry` or `c-mapping-key` or `c-mapping-value` or `c-collect-entry` or `c-sequence-start` or `c-sequence-end` or `c-mapping-start` or `c-mapping-end` or `c-comment` or `c-anchor` or `c-alias` or `c-tag` or `c-literal` or `c-folded` or `c-single-quote` or `c-double-quote` or `c-directive` or `c-reserved`),

    /**
     * `23` : c-flow-indicator:
     * [->c-collect-entry |
     *    ->c-sequence-start |
     *    ->c-sequence-end |
     *    ->c-mapping-start |
     *    ->c-mapping-end]
     */
    `c-flow-indicator`(`c-collect-entry` or `c-sequence-start` or `c-sequence-end` or `c-mapping-start` or `c-mapping-end`),

    /**
     * `24` : b-line-feed:
     * <[\n][LINE FEED (LF)][0xa]>
     */
    `b-line-feed`('\n'),

    /**
     * `25` : b-carriage-return:
     * <[\r][CARRIAGE RETURN (CR)][0xd]>
     */
    `b-carriage-return`('\r'),

    /**
     * `26` : b-char:
     * [->b-line-feed |
     *    ->b-carriage-return]
     */
    `b-char`(`b-line-feed` or `b-carriage-return`),

    /**
     * `27` : nb-char:
     * (->c-printable - ->b-char - ->c-byte-order-mark)
     */
    `nb-char`(`c-printable` - `b-char` - `c-byte-order-mark`),

    /**
     * `28` : b-break:
     * [->b-carriage-return + ->b-line-feed |
     *    ->b-carriage-return |
     *    ->b-line-feed]
     */
    `b-break`((`b-carriage-return` + `b-line-feed`) or `b-carriage-return` or `b-line-feed`),

    /**
     * `29` : b-as-line-feed:
     * ->b-break
     */
    `b-as-line-feed`(`b-break`),

    /**
     * `30` : b-non-content:
     * ->b-break
     */
    `b-non-content`(`b-break`),

    /**
     * `31` : s-space:
     * <[ ][SPACE][0x20]>
     */
    `s-space`(' '),

    /**
     * `32` : s-tab:
     * <[\t][CHARACTER TABULATION][0x9]>
     */
    `s-tab`('\t'),

    /**
     * `33` : s-white:
     * [->s-space |
     *    ->s-tab]
     */
    `s-white`(`s-space` or `s-tab`),

    /**
     * `34` : ns-char:
     * (->nb-char - ->s-white)
     */
    `ns-char`(`nb-char` - `s-white`),

    /**
     * `35` : ns-dec-digit:
     * [<[0][DIGIT ZERO][0x30]>-<[9][DIGIT NINE][0x39]>]
     */
    `ns-dec-digit`('0'..'9'),

    /**
     * `36` : ns-hex-digit:
     * [->ns-dec-digit |
     *    [<[A][LATIN CAPITAL LETTER A][0x41]>-<[F][LATIN CAPITAL LETTER F][0x46]>] |
     *    [<[a][LATIN SMALL LETTER A][0x61]>-<[f][LATIN SMALL LETTER F][0x66]>]]
     */
    `ns-hex-digit`(`ns-dec-digit` or ('A'..'F') or ('a'..'f')),

    /**
     * `37` : ns-ascii-letter:
     * [[<[A][LATIN CAPITAL LETTER A][0x41]>-<[Z][LATIN CAPITAL LETTER Z][0x5a]>] |
     *    [<[a][LATIN SMALL LETTER A][0x61]>-<[z][LATIN SMALL LETTER Z][0x7a]>]]
     */
    `ns-ascii-letter`(('A'..'Z') or ('a'..'z')),

    /**
     * `38` : ns-word-char:
     * [->ns-dec-digit |
     *    ->ns-ascii-letter |
     *    <[-][HYPHEN-MINUS][0x2d]>]
     */
    `ns-word-char`(`ns-dec-digit` or `ns-ascii-letter` or '-'),

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
    `ns-uri-char`('%' + '#' + ';' + '/' + '?' + ':' + '@' + '&' + '=' + '+' + '$' + ',' + '_' + '.' + '!' + '~' + '*' + '\'' + '(' + ')' + '[' + ']'),

    /**
     * `40` : ns-tag-char:
     * (->ns-uri-char - ->c-tag - ->c-flow-indicator)
     */
    `ns-tag-char`(`ns-uri-char` - `c-tag` - `c-flow-indicator`),

    /**
     * `41` : c-escape:
     * <[\\][REVERSE SOLIDUS][0x5c]>
     */
    `c-escape`('\\'),

    /**
     * `42` : ns-esc-null:
     * <[0][DIGIT ZERO][0x30]>
     */
    `ns-esc-null`('0'),

    /**
     * `43` : ns-esc-bell:
     * <[a][LATIN SMALL LETTER A][0x61]>
     */
    `ns-esc-bell`('a'),

    /**
     * `44` : ns-esc-backspace:
     * <[b][LATIN SMALL LETTER B][0x62]>
     */
    `ns-esc-backspace`('b'),

    /**
     * `45` : ns-esc-horizontal-tab:
     * [<[t][LATIN SMALL LETTER T][0x74]> |
     *    <[\t][CHARACTER TABULATION][0x9]>]
     */
    `ns-esc-horizontal-tab`('t' or '\t'),

    /**
     * `46` : ns-esc-line-feed:
     * <[n][LATIN SMALL LETTER N][0x6e]>
     */
    `ns-esc-line-feed`('n'),

    /**
     * `47` : ns-esc-vertical-tab:
     * <[v][LATIN SMALL LETTER V][0x76]>
     */
    `ns-esc-vertical-tab`('v'),

    /**
     * `48` : ns-esc-form-feed:
     * <[f][LATIN SMALL LETTER F][0x66]>
     */
    `ns-esc-form-feed`('f'),

    /**
     * `49` : ns-esc-carriage-return:
     * <[r][LATIN SMALL LETTER R][0x72]>
     */
    `ns-esc-carriage-return`('r'),

    /**
     * `50` : ns-esc-escape:
     * <[e][LATIN SMALL LETTER E][0x65]>
     */
    `ns-esc-escape`('e'),

    /**
     * `51` : ns-esc-space:
     * <[ ][SPACE][0x20]>
     */
    `ns-esc-space`(' '),

    /**
     * `52` : ns-esc-double-quote:
     * ->c-double-quote
     */
    `ns-esc-double-quote`(`c-double-quote`),

    /**
     * `53` : ns-esc-slash:
     * <[/][SOLIDUS][0x2f]>
     */
    `ns-esc-slash`('/'),

    /**
     * `54` : ns-esc-backslash:
     * ->c-escape
     */
    `ns-esc-backslash`(`c-escape`),

    /**
     * `55` : ns-esc-next-line:
     * <[N][LATIN CAPITAL LETTER N][0x4e]>
     */
    `ns-esc-next-line`('N'),

    /**
     * `56` : ns-esc-non-breaking-space:
     * <[_][LOW LINE][0x5f]>
     */
    `ns-esc-non-breaking-space`('_'),

    /**
     * `57` : ns-esc-line-separator:
     * <[L][LATIN CAPITAL LETTER L][0x4c]>
     */
    `ns-esc-line-separator`('L'),

    /**
     * `58` : ns-esc-paragraph-separator:
     * <[P][LATIN CAPITAL LETTER P][0x50]>
     */
    `ns-esc-paragraph-separator`('P'),

    /**
     * `59` : ns-esc-8-bit:
     * <[x][LATIN SMALL LETTER X][0x78]> + (->ns-hex-digit × 2)
     */
    `ns-esc-8-bit`('x'),

    /**
     * `60` : ns-esc-16-bit:
     * <[u][LATIN SMALL LETTER U][0x75]> + (->ns-hex-digit × 4)
     */
    `ns-esc-16-bit`('u'),

    /**
     * `61` : ns-esc-32-bit:
     * <[U][LATIN CAPITAL LETTER U][0x55]> + (->ns-hex-digit × 8)
     */
    `ns-esc-32-bit`('U'),

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
    `c-ns-esc-char`(),

    /**
     * `66` : s-separate-in-line:
     * [(->s-white × +) |
     *    ->Start of line]
     */
    /* TODO not generated */

    /**
     * `72` : b-as-space:
     * ->b-break
     */
    `b-as-space`(`b-break`),

    /**
     * `75` : c-nb-comment-text:
     * ->c-comment + (->nb-char × *)
     */
    `c-nb-comment-text`(),

    /**
     * `76` : b-comment:
     * [->b-non-content |
     *    ->End of file]
     */
    `b-comment`(`b-non-content` or EOF),

    /**
     * `77` : s-b-comment:
     * (->s-separate-in-line + (->c-nb-comment-text × ?) × ?) + ->b-comment
     */
    `s-b-comment`(),

    /**
     * `78` : l-comment:
     * ->s-separate-in-line + (->c-nb-comment-text × ?) + ->b-comment
     */
    `l-comment`(),

    /**
     * `79` : s-l-comments:
     * [->s-b-comment |
     *    ->Start of line] + (->l-comment × *)
     */
    `s-l-comments`(),

    /**
     * `82` : l-directive:
     * ->c-directive + [->ns-yaml-directive |
     *    ->ns-tag-directive |
     *    ->ns-reserved-directive] + ->s-l-comments
     */
    `l-directive`(),

    /**
     * `83` : ns-reserved-directive:
     * ->ns-directive-name + (->s-separate-in-line + ->ns-directive-parameter × *)
     */
    `ns-reserved-directive`(),

    /**
     * `84` : ns-directive-name:
     * (->ns-char × +)
     */
    /* TODO not generated */

    /**
     * `85` : ns-directive-parameter:
     * (->ns-char × +)
     */
    /* TODO not generated */

    /**
     * `86` : ns-yaml-directive:
     * <[Y][LATIN CAPITAL LETTER Y][0x59]> + <[A][LATIN CAPITAL LETTER A][0x41]> + <[M][LATIN CAPITAL LETTER M][0x4d]> + <[L][LATIN CAPITAL LETTER L][0x4c]> + ->s-separate-in-line + ->ns-yaml-version
     */
    `ns-yaml-directive`('Y' + 'A' + 'M' + 'L'),

    /**
     * `87` : ns-yaml-version:
     * (->ns-dec-digit × +) + <[.][FULL STOP][0x2e]> + (->ns-dec-digit × +)
     */
    /* TODO not generated */

    /**
     * `88` : ns-tag-directive:
     * <[T][LATIN CAPITAL LETTER T][0x54]> + <[A][LATIN CAPITAL LETTER A][0x41]> + <[G][LATIN CAPITAL LETTER G][0x47]> + ->s-separate-in-line + ->c-tag-handle + ->s-separate-in-line + ->ns-tag-prefix
     */
    `ns-tag-directive`('T' + 'A' + 'G'),

    /**
     * `89` : c-tag-handle:
     * [->c-named-tag-handle |
     *    ->c-secondary-tag-handle |
     *    ->c-primary-tag-handle]
     */
    /* TODO not generated */

    /**
     * `90` : c-primary-tag-handle:
     * ->c-tag
     */
    `c-primary-tag-handle`(`c-tag`),

    /**
     * `91` : c-secondary-tag-handle:
     * ->c-tag + ->c-tag
     */
    `c-secondary-tag-handle`(),

    /**
     * `92` : c-named-tag-handle:
     * ->c-tag + (->ns-word-char × +) + ->c-tag
     */
    `c-named-tag-handle`(),

    /**
     * `93` : ns-tag-prefix:
     * [->c-ns-local-tag-prefix |
     *    ->ns-global-tag-prefix]
     */
    /* TODO not generated */

    /**
     * `94` : c-ns-local-tag-prefix:
     * ->c-tag + (->ns-uri-char × *)
     */
    `c-ns-local-tag-prefix`(),

    /**
     * `95` : ns-global-tag-prefix:
     * ->ns-tag-char + (->ns-uri-char × *)
     */
    `ns-global-tag-prefix`(),

    /**
     * `97` : c-ns-tag-property:
     * [->c-verbatim-tag |
     *    ->c-ns-shorthand-tag |
     *    ->c-non-specific-tag]
     */
    /* TODO not generated */

    /**
     * `98` : c-verbatim-tag:
     * ->c-tag + <[<][LESS-THAN SIGN][0x3c]> + (->ns-uri-char × +) + <[>][GREATER-THAN SIGN][0x3e]>
     */
    /* TODO not generated */

    /**
     * `99` : c-ns-shorthand-tag:
     * ->c-tag-handle + (->ns-tag-char × +)
     */
    `c-ns-shorthand-tag`(),

    /**
     * `100` : c-non-specific-tag:
     * ->c-tag
     */
    `c-non-specific-tag`(`c-tag`),

    /**
     * `101` : c-ns-anchor-property:
     * ->c-anchor + ->ns-anchor-name
     */
    `c-ns-anchor-property`(),

    /**
     * `102` : ns-anchor-char:
     * (->ns-char - ->c-flow-indicator)
     */
    `ns-anchor-char`(`ns-char` - `c-flow-indicator`),

    /**
     * `103` : ns-anchor-name:
     * (->ns-anchor-char × +)
     */
    /* TODO not generated */

    /**
     * `104` : c-ns-alias-node:
     * ->c-alias + ->ns-anchor-name
     */
    `c-ns-alias-node`(),

    /**
     * `105` : e-scalar:
     * ->Empty
     */
    `e-scalar`(empty),

    /**
     * `106` : e-node:
     * ->e-scalar
     */
    `e-node`(`e-scalar`),

    /**
     * `107` : nb-double-char:
     * [->c-ns-esc-char |
     *    (->nb-json - ->c-escape - ->c-double-quote)]
     */
    `nb-double-char`(`c-ns-esc-char` or `nb-json` or `c-escape` or `c-double-quote`),

    /**
     * `108` : ns-double-char:
     * (->nb-double-char - ->s-white)
     */
    `ns-double-char`(`nb-double-char` - `s-white`),

    /**
     * `111` : nb-double-one-line:
     * (->nb-double-char × *)
     */
    /* TODO not generated */

    /**
     * `114` : nb-ns-double-in-line:
     * ((->s-white × *) + ->ns-double-char × *)
     */
    /* TODO not generated */

    /**
     * `117` : c-quoted-quote:
     * ->c-single-quote + ->c-single-quote
     */
    `c-quoted-quote`(),

    /**
     * `118` : nb-single-char:
     * [->c-quoted-quote |
     *    (->nb-json - ->c-single-quote)]
     */
    `nb-single-char`(`c-quoted-quote` or `nb-json` or `c-single-quote`),

    /**
     * `119` : ns-single-char:
     * (->nb-single-char - ->s-white)
     */
    `ns-single-char`(`nb-single-char` - `s-white`),

    /**
     * `122` : nb-single-one-line:
     * (->nb-single-char × *)
     */
    /* TODO not generated */

    /**
     * `123` : nb-ns-single-in-line:
     * ((->s-white × *) + ->ns-single-char × *)
     */
    /* TODO not generated */

    /**
     * `128` : ns-plain-safe-out:
     * ->ns-char
     */
    `ns-plain-safe-out`(`ns-char`),

    /**
     * `129` : ns-plain-safe-in:
     * (->ns-char - ->c-flow-indicator)
     */
    `ns-plain-safe-in`(`ns-char` - `c-flow-indicator`),

    /**
     * `193` : ns-s-block-map-implicit-key:
     * [->c-s-implicit-json-key(c) |
     *    ->ns-s-implicit-yaml-key(c)]
     */
    /* TODO not generated */

    /**
     * `202` : l-document-prefix:
     * (->c-byte-order-mark × ?) + (->l-comment × *)
     */
    `l-document-prefix`(),

    /**
     * `203` : c-directives-end:
     * <[-][HYPHEN-MINUS][0x2d]> + <[-][HYPHEN-MINUS][0x2d]> + <[-][HYPHEN-MINUS][0x2d]>
     */
    `c-directives-end`('-' + '-' + '-'),

    /**
     * `204` : c-document-end:
     * <[.][FULL STOP][0x2e]> + <[.][FULL STOP][0x2e]> + <[.][FULL STOP][0x2e]>
     */
    `c-document-end`('.' + '.' + '.'),

    /**
     * `205` : l-document-suffix:
     * ->c-document-end + ->s-l-comments
     */
    `l-document-suffix`(),

    /**
     * `206` : c-forbidden:
     * ->Start of line + [->c-directives-end |
     *    ->c-document-end] + [->b-char |
     *    ->s-white |
     *    ->End of file]
     */
    `c-forbidden`(),

    /**
     * `207` : l-bare-document:
     * ->s-l+block-node(n,c) + ->Excluding c-forbidden content
     */
    /* TODO not generated */

    /**
     * `208` : l-explicit-document:
     * ->c-directives-end + [->l-bare-document |
     *    ->e-node + ->s-l-comments]
     */
    `l-explicit-document`(),

    /**
     * `209` : l-directive-document:
     * (->l-directive × +) + ->l-explicit-document
     */
    `l-directive-document`(),

    /**
     * `210` : l-any-document:
     * [->l-directive-document |
     *    ->l-explicit-document |
     *    ->l-bare-document]
     */
    /* TODO not generated */

    /**
     * `211` : l-yaml-stream:
     * (->l-document-prefix × *) + (->l-any-document × ?) + ((->l-document-suffix × +) + (->l-document-prefix × *) + [(->l-any-document × ?) |
     *    (->l-document-prefix × *) + (->l-explicit-document × ?)] × *)
     */
    `l-yaml-stream`(),
    ;


    @Deprecated("not yet generated") constructor() : this(undefined)
    constructor(codePoint: Char) : this(symbol(codePoint))
    constructor(range: CharRange) : this(range.toCodePointRange())
    constructor(range: CodePointRange) : this(symbol(range))

    override fun match(reader: CodePointReader): Match = this.token.match(reader)
}

private infix fun Char.or(that: Char) = symbol(this) or symbol(that)
private infix fun Char.or(that: Token) = symbol(this) or that
private infix fun CharRange.or(that: CharRange): Token = symbol(CodePoint.of(this.first)..CodePoint.of(this.last)) or symbol(CodePoint.of(that.first)..CodePoint.of(that.last))
private infix fun Token.or(that: String): Token = or(symbol(that))
private infix fun Token.or(that: Char): Token = or(symbol(that))
private infix operator fun Char.rangeTo(that: Char) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun Char.rangeTo(that: String) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun String.rangeTo(that: String) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun Char.plus(that: Char) = symbol(this) + symbol(that)
private infix operator fun Token.plus(that: Char) = this + symbol(that)
private infix fun Token.or(range: CharRange) = this.or(symbol(range.toCodePointRange()))
private val EOF = symbol(CodePoint.EOF)

/**
 * `63` : s-indent(n):
 * (->s-space × n)
 */
fun `s-indent`(n: Int): Token {
    val token = `s-space` * n
    return token("s-indent($n)") { token.match(it) }
}

/**
 * `64` : s-indent(<n):
 * (->s-space × m /* Where m < n */)
 */
fun `s-indent≪`(n: Int) = token("s-indent(<$n)") { reader ->
    val match = reader.mark { reader.readWhile { reader -> `s-space`.match(reader).codePoints } }
    if (match.size >= n) return@token Match(matches = false)
    reader.read(match.size)
    return@token Match(matches = true, codePoints = match)
}

/**
 * `65` : s-indent(≤n):
 * (->s-space × m /* Where m ≤ n */)
 */
fun `s-indent≤`(n: Int) = token("s-indent(≤$n)") { reader ->
    val match = reader.mark { reader.readWhile { reader -> `s-space`.match(reader).codePoints } }
    if (match.size > n) return@token Match(matches = false)
    reader.read(match.size)
    return@token Match(matches = true, codePoints = match)
}

/**
 * `67` : s-line-prefix(n,c):
 * <c> = ->block-out ⇒ ->s-block-line-prefix(n)
 * <c> = ->block-in ⇒ ->s-block-line-prefix(n)
 * <c> = ->flow-out ⇒ ->s-flow-line-prefix(n)
 * <c> = ->flow-in ⇒ ->s-flow-line-prefix(n)
 */
fun `s-line-prefix`(n: Int, c: InOutMode) = when (c) {
    `block-out` -> `s-block-line-prefix`(n) describedAs "s-line-prefix($c)"
    `block-in` -> `s-block-line-prefix`(n) describedAs "s-line-prefix($c)"
    `flow-out` -> `s-flow-line-prefix`(n) describedAs "s-line-prefix($c)"
    `flow-in` -> `s-flow-line-prefix`(n) describedAs "s-line-prefix($c)"
}

/**
 * `68` : s-block-line-prefix(n):
 * ->s-indent(n)
 */
fun `s-block-line-prefix`(n: Int) = `s-indent`(n)

/**
 * `69` : s-flow-line-prefix(n):
 * ->s-indent(n) + (->s-separate-in-line × ?)
 */
fun `s-flow-line-prefix`(n: Int) = `s-indent`(n)
