@file:Suppress("unused", "ObjectPropertyName", "NonAsciiCharacters")

package com.github.t1.yaml.parser

import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.Token
import com.github.t1.yaml.tools.symbol
import com.github.t1.yaml.tools.toCodePointRange
import com.github.t1.yaml.tools.undefined

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

/**
 * `1` : c-printable:
 *   [<[\t][CHARACTER TABULATION][0x9]> ||
 *    <[\n][LINE FEED (LF)][0xa]> ||
 *    <[\r][CARRIAGE RETURN (CR)][0xd]> ||
 *    [<[ ][SPACE][0x20]>-<[~][TILDE][0x7e]>] ||
 *    <[\u0085][NEXT LINE (NEL)][0x85]> ||
 *    [<[ ][NO-BREAK SPACE][0xa0]>-<[퟿][?][0xd7ff]>] ||
 *    [<[][PRIVATE USE AREA E000][0xe000]>-<[�][REPLACEMENT CHARACTER][0xfffd]>] ||
 *    [<[\uD800\uDC00][LINEAR B SYLLABLE B008 A][0x10000]>-<[\uDBFF\uDFFF][?][0x10ffff]>]]
 */
val `c-printable` = '\t' or '\n' or '\r' or (' '..'~') or '\u0085' or (' '..'퟿') or (''..'�') or ("\uD800\uDC00".."\uDBFF\uDFFF")

/**
 * `2` : nb-json:
 *   [<[\t][CHARACTER TABULATION][0x9]> ||
 *    [<[ ][SPACE][0x20]>-<[\uDBFF\uDFFF][?][0x10ffff]>]]
 */
val `nb-json` = '\t' or (' '.."\uDBFF\uDFFF")

/**
 * `3` : c-byte-order-mark:
 *   <[\uFEFF][ZERO WIDTH NO-BREAK SPACE][0xfeff]>
 */
val `c-byte-order-mark` = symbol('\uFEFF')

/**
 * `4` : c-sequence-entry:
 *   <[-][HYPHEN-MINUS][0x2d]>
 */
val `c-sequence-entry` = symbol('-')

/**
 * `5` : c-mapping-key:
 *   <[?][QUESTION MARK][0x3f]>
 */
val `c-mapping-key` = symbol('?')

/**
 * `6` : c-mapping-value:
 *   <[:][COLON][0x3a]>
 */
val `c-mapping-value` = symbol(':')

/**
 * `7` : c-collect-entry:
 *   <[,][COMMA][0x2c]>
 */
val `c-collect-entry` = symbol(',')

/**
 * `8` : c-sequence-start:
 *   <[[][LEFT SQUARE BRACKET][0x5b]>
 */
val `c-sequence-start` = symbol('[') describedAs "c-sequence-start"

/**
 * `9` : c-sequence-end:
 *   <[]][RIGHT SQUARE BRACKET][0x5d]>
 */
val `c-sequence-end` = symbol(']')

/**
 * `10` : c-mapping-start:
 *   <[{][LEFT CURLY BRACKET][0x7b]>
 */
val `c-mapping-start` = symbol('{') describedAs "c-mapping-start"

/**
 * `11` : c-mapping-end:
 *   <[}][RIGHT CURLY BRACKET][0x7d]>
 */
val `c-mapping-end` = symbol('}')

/**
 * `12` : c-comment:
 *   <[#][NUMBER SIGN][0x23]>
 */
val `c-comment` = symbol('#')

/**
 * `13` : c-anchor:
 *   <[&][AMPERSAND][0x26]>
 */
val `c-anchor` = symbol('&')

/**
 * `14` : c-alias:
 *   <[*][ASTERISK][0x2a]>
 */
val `c-alias` = symbol('*')

/**
 * `15` : c-tag:
 *   <[!][EXCLAMATION MARK][0x21]>
 */
val `c-tag` = symbol('!')

/**
 * `16` : c-literal:
 *   <[|][VERTICAL LINE][0x7c]>
 */
val `c-literal` = symbol('|')

/**
 * `17` : c-folded:
 *   <[>][GREATER-THAN SIGN][0x3e]>
 */
val `c-folded` = symbol('>')

/**
 * `18` : c-single-quote:
 *   <[\'][APOSTROPHE][0x27]>
 */
val `c-single-quote` = symbol('\'')

/**
 * `19` : c-double-quote:
 *   <["][QUOTATION MARK][0x22]>
 */
val `c-double-quote` = symbol('"')

/**
 * `20` : c-directive:
 *   <[%][PERCENT SIGN][0x25]>
 */
val `c-directive` = symbol('%')

/**
 * `21` : c-reserved:
 *   [<[@][COMMERCIAL AT][0x40]> ||
 *    <[`][GRAVE ACCENT][0x60]>]
 */
val `c-reserved` = symbol('@') or '`'

/**
 * `22` : c-indicator:
 *   [->c-sequence-entry ||
 *    ->c-mapping-key ||
 *    ->c-mapping-value ||
 *    ->c-collect-entry ||
 *    ->c-sequence-start ||
 *    ->c-sequence-end ||
 *    ->c-mapping-start ||
 *    ->c-mapping-end ||
 *    ->c-comment ||
 *    ->c-anchor ||
 *    ->c-alias ||
 *    ->c-tag ||
 *    ->c-literal ||
 *    ->c-folded ||
 *    ->c-single-quote ||
 *    ->c-double-quote ||
 *    ->c-directive ||
 *    ->c-reserved]
 */
val `c-indicator` = `c-sequence-entry` or `c-mapping-key` or `c-mapping-value` or `c-collect-entry` or `c-sequence-start` or `c-sequence-end` or `c-mapping-start` or `c-mapping-end` or `c-comment` or `c-anchor` or `c-alias` or `c-tag` or `c-literal` or `c-folded` or `c-single-quote` or `c-double-quote` or `c-directive` or `c-reserved`

/**
 * `23` : c-flow-indicator:
 *   [->c-collect-entry ||
 *    ->c-sequence-start ||
 *    ->c-sequence-end ||
 *    ->c-mapping-start ||
 *    ->c-mapping-end]
 */
val `c-flow-indicator` = `c-collect-entry` or `c-sequence-start` or `c-sequence-end` or `c-mapping-start` or `c-mapping-end`

/**
 * `24` : b-line-feed:
 *   <[\n][LINE FEED (LF)][0xa]>
 */
val `b-line-feed` = symbol('\n')

/**
 * `25` : b-carriage-return:
 *   <[\r][CARRIAGE RETURN (CR)][0xd]>
 */
val `b-carriage-return` = symbol('\r')

/**
 * `26` : b-char:
 *   [->b-line-feed ||
 *    ->b-carriage-return]
 */
val `b-char` = `b-line-feed` or `b-carriage-return`

/**
 * `27` : nb-char:
 *   ->c-printable - ->b-char - ->c-byte-order-mark
 */
val `nb-char` = undefined

/**
 * `28` : b-break:
 *   [->b-carriage-return + ->b-line-feed ||
 *    ->b-carriage-return ||
 *    ->b-line-feed]
 */
val `b-break` = (`b-carriage-return` + `b-line-feed`) or `b-carriage-return` or `b-line-feed`

/**
 * `29` : b-as-line-feed:
 *   ->b-break
 */
val `b-as-line-feed` = `b-break`

/**
 * `30` : b-non-content:
 *   ->b-break
 */
val `b-non-content` = `b-break`

/**
 * `31` : s-space:
 *   <[ ][SPACE][0x20]>
 */
val `s-space` = symbol(' ')

/**
 * `32` : s-tab:
 *   <[\t][CHARACTER TABULATION][0x9]>
 */
val `s-tab` = symbol('\t')

/**
 * `33` : s-white:
 *   [->s-space ||
 *    ->s-tab]
 */
val `s-white` = `s-space` or `s-tab`

/**
 * `34` : ns-char:
 *   ->nb-char - ->s-white
 */
val `ns-char` = undefined

/**
 * `35` : ns-dec-digit:
 *   [<[0][DIGIT ZERO][0x30]>-<[9][DIGIT NINE][0x39]>]
 */
val `ns-dec-digit` = token('0'..'9')

/**
 * `36` : ns-hex-digit:
 *   [->ns-dec-digit ||
 *    [<[A][LATIN CAPITAL LETTER A][0x41]>-<[F][LATIN CAPITAL LETTER F][0x46]>] ||
 *    [<[a][LATIN SMALL LETTER A][0x61]>-<[f][LATIN SMALL LETTER F][0x66]>]]
 */
val `ns-hex-digit` = `ns-dec-digit` or ('A'..'F') or ('a'..'f')

/**
 * `37` : ns-ascii-letter:
 *   [[<[A][LATIN CAPITAL LETTER A][0x41]>-<[Z][LATIN CAPITAL LETTER Z][0x5a]>] ||
 *    [<[a][LATIN SMALL LETTER A][0x61]>-<[z][LATIN SMALL LETTER Z][0x7a]>]]
 */
val `ns-ascii-letter` = undefined

/**
 * `38` : ns-word-char:
 *   [->ns-dec-digit ||
 *    ->ns-ascii-letter ||
 *    <[-][HYPHEN-MINUS][0x2d]>]
 */
val `ns-word-char` = `ns-dec-digit` or `ns-ascii-letter` or '-'

/**
 * `39` : ns-uri-char:
 *   <[%][PERCENT SIGN][0x25]> + ->ns-hex-digit + [->ns-hex-digit ||
 *    ->ns-word-char ||
 *    <[#][NUMBER SIGN][0x23]> ||
 *    <[;][SEMICOLON][0x3b]> ||
 *    <[/][SOLIDUS][0x2f]> ||
 *    <[?][QUESTION MARK][0x3f]> ||
 *    <[:][COLON][0x3a]> ||
 *    <[@][COMMERCIAL AT][0x40]> ||
 *    <[&][AMPERSAND][0x26]> ||
 *    <[=][EQUALS SIGN][0x3d]> ||
 *    <[+][PLUS SIGN][0x2b]> ||
 *    <[$][DOLLAR SIGN][0x24]> ||
 *    <[,][COMMA][0x2c]> ||
 *    <[_][LOW LINE][0x5f]> ||
 *    <[.][FULL STOP][0x2e]> ||
 *    <[!][EXCLAMATION MARK][0x21]> ||
 *    <[~][TILDE][0x7e]> ||
 *    <[*][ASTERISK][0x2a]> ||
 *    <[\'][APOSTROPHE][0x27]> ||
 *    <[(][LEFT PARENTHESIS][0x28]> ||
 *    <[)][RIGHT PARENTHESIS][0x29]> ||
 *    <[[][LEFT SQUARE BRACKET][0x5b]> ||
 *    <[]][RIGHT SQUARE BRACKET][0x5d]>]
 */
val `ns-uri-char` = symbol('%') + '#' + ';' + '/' + '?' + ':' + '@' + '&' + '=' + '+' + '$' + ',' + '_' + '.' + '!' + '~' + '*' + '\'' + '(' + ')' + '[' + ']'

/**
 * `40` : ns-tag-char:
 *   ->ns-uri-char - ->c-tag - ->c-flow-indicator
 */
val `ns-tag-char` = undefined

/**
 * `41` : c-escape:
 *   <[\\][REVERSE SOLIDUS][0x5c]>
 */
val `c-escape` = symbol('\\')

/**
 * `42` : ns-esc-null:
 *   <[0][DIGIT ZERO][0x30]>
 */
val `ns-esc-null` = symbol('0')

/**
 * `43` : ns-esc-bell:
 *   <[a][LATIN SMALL LETTER A][0x61]>
 */
val `ns-esc-bell` = symbol('a')

/**
 * `44` : ns-esc-backspace:
 *   <[b][LATIN SMALL LETTER B][0x62]>
 */
val `ns-esc-backspace` = symbol('b')

/**
 * `45` : ns-esc-horizontal-tab:
 *   [<[t][LATIN SMALL LETTER T][0x74]> ||
 *    <[\t][CHARACTER TABULATION][0x9]>]
 */
val `ns-esc-horizontal-tab` = symbol('t') or '\t'

/**
 * `46` : ns-esc-line-feed:
 *   <[n][LATIN SMALL LETTER N][0x6e]>
 */
val `ns-esc-line-feed` = symbol('n')

/**
 * `47` : ns-esc-vertical-tab:
 *   <[v][LATIN SMALL LETTER V][0x76]>
 */
val `ns-esc-vertical-tab` = symbol('v')

/**
 * `48` : ns-esc-form-feed:
 *   <[f][LATIN SMALL LETTER F][0x66]>
 */
val `ns-esc-form-feed` = symbol('f')

/**
 * `49` : ns-esc-carriage-return:
 *   <[r][LATIN SMALL LETTER R][0x72]>
 */
val `ns-esc-carriage-return` = symbol('r')

/**
 * `50` : ns-esc-escape:
 *   <[e][LATIN SMALL LETTER E][0x65]>
 */
val `ns-esc-escape` = symbol('e')

/**
 * `51` : ns-esc-space:
 *   <[ ][SPACE][0x20]>
 */
val `ns-esc-space` = symbol(' ')

/**
 * `52` : ns-esc-double-quote:
 *   ->c-double-quote
 */
val `ns-esc-double-quote` = `c-double-quote`

/**
 * `53` : ns-esc-slash:
 *   <[/][SOLIDUS][0x2f]>
 */
val `ns-esc-slash` = symbol('/')

/**
 * `54` : ns-esc-backslash:
 *   ->c-escape
 */
val `ns-esc-backslash` = `c-escape`

/**
 * `55` : ns-esc-next-line:
 *   <[N][LATIN CAPITAL LETTER N][0x4e]>
 */
val `ns-esc-next-line` = symbol('N')

/**
 * `56` : ns-esc-non-breaking-space:
 *   <[_][LOW LINE][0x5f]>
 */
val `ns-esc-non-breaking-space` = symbol('_')

/**
 * `57` : ns-esc-line-separator:
 *   <[L][LATIN CAPITAL LETTER L][0x4c]>
 */
val `ns-esc-line-separator` = symbol('L')

/**
 * `58` : ns-esc-paragraph-separator:
 *   <[P][LATIN CAPITAL LETTER P][0x50]>
 */
val `ns-esc-paragraph-separator` = symbol('P')

/**
 * `59` : ns-esc-8-bit:
 *   <[x][LATIN SMALL LETTER X][0x78]> + (->ns-hex-digit × 2)
 */
val `ns-esc-8-bit` = symbol('x')

/**
 * `60` : ns-esc-16-bit:
 *   <[u][LATIN SMALL LETTER U][0x75]> + (->ns-hex-digit × 4)
 */
val `ns-esc-16-bit` = symbol('u')

/**
 * `61` : ns-esc-32-bit:
 *   <[U][LATIN CAPITAL LETTER U][0x55]> + (->ns-hex-digit × 8)
 */
val `ns-esc-32-bit` = symbol('U')

/**
 * `62` : c-ns-esc-char:
 *   ->c-escape + [->ns-esc-null ||
 *    ->ns-esc-bell ||
 *    ->ns-esc-backspace ||
 *    ->ns-esc-horizontal-tab ||
 *    ->ns-esc-line-feed ||
 *    ->ns-esc-vertical-tab ||
 *    ->ns-esc-form-feed ||
 *    ->ns-esc-carriage-return ||
 *    ->ns-esc-escape ||
 *    ->ns-esc-space ||
 *    ->ns-esc-double-quote ||
 *    ->ns-esc-slash ||
 *    ->ns-esc-backslash ||
 *    ->ns-esc-next-line ||
 *    ->ns-esc-non-breaking-space ||
 *    ->ns-esc-line-separator ||
 *    ->ns-esc-paragraph-separator ||
 *    ->ns-esc-8-bit ||
 *    ->ns-esc-16-bit ||
 *    ->ns-esc-32-bit]
 */
val `c-ns-esc-char` = undefined

/**
 * `63` : s-indent (n):
 *   (->s-space × n)
 */
val `s-indent(n)` = undefined

/**
 * `64` : s-indent (<n):
 *   (->s-space × m)
 */
val `s-indent(«n)` = undefined

/**
 * `65` : s-indent (≤n):
 *   (->s-space × m)
 */
val `s-indent(≤n)` = undefined

/**
 * `66` : s-separate-in-line:
 *   (->s-white × +)
 */
val `s-separate-in-line` = undefined

/**
 * `67` : s-line-prefix (n,c):
 *   <c = block-out> ⇒ <->s-block-line-prefix(n)>
 *   <c = block-in> ⇒ <->s-block-line-prefix(n)>
 *   <c = flow-out> ⇒ <->s-flow-line-prefix(n)>
 *   <c = flow-in> ⇒ <->s-flow-line-prefix(n)>
 */
val `s-line-prefix(n,c)` = undefined

/**
 * `68` : s-block-line-prefix (n):
 *   ->s-indent(n)
 */
val `s-block-line-prefix(n)` = `s-indent(n)`

/**
 * `69` : s-flow-line-prefix (n):
 *   ->s-indent(n) + (->s-separate-in-line × ?)
 */
val `s-flow-line-prefix(n)` = undefined

/**
 * `70` : l-empty (n,c):
 *   [->s-line-prefix(n,c) ||
 *    ->s-indent(n)] + ->b-as-line-feed
 */
val `l-empty(n,c)` = undefined

/**
 * `71` : b-l-trimmed (n,c):
 *   ->b-non-content + (->l-empty(n,c) × +)
 */
val `b-l-trimmed(n,c)` = undefined

/**
 * `72` : b-as-space:
 *   ->b-break
 */
val `b-as-space` = `b-break`

/**
 * `73` : b-l-folded (n,c):
 *   [->b-l-trimmed(n,c) ||
 *    ->b-as-space]
 */
val `b-l-folded(n,c)` = `b-l-trimmed(n,c)` or `b-as-space`

/**
 * `74` : s-flow-folded (n):
 *   (->s-separate-in-line × ?) + ->b-l-folded(n,c) + ->s-flow-line-prefix(n)
 */
val `s-flow-folded(n)` = undefined

/**
 * `75` : c-nb-comment-text:
 *   ->c-comment + (->nb-char × *)
 */
val `c-nb-comment-text` = undefined

/**
 * `76` : b-comment:
 *   ->b-non-content
 */
val `b-comment` = `b-non-content`

/**
 * `77` : s-b-comment:
 *   (->s-separate-in-line + (->c-nb-comment-text × ?) × ?) + ->b-comment
 */
val `s-b-comment` = undefined

/**
 * `78` : l-comment:
 *   ->s-separate-in-line + (->c-nb-comment-text × ?) + ->b-comment
 */
val `l-comment` = undefined

/**
 * `79` : s-l-comments:
 *   ->s-b-comment + (->l-comment × *)
 */
val `s-l-comments` = undefined

/**
 * `80` : s-separate (n,c):
 *   <c = block-out> ⇒ <->s-separate-lines(n)>
 *   <c = block-in> ⇒ <->s-separate-lines(n)>
 *   <c = flow-out> ⇒ <->s-separate-lines(n)>
 *   <c = flow-in> ⇒ <->s-separate-lines(n)>
 *   <c = block-key> ⇒ <->s-separate-in-line>
 *   <c = flow-key> ⇒ <->s-separate-in-line>
 */
val `s-separate(n,c)` = undefined

/**
 * `81` : s-separate-lines (n):
 *   [->s-l-comments + ->s-flow-line-prefix(n) ||
 *    ->s-separate-in-line]
 */
val `s-separate-lines(n)` = undefined

/**
 * `82` : l-directive:
 *   ->c-directive + [->ns-yaml-directive ||
 *    ->ns-tag-directive ||
 *    ->ns-reserved-directive] + ->s-l-comments
 */
val `l-directive` = undefined

/**
 * `83` : ns-reserved-directive:
 *   ->ns-directive-name + (->s-separate-in-line + ->ns-directive-parameter × *)
 */
val `ns-reserved-directive` = undefined

/**
 * `84` : ns-directive-name:
 *   (->ns-char × +)
 */
val `ns-directive-name` = undefined

/**
 * `85` : ns-directive-parameter:
 *   (->ns-char × +)
 */
val `ns-directive-parameter` = undefined

/**
 * `86` : ns-yaml-directive:
 *   <[Y][LATIN CAPITAL LETTER Y][0x59]> + <[A][LATIN CAPITAL LETTER A][0x41]> + <[M][LATIN CAPITAL LETTER M][0x4d]> + <[L][LATIN CAPITAL LETTER L][0x4c]> + ->s-separate-in-line + ->ns-yaml-version
 */
val `ns-yaml-directive` = symbol('Y') + 'A' + 'M' + 'L'

/**
 * `87` : ns-yaml-version:
 *   (->ns-dec-digit × +) + <[.][FULL STOP][0x2e]> + (->ns-dec-digit × +)
 */
val `ns-yaml-version` = undefined

/**
 * `88` : ns-tag-directive:
 *   <[T][LATIN CAPITAL LETTER T][0x54]> + <[A][LATIN CAPITAL LETTER A][0x41]> + <[G][LATIN CAPITAL LETTER G][0x47]> + ->s-separate-in-line + ->c-tag-handle + ->s-separate-in-line + ->ns-tag-prefix
 */
val `ns-tag-directive` = symbol('T') + 'A' + 'G'

/**
 * `89` : c-tag-handle:
 *   [->c-named-tag-handle ||
 *    ->c-secondary-tag-handle ||
 *    ->c-primary-tag-handle]
 */
val `c-tag-handle` = undefined

/**
 * `90` : c-primary-tag-handle:
 *   ->c-tag
 */
val `c-primary-tag-handle` = `c-tag`

/**
 * `91` : c-secondary-tag-handle:
 *   ->c-tag + ->c-tag
 */
val `c-secondary-tag-handle` = undefined

/**
 * `92` : c-named-tag-handle:
 *   ->c-tag + (->ns-word-char × +) + ->c-tag
 */
val `c-named-tag-handle` = undefined

/**
 * `93` : ns-tag-prefix:
 *   [->c-ns-local-tag-prefix ||
 *    ->ns-global-tag-prefix]
 */
val `ns-tag-prefix` = undefined

/**
 * `94` : c-ns-local-tag-prefix:
 *   ->c-tag + (->ns-uri-char × *)
 */
val `c-ns-local-tag-prefix` = undefined

/**
 * `95` : ns-global-tag-prefix:
 *   ->ns-tag-char + (->ns-uri-char × *)
 */
val `ns-global-tag-prefix` = undefined

/**
 * `96` : c-ns-properties (n,c):
 *   [->c-ns-tag-property + (->s-separate(n,c) + ->c-ns-anchor-property × ?) ||
 *    ->c-ns-anchor-property + (->s-separate(n,c) + ->c-ns-tag-property × ?)]
 */
val `c-ns-properties(n,c)` = undefined

/**
 * `97` : c-ns-tag-property:
 *   [->c-verbatim-tag ||
 *    ->c-ns-shorthand-tag ||
 *    ->c-non-specific-tag]
 */
val `c-ns-tag-property` = undefined

/**
 * `98` : c-verbatim-tag:
 *   ->c-tag + <[<][LESS-THAN SIGN][0x3c]> + (->ns-uri-char × +) + <[>][GREATER-THAN SIGN][0x3e]>
 */
val `c-verbatim-tag` = undefined

/**
 * `99` : c-ns-shorthand-tag:
 *   ->c-tag-handle + (->ns-tag-char × +)
 */
val `c-ns-shorthand-tag` = undefined

/**
 * `100` : c-non-specific-tag:
 *   ->c-tag
 */
val `c-non-specific-tag` = `c-tag`

/**
 * `101` : c-ns-anchor-property:
 *   ->c-anchor + ->ns-anchor-name
 */
val `c-ns-anchor-property` = undefined

/**
 * `102` : ns-anchor-char:
 *   ->ns-char - ->c-flow-indicator
 */
val `ns-anchor-char` = undefined

/**
 * `103` : ns-anchor-name:
 *   (->ns-anchor-char × +)
 */
val `ns-anchor-name` = undefined

/**
 * `104` : c-ns-alias-node:
 *   ->c-alias + ->ns-anchor-name
 */
val `c-ns-alias-node` = undefined

/**
 * `105` : e-scalar:
 *   <empty>
 */
val `e-scalar` = undefined

/**
 * `106` : e-node:
 *   ->e-scalar
 */
val `e-node` = `e-scalar`

/**
 * `107` : nb-double-char:
 *   [->c-ns-esc-char ||
 *    ->nb-json - ->c-escape - ->c-double-quote]
 */
val `nb-double-char` = `c-ns-esc-char` or `nb-json` or `c-escape` or `c-double-quote`

/**
 * `108` : ns-double-char:
 *   ->nb-double-char - ->s-white
 */
val `ns-double-char` = undefined

/**
 * `109` : c-double-quoted (n,c):
 *   ->c-double-quote + ->nb-double-text(n,c) + ->c-double-quote
 */
val `c-double-quoted(n,c)` = undefined

/**
 * `110` : nb-double-text (n,c):
 *   <c = flow-out> ⇒ <->nb-double-multi-line(n)>
 *   <c = flow-in> ⇒ <->nb-double-multi-line(n)>
 *   <c = block-key> ⇒ <->nb-double-one-line>
 *   <c = flow-key> ⇒ <->nb-double-one-line>
 */
val `nb-double-text(n,c)` = undefined

/**
 * `111` : nb-double-one-line:
 *   (->nb-double-char × *)
 */
val `nb-double-one-line` = undefined

/**
 * `112` : s-double-escaped (n):
 *   (->s-white × *) + ->c-escape + ->b-non-content + (->l-empty(n,c) × *) + ->s-flow-line-prefix(n)
 */
val `s-double-escaped(n)` = undefined

/**
 * `113` : s-double-break (n):
 *   [->s-double-escaped(n) ||
 *    ->s-flow-folded(n)]
 */
val `s-double-break(n)` = `s-double-escaped(n)` or `s-flow-folded(n)`

/**
 * `114` : nb-ns-double-in-line:
 *   ((->s-white × *) + ->ns-double-char × *)
 */
val `nb-ns-double-in-line` = undefined

/**
 * `115` : s-double-next-line (n):
 *   ->s-double-break(n) + (->ns-double-char + ->nb-ns-double-in-line + [->s-double-next-line(n) ||
 *    (->s-white × *)] × ?)
 */
val `s-double-next-line(n)` = undefined

/**
 * `116` : nb-double-multi-line (n):
 *   ->nb-ns-double-in-line + [->s-double-next-line(n) ||
 *    (->s-white × *)]
 */
val `nb-double-multi-line(n)` = undefined

/**
 * `117` : c-quoted-quote:
 *   ->c-single-quote + ->c-single-quote
 */
val `c-quoted-quote` = undefined

/**
 * `118` : nb-single-char:
 *   [->c-quoted-quote ||
 *    ->nb-json - ->c-single-quote]
 */
val `nb-single-char` = `c-quoted-quote` or `nb-json` or `c-single-quote`

/**
 * `119` : ns-single-char:
 *   ->nb-single-char - ->s-white
 */
val `ns-single-char` = undefined

/**
 * `120` : c-single-quoted (n,c):
 *   ->c-single-quote + ->nb-single-text(n,c) + ->c-single-quote
 */
val `c-single-quoted(n,c)` = undefined

/**
 * `121` : nb-single-text (n,c):
 *   <c = flow-out> ⇒ <->nb-single-multi-line(n)>
 *   <c = flow-in> ⇒ <->nb-single-multi-line(n)>
 *   <c = block-key> ⇒ <->nb-single-one-line>
 *   <c = flow-key> ⇒ <->nb-single-one-line>
 */
val `nb-single-text(n,c)` = undefined

/**
 * `122` : nb-single-one-line:
 *   (->nb-single-char × *)
 */
val `nb-single-one-line` = undefined

/**
 * `123` : nb-ns-single-in-line:
 *   ((->s-white × *) + ->ns-single-char × *)
 */
val `nb-ns-single-in-line` = undefined

/**
 * `124` : s-single-next-line (n):
 *   ->s-flow-folded(n) + (->ns-single-char + ->nb-ns-single-in-line + [->s-single-next-line(n) ||
 *    (->s-white × *)] × ?)
 */
val `s-single-next-line(n)` = undefined

/**
 * `125` : nb-single-multi-line (n):
 *   ->nb-ns-single-in-line + [->s-single-next-line(n) ||
 *    (->s-white × *)]
 */
val `nb-single-multi-line(n)` = undefined

/**
 * `126` : ns-plain-first (c):
 *   [->ns-char - ->c-indicator ||
 *    ->c-mapping-key ||
 *    ->c-mapping-value ||
 *    ->c-sequence-entry]
 */
val `ns-plain-first(c)` = undefined

/**
 * `127` : ns-plain-safe (c):
 *   <c = flow-out> ⇒ <->ns-plain-safe-out>
 *   <c = flow-in> ⇒ <->ns-plain-safe-in>
 *   <c = block-key> ⇒ <->ns-plain-safe-out>
 *   <c = flow-key> ⇒ <->ns-plain-safe-in>
 */
val `ns-plain-safe(c)` = undefined

/**
 * `128` : ns-plain-safe-out:
 *   ->ns-char
 */
val `ns-plain-safe-out` = `ns-char`

/**
 * `129` : ns-plain-safe-in:
 *   ->ns-char - ->c-flow-indicator
 */
val `ns-plain-safe-in` = undefined

/**
 * `130` : ns-plain-char (c):
 *   ->ns-plain-safe(c) - [->c-mapping-value - ->c-comment ||
 *    ->c-comment ||
 *    ->c-mapping-value]
 */
val `ns-plain-char(c)` = undefined

/**
 * `131` : ns-plain (n,c):
 *   <c = flow-out> ⇒ <->ns-plain-multi-line(n,c)>
 *   <c = flow-in> ⇒ <->ns-plain-multi-line(n,c)>
 *   <c = block-key> ⇒ <->ns-plain-one-line(c)>
 *   <c = flow-key> ⇒ <->ns-plain-one-line(c)>
 */
val `ns-plain(n,c)` = undefined

/**
 * `132` : nb-ns-plain-in-line (c):
 *   ((->s-white × *) + ->ns-plain-char(c) × *)
 */
val `nb-ns-plain-in-line(c)` = undefined

/**
 * `133` : ns-plain-one-line (c):
 *   ->ns-plain-first(c) + ->nb-ns-plain-in-line(c)
 */
val `ns-plain-one-line(c)` = undefined

/**
 * `134` : s-ns-plain-next-line (n,c):
 *   ->s-flow-folded(n) + ->ns-plain-char(c) + ->nb-ns-plain-in-line(c)
 */
val `s-ns-plain-next-line(n,c)` = undefined

/**
 * `135` : ns-plain-multi-line (n,c):
 *   ->ns-plain-one-line(c) + (->s-ns-plain-next-line(n,c) × *)
 */
val `ns-plain-multi-line(n,c)` = undefined

/**
 * `136` : in-flow (c):
 *   <c = flow-out> ⇒ <flow-in>
 *   <c = flow-in> ⇒ <flow-in>
 *   <c = block-key> ⇒ <flow-key>
 *   <c = flow-key> ⇒ <flow-key>
 */
val `in-flow(c)` = undefined

/**
 * `137` : c-flow-sequence (n,c):
 *   ->c-sequence-start + (->s-separate(n,c) × ?) + ->ns-s-flow-seq-entries(n,c) + ->in-flow(c) + (->ns-s-flow-seq-entries(n,c) × ?) + ->c-sequence-end
 */
val `c-flow-sequence(n,c)` = undefined

/**
 * `138` : ns-s-flow-seq-entries (n,c):
 *   ->ns-flow-seq-entry(n,c) + (->s-separate(n,c) × ?) + (->c-collect-entry + (->s-separate(n,c) × ?) + (->ns-s-flow-seq-entries(n,c) × ?) × ?)
 */
val `ns-s-flow-seq-entries(n,c)` = undefined

/**
 * `139` : ns-flow-seq-entry (n,c):
 *   [->ns-flow-pair(n,c) ||
 *    ->ns-flow-node(n,c)]
 */
val `ns-flow-seq-entry(n,c)` = undefined

/**
 * `140` : c-flow-mapping (n,c):
 *   ->c-mapping-start + (->s-separate(n,c) × ?) + ->ns-s-flow-map-entries(n,c) + ->in-flow(c) + (->ns-s-flow-map-entries(n,c) × ?) + ->c-mapping-end
 */
val `c-flow-mapping(n,c)` = undefined

/**
 * `141` : ns-s-flow-map-entries (n,c):
 *   ->ns-flow-map-entry(n,c) + (->s-separate(n,c) × ?) + (->c-collect-entry + (->s-separate(n,c) × ?) + (->ns-s-flow-map-entries(n,c) × ?) × ?)
 */
val `ns-s-flow-map-entries(n,c)` = undefined

/**
 * `142` : ns-flow-map-entry (n,c):
 *   [->c-mapping-key + ->s-separate(n,c) + ->ns-flow-map-explicit-entry(n,c) ||
 *    ->ns-flow-map-implicit-entry(n,c)]
 */
val `ns-flow-map-entry(n,c)` = undefined

/**
 * `143` : ns-flow-map-explicit-entry (n,c):
 *   [->ns-flow-map-implicit-entry(n,c) ||
 *    ->e-node + ->e-node]
 */
val `ns-flow-map-explicit-entry(n,c)` = undefined

/**
 * `144` : ns-flow-map-implicit-entry (n,c):
 *   [->ns-flow-map-yaml-key-entry(n,c) ||
 *    ->c-ns-flow-map-empty-key-entry(n,c) ||
 *    ->c-ns-flow-map-json-key-entry(n,c)]
 */
val `ns-flow-map-implicit-entry(n,c)` = undefined

/**
 * `145` : ns-flow-map-yaml-key-entry (n,c):
 *   ->ns-flow-yaml-node(n,c) + [(->s-separate(n,c) × ?) + ->c-ns-flow-map-separate-value(n,c) ||
 *    ->e-node]
 */
val `ns-flow-map-yaml-key-entry(n,c)` = undefined

/**
 * `146` : c-ns-flow-map-empty-key-entry (n,c):
 *   ->e-node + ->c-ns-flow-map-separate-value(n,c)
 */
val `c-ns-flow-map-empty-key-entry(n,c)` = undefined

/**
 * `147` : c-ns-flow-map-separate-value (n,c):
 *   ->c-mapping-value + [->s-separate(n,c) + ->ns-flow-node(n,c) ||
 *    ->e-node]
 */
val `c-ns-flow-map-separate-value(n,c)` = undefined

/**
 * `148` : c-ns-flow-map-json-key-entry (n,c):
 *   ->c-flow-json-node(n,c) + [(->s-separate(n,c) × ?) + ->c-ns-flow-map-adjacent-value(n,c) ||
 *    ->e-node]
 */
val `c-ns-flow-map-json-key-entry(n,c)` = undefined

/**
 * `149` : c-ns-flow-map-adjacent-value (n,c):
 *   ->c-mapping-value + [(->s-separate(n,c) × ?) + ->ns-flow-node(n,c) ||
 *    ->e-node]
 */
val `c-ns-flow-map-adjacent-value(n,c)` = undefined

/**
 * `150` : ns-flow-pair (n,c):
 *   [->c-mapping-key + ->s-separate(n,c) + ->ns-flow-map-explicit-entry(n,c) ||
 *    ->ns-flow-pair-entry(n,c)]
 */
val `ns-flow-pair(n,c)` = undefined

/**
 * `151` : ns-flow-pair-entry (n,c):
 *   [->ns-flow-pair-yaml-key-entry(n,c) ||
 *    ->c-ns-flow-map-empty-key-entry(n,c) ||
 *    ->c-ns-flow-pair-json-key-entry(n,c)]
 */
val `ns-flow-pair-entry(n,c)` = undefined

/**
 * `152` : ns-flow-pair-yaml-key-entry (n,c):
 *   ->ns-s-implicit-yaml-key(c) + ->c-ns-flow-map-separate-value(n,c)
 */
val `ns-flow-pair-yaml-key-entry(n,c)` = undefined

/**
 * `153` : c-ns-flow-pair-json-key-entry (n,c):
 *   ->c-s-implicit-json-key(c) + ->c-ns-flow-map-adjacent-value(n,c)
 */
val `c-ns-flow-pair-json-key-entry(n,c)` = undefined

/**
 * `154` : ns-s-implicit-yaml-key (c):
 *   ->ns-flow-yaml-node(n,c) + (->s-separate-in-line × ?)
 */
val `ns-s-implicit-yaml-key(c)` = undefined

/**
 * `155` : c-s-implicit-json-key (c):
 *   ->c-flow-json-node(n,c) + (->s-separate-in-line × ?)
 */
val `c-s-implicit-json-key(c)` = undefined

/**
 * `156` : ns-flow-yaml-content (n,c):
 *   ->ns-plain(n,c)
 */
val `ns-flow-yaml-content(n,c)` = `ns-plain(n,c)`

/**
 * `157` : c-flow-json-content (n,c):
 *   [->c-flow-sequence(n,c) ||
 *    ->c-flow-mapping(n,c) ||
 *    ->c-single-quoted(n,c) ||
 *    ->c-double-quoted(n,c)]
 */
val `c-flow-json-content(n,c)` = `c-flow-sequence(n,c)` or `c-flow-mapping(n,c)` or `c-single-quoted(n,c)` or `c-double-quoted(n,c)`

/**
 * `158` : ns-flow-content (n,c):
 *   [->ns-flow-yaml-content(n,c) ||
 *    ->c-flow-json-content(n,c)]
 */
val `ns-flow-content(n,c)` = `ns-flow-yaml-content(n,c)` or `c-flow-json-content(n,c)`

/**
 * `159` : ns-flow-yaml-node (n,c):
 *   [->c-ns-alias-node ||
 *    ->ns-flow-yaml-content(n,c) ||
 *    ->c-ns-properties(n,c) + [->s-separate(n,c) + ->ns-flow-yaml-content(n,c) ||
 *    ->e-scalar]]
 */
val `ns-flow-yaml-node(n,c)` = `c-ns-alias-node` or `ns-flow-yaml-content(n,c)` or `c-ns-properties(n,c)` or `s-separate(n,c)` or `ns-flow-yaml-content(n,c)` or `e-scalar`

/**
 * `160` : c-flow-json-node (n,c):
 *   (->c-ns-properties(n,c) + ->s-separate(n,c) × ?) + ->c-flow-json-content(n,c)
 */
val `c-flow-json-node(n,c)` = undefined

/**
 * `161` : ns-flow-node (n,c):
 *   [->c-ns-alias-node ||
 *    ->ns-flow-content(n,c) ||
 *    ->c-ns-properties(n,c) + [->s-separate(n,c) + ->ns-flow-content(n,c) ||
 *    ->e-scalar]]
 */
val `ns-flow-node(n,c)` = `c-ns-alias-node` or `ns-flow-content(n,c)` or `c-ns-properties(n,c)` or `s-separate(n,c)` or `ns-flow-content(n,c)` or `e-scalar`

/**
 * `162` : c-b-block-header (m,t):
 *   [->c-indentation-indicator(m) + ->c-chomping-indicator(t) ||
 *    ->c-chomping-indicator(t) + ->c-indentation-indicator(m)] + ->s-b-comment
 */
val `c-b-block-header(m,t)` = undefined

/**
 * `163` : c-indentation-indicator (m):
 *   ->ns-dec-digit ⇒ <m =->ns-dec-digit - #x30>
 *   <> ⇒ <m = auto-detect()>
 */
val `c-indentation-indicator(m)` = undefined

/**
 * `164` : c-chomping-indicator (t):
 *   <[-][HYPHEN-MINUS][0x2d]> ⇒ <t = strip>
 *   <<[+][PLUS SIGN][0x2b]>> ⇒ <t = keep>
 *   <> ⇒ <t = clip>
 */
val `c-chomping-indicator(t)` = undefined

/**
 * `165` : b-chomped-last (t):
 *   <t = strip> ⇒ <->b-non-content | /* End of file */>
 *   <t = clip> ⇒ <->b-as-line-feed | /* End of file */>
 *   <t = keep> ⇒ <->b-as-line-feed | /* End of file */>
 */
val `b-chomped-last(t)` = undefined

/**
 * `166` : l-chomped-empty (n,t):
 *   <t = strip> ⇒ <->l-strip-empty(n)>
 *   <t = clip> ⇒ <->l-strip-empty(n)>
 *   <t = keep> ⇒ <->l-keep-empty(n)>
 */
val `l-chomped-empty(n,t)` = undefined

/**
 * `167` : l-strip-empty (n):
 *   (->s-indent(n) + ->b-non-content × *) + (->l-trail-comments(n) × ?)
 */
val `l-strip-empty(n)` = undefined

/**
 * `168` : l-keep-empty (n):
 *   (->l-empty(n,c) × *) + (->l-trail-comments(n) × ?)
 */
val `l-keep-empty(n)` = undefined

/**
 * `169` : l-trail-comments (n):
 *   ->s-indent(n) + ->c-nb-comment-text + ->b-comment + (->l-comment × *)
 */
val `l-trail-comments(n)` = undefined

/**
 * `170` : c-l+literal (n):
 *   ->c-literal + ->c-b-block-header(m,t) + ->l-literal-content(n,t)
 */
val `c-l+literal(n)` = undefined

/**
 * `171` : l-nb-literal-text (n):
 *   (->l-empty(n,c) × *) + ->s-indent(n) + (->nb-char × +)
 */
val `l-nb-literal-text(n)` = undefined

/**
 * `172` : b-nb-literal-next (n):
 *   ->b-as-line-feed + ->l-nb-literal-text(n)
 */
val `b-nb-literal-next(n)` = undefined

/**
 * `173` : l-literal-content (n,t):
 *   (->l-nb-literal-text(n) + (->b-nb-literal-next(n) × *) + ->b-chomped-last(t) × ?) + ->l-chomped-empty(n,t)
 */
val `l-literal-content(n,t)` = undefined

/**
 * `174` : c-l+folded (n):
 *   ->c-folded + ->c-b-block-header(m,t) + ->l-folded-content(n,t)
 */
val `c-l+folded(n)` = undefined

/**
 * `175` : s-nb-folded-text (n):
 *   ->s-indent(n) + ->ns-char + (->nb-char × *)
 */
val `s-nb-folded-text(n)` = undefined

/**
 * `176` : l-nb-folded-lines (n):
 *   ->s-nb-folded-text(n) + (->b-l-folded(n,c) + ->s-nb-folded-text(n) × *)
 */
val `l-nb-folded-lines(n)` = undefined

/**
 * `177` : s-nb-spaced-text (n):
 *   ->s-indent(n) + ->s-white + (->nb-char × *)
 */
val `s-nb-spaced-text(n)` = undefined

/**
 * `178` : b-l-spaced (n):
 *   ->b-as-line-feed + (->l-empty(n,c) × *)
 */
val `b-l-spaced(n)` = undefined

/**
 * `179` : l-nb-spaced-lines (n):
 *   ->s-nb-spaced-text(n) + (->b-l-spaced(n) + ->s-nb-spaced-text(n) × *)
 */
val `l-nb-spaced-lines(n)` = undefined

/**
 * `180` : l-nb-same-lines (n):
 *   (->l-empty(n,c) × *) + [->l-nb-folded-lines(n) ||
 *    ->l-nb-spaced-lines(n)]
 */
val `l-nb-same-lines(n)` = undefined

/**
 * `181` : l-nb-diff-lines (n):
 *   ->l-nb-same-lines(n) + (->b-as-line-feed + ->l-nb-same-lines(n) × *)
 */
val `l-nb-diff-lines(n)` = undefined

/**
 * `182` : l-folded-content (n,t):
 *   (->l-nb-diff-lines(n) + ->b-chomped-last(t) × ?) + ->l-chomped-empty(n,t)
 */
val `l-folded-content(n,t)` = undefined

/**
 * `183` : l+block-sequence (n):
 *   (->s-indent(n) + ->c-l-block-seq-entry(n) × +)
 */
val `l+block-sequence(n)` = undefined

/**
 * `184` : c-l-block-seq-entry (n):
 *   ->c-sequence-entry + ->s-l+block-indented(n,c)
 */
val `c-l-block-seq-entry(n)` = undefined

/**
 * `185` : s-l+block-indented (n,c):
 *   [->s-indent(n) + [->ns-l-compact-sequence(n) ||
 *    ->ns-l-compact-mapping(n)] ||
 *    ->s-l+block-node(n,c) ||
 *    ->e-node + ->s-l-comments]
 */
val `s-l+block-indented(n,c)` = undefined

/**
 * `186` : ns-l-compact-sequence (n):
 *   ->c-l-block-seq-entry(n) + (->s-indent(n) + ->c-l-block-seq-entry(n) × *)
 */
val `ns-l-compact-sequence(n)` = undefined

/**
 * `187` : l+block-mapping (n):
 *   (->s-indent(n) + ->ns-l-block-map-entry(n) × +)
 */
val `l+block-mapping(n)` = undefined

/**
 * `188` : ns-l-block-map-entry (n):
 *   [->c-l-block-map-explicit-entry(n) ||
 *    ->ns-l-block-map-implicit-entry(n)]
 */
val `ns-l-block-map-entry(n)` = undefined

/**
 * `189` : c-l-block-map-explicit-entry (n):
 *   ->c-l-block-map-explicit-key(n) + [->l-block-map-explicit-value(n) ||
 *    ->e-node]
 */
val `c-l-block-map-explicit-entry(n)` = undefined

/**
 * `190` : c-l-block-map-explicit-key (n):
 *   ->c-mapping-key + ->s-l+block-indented(n,c)
 */
val `c-l-block-map-explicit-key(n)` = undefined

/**
 * `191` : l-block-map-explicit-value (n):
 *   ->s-indent(n) + ->c-mapping-value + ->s-l+block-indented(n,c)
 */
val `l-block-map-explicit-value(n)` = undefined

/**
 * `192` : ns-l-block-map-implicit-entry (n):
 *   [->ns-s-block-map-implicit-key ||
 *    ->e-node] + ->c-l-block-map-implicit-value(n)
 */
val `ns-l-block-map-implicit-entry(n)` = undefined

/**
 * `193` : ns-s-block-map-implicit-key:
 *   [->c-s-implicit-json-key(c) ||
 *    ->ns-s-implicit-yaml-key(c)]
 */
val `ns-s-block-map-implicit-key` = `c-s-implicit-json-key(c)` or `ns-s-implicit-yaml-key(c)`

/**
 * `194` : c-l-block-map-implicit-value (n):
 *   ->c-mapping-value + [->s-l+block-node(n,c) ||
 *    ->e-node + ->s-l-comments]
 */
val `c-l-block-map-implicit-value(n)` = undefined

/**
 * `195` : ns-l-compact-mapping (n):
 *   ->ns-l-block-map-entry(n) + (->s-indent(n) + ->ns-l-block-map-entry(n) × *)
 */
val `ns-l-compact-mapping(n)` = undefined

/**
 * `196` : s-l+block-node (n,c):
 *   [->s-l+block-in-block(n,c) ||
 *    ->s-l+flow-in-block(n)]
 */
val `s-l+block-node(n,c)` = undefined

/**
 * `197` : s-l+flow-in-block (n):
 *   ->s-separate(n,c) + ->ns-flow-node(n,c) + ->s-l-comments
 */
val `s-l+flow-in-block(n)` = undefined

/**
 * `198` : s-l+block-in-block (n,c):
 *   [->s-l+block-scalar(n,c) ||
 *    ->s-l+block-collection(n,c)]
 */
val `s-l+block-in-block(n,c)` = undefined

/**
 * `199` : s-l+block-scalar (n,c):
 *   ->s-separate(n,c) + (->c-ns-properties(n,c) + ->s-separate(n,c) × ?) + [->c-l+literal(n) ||
 *    ->c-l+folded(n)]
 */
val `s-l+block-scalar(n,c)` = undefined

/**
 * `200` : s-l+block-collection (n,c):
 *   (->s-separate(n,c) + ->c-ns-properties(n,c) × ?) + ->s-l-comments + ->l+block-sequence(n) + ->seq-spaces(n,c) + [->l+block-sequence(n) ||
 *    ->l+block-mapping(n)]
 */
val `s-l+block-collection(n,c)` = undefined

/**
 * `201` : seq-spaces (n,c):
 *   <c = block-out> ⇒ <n-1>
 *   <c = block-in> ⇒ <n>
 */
val `seq-spaces(n,c)` = undefined

/**
 * `202` : l-document-prefix:
 *   (->c-byte-order-mark × ?) + (->l-comment × *)
 */
val `l-document-prefix` = undefined

/**
 * `203` : c-directives-end:
 *   <[-][HYPHEN-MINUS][0x2d]> + <[-][HYPHEN-MINUS][0x2d]> + <[-][HYPHEN-MINUS][0x2d]>
 */
val `c-directives-end` = symbol('-') + '-' + '-'

/**
 * `204` : c-document-end:
 *   <[.][FULL STOP][0x2e]> + <[.][FULL STOP][0x2e]> + <[.][FULL STOP][0x2e]>
 */
val `c-document-end` = symbol('.') + '.' + '.'

/**
 * `205` : l-document-suffix:
 *   ->c-document-end + ->s-l-comments
 */
val `l-document-suffix` = undefined

/**
 * `206` : c-forbidden:
 *   [->c-directives-end ||
 *    ->c-document-end] + [->b-char ||
 *    ->s-white]
 */
val `c-forbidden` = undefined

/**
 * `207` : l-bare-document:
 *   ->s-l+block-node(n,c)
 */
val `l-bare-document` = `s-l+block-node(n,c)`

/**
 * `208` : l-explicit-document:
 *   ->c-directives-end + [->l-bare-document ||
 *    ->e-node + ->s-l-comments]
 */
val `l-explicit-document` = undefined

/**
 * `209` : l-directive-document:
 *   (->l-directive × +) + ->l-explicit-document
 */
val `l-directive-document` = undefined

/**
 * `210` : l-any-document:
 *   [->l-directive-document ||
 *    ->l-explicit-document ||
 *    ->l-bare-document]
 */
val `l-any-document` = `l-directive-document` or `l-explicit-document` or `l-bare-document`

/**
 * `211` : l-yaml-stream:
 *   (->l-document-prefix × *) + (->l-any-document × ?) + ((->l-document-suffix × +) + (->l-document-prefix × *) + [(->l-any-document × ?) ||
 *    (->l-document-prefix × *) + (->l-explicit-document × ?)] × *)
 */
val `l-yaml-stream` = undefined

private infix fun Char.or(that: Char) = symbol(this) or symbol(that)
private infix fun Char.or(that: Token) = symbol(this) or that
private infix fun Token.or(that: String): Token = or(symbol(that))
private infix fun Token.or(that: Char): Token = or(symbol(that))
private infix operator fun Char.rangeTo(that: Char) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun Char.rangeTo(that: String) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun String.rangeTo(that: String) = symbol(CodePoint.of(this)..CodePoint.of(that))
private infix operator fun Char.plus(that: Char) = symbol(this) + symbol(that)
private infix operator fun Token.plus(that: Char) = this + symbol(that)
private infix fun Token.or(range: CharRange) = this.or(symbol(range.toCodePointRange()))
private fun token(range: CharRange) = symbol(range.toCodePointRange())
