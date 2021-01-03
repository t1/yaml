package spec.generator

import com.github.t1.yaml.parser.ChompMode.clip
import com.github.t1.yaml.parser.ChompMode.keep
import com.github.t1.yaml.parser.ChompMode.strip
import com.github.t1.yaml.parser.`c-b-block-header`
import com.github.t1.yaml.parser.`c-chomping-indicator`
import com.github.t1.yaml.parser.`c-indentation-indicator`
import com.github.t1.yaml.parser.`c-nb-comment-text`
import com.github.t1.yaml.parser.`l-document-prefix`
import com.github.t1.yaml.parser.`nb-char`
import com.github.t1.yaml.parser.`ns-directive-name`
import com.github.t1.yaml.parser.`ns-esc-16-bit`
import com.github.t1.yaml.parser.`ns-esc-32-bit`
import com.github.t1.yaml.parser.`ns-esc-8-bit`
import com.github.t1.yaml.parser.`s-block-line-prefix`
import com.github.t1.yaml.parser.`s-double-next-line`
import com.github.t1.yaml.parser.`s-indent`
import com.github.t1.yaml.parser.`s-indent≤`
import com.github.t1.yaml.parser.`s-indent≪`
import com.github.t1.yaml.parser.`s-separate-in-line`
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters") class YamlTokensTest : TokenTestTools() {
    @Test fun `s-indent`() {
        token = `s-indent`(0)
        "" doesnt match
        " " matches " "
        "  " matches " "
        "   " matches " "

        token = `s-indent`(1)
        "" doesnt match
        " " matches " "
        "  " matches " "
        "   " matches " "

        token = `s-indent`(2)
        "" doesnt match
        " " doesnt match
        "  " matches "  "
        "   " matches "  "

        token = `s-indent`(3)
        "" doesnt match
        " " doesnt match
        "  " doesnt match
        "   " matches "   "

        token = `s-indent`(4)
        "" doesnt match
        " " doesnt match
        "  " doesnt match
        "   " doesnt match
    }

    @Test fun `s-indent≪`() {
        token = `s-indent≪`(0)
        "" doesnt match
        " " doesnt match
        "  " doesnt match
        "   " doesnt match

        token = `s-indent≪`(1)
        "" matches ""
        " " doesnt match
        "  " doesnt match
        "   " doesnt match

        token = `s-indent≪`(2)
        "" matches ""
        " " matches " "
        "  " doesnt match
        "   " doesnt match

        token = `s-indent≪`(3)
        "" matches ""
        " " matches " "
        "  " matches "  "
        "   " doesnt match

        token = `s-indent≪`(4)
        "" matches ""
        " " matches " "
        "  " matches "  "
        "   " matches "   "
    }

    @Test fun `s-indent≤`() {
        token = `s-indent≤`(0)
        "" matches ""
        " " doesnt match
        "  " doesnt match
        "   " doesnt match

        token = `s-indent≤`(1)
        "" matches ""
        " " matches " "
        "  " doesnt match
        "   " doesnt match

        token = `s-indent≤`(2)
        "" matches ""
        " " matches " "
        "  " matches "  "
        "   " doesnt match

        token = `s-indent≤`(3)
        "" matches ""
        " " matches " "
        "  " matches "  "
        "   " matches "   "

        token = `s-indent≤`(4)
        "" matches ""
        " " matches " "
        "  " matches "  "
        "   " matches "   "
    }

    @Test fun `ns-esc-8-bit`() {
        token = `ns-esc-8-bit`

        "x02" matches "x02"
        "x02x" matches "x02"
        "x020" matches "x02"
        "0x02" doesnt match
        "y02" doesnt match
        "x0" doesnt match
        "x2" doesnt match
    }

    @Test fun `ns-esc-16-bit`() {
        token = `ns-esc-16-bit`

        "x02" doesnt match
        "U01234567" doesnt match
        "u02" doesnt match
        "u02x" doesnt match
        "u020" doesnt match
        "0u02" doesnt match
        "y02" doesnt match
        "u0" doesnt match
        "u2" doesnt match
        "u012" doesnt match
        "u234" doesnt match
        "u0123" matches "u0123"
        "u0123x" matches "u0123"
        "u01230" matches "u0123"
    }

    @Test fun `ns-esc-32-bit`() {
        token = `ns-esc-32-bit`

        "x02" doesnt match
        "u0123" doesnt match
        "u01234567" doesnt match
        "U02" doesnt match
        "U02x" doesnt match
        "U020" doesnt match
        "0U02" doesnt match
        "y02" doesnt match
        "U0" doesnt match
        "U2" doesnt match
        "U012" doesnt match
        "U234" doesnt match
        "U01234567" matches "U01234567"
        "U01234567x" matches "U01234567"
        "U012345670" matches "U01234567"
    }

    @Test fun `s-separate-in-line`() {
        token = `s-separate-in-line` // [(->s-white × +) | ->Start of line]

        "x y".afterRead('x') matches " "
        "x".afterRead('x') doesnt match
        "" matches ""
        "x" matches ""
        " " matches " "
        " x" matches " "
        "  " matches "  "
        "  x" matches "  "
        "   " matches "   "
        "   x" matches "   "
    }

    @Test fun `ns-directive-name`() {
        token = `ns-directive-name` // * (->ns-char × +)

        " x" doesnt match
        "x " matches "x"
        "xy " matches "xy"
    }

    @Test fun `c-nb-comment-text`() {
        token = `c-nb-comment-text` // ->c-comment + (->nb-char × *)

        "x#" doesnt match
        "#" matches "#"
        "#x" matches "#x"
        "#xy" matches "#xy"
        "#xyz" matches "#xyz"
        "#\uFEFF" matches "#" // BOM (0xFEFF) is not a nb-char
        "#x\uFEFF" matches "#x"
        "#xy\uFEFF" matches "#xy"
    }

    @Test fun `l-document-prefix`() {
        token = `l-document-prefix` // (->c-byte-order-mark × ?) + (->l-comment × *)

        "x" matches ""
        "x\uFEFF" matches ""
        "x#" matches ""
        "#x" matches "#x"
        "##x" matches "##x"
        "\uFEFF" matches "\uFEFF"
        "#\uFEFF" matches ""
        "\uFEFF#" matches "\uFEFF#"
        "\uFEFF##" matches "\uFEFF##"
    }

    @Test fun `nb-char`() {
        token = `nb-char` // `c-printable` - `b-char` - `c-byte-order-mark`

        "x" matches "x"
        "x#" matches "x"
        "\b" doesnt match
        "\n" doesnt match
        "\r" doesnt match
        "\uFEFF" doesnt match
    }

    @Test fun `s-block-line-prefix`() {
        token = `s-block-line-prefix`(0) // `s-indent`(n)
        "" doesnt match
        " " matches " "
        "  " matches " "
        "   " matches " "

        token = `s-block-line-prefix`(1) // `s-indent`(n)
        "" doesnt match
        " " matches " "
        "  " matches " "
        "   " matches " "

        token = `s-block-line-prefix`(2) // `s-indent`(n)
        "" doesnt match
        " " doesnt match
        "  " matches "  "
        "   " matches "  "

        token = `s-block-line-prefix`(3) // `s-indent`(n)
        "" doesnt match
        " " doesnt match
        "  " doesnt match
        "   " matches "   "

        token = `s-block-line-prefix`(4) // `s-indent`(n)
        "" doesnt match
        " " doesnt match
        "  " doesnt match
        "   " doesnt match
    }

    @Test fun `s-double-next-line(3)`() {
        token = `s-double-next-line`(3) // `s-double-break`(n) + (`ns-double-char` + `nb-ns-double-in-line` + `s-double-next-line`(n) or `s-white` * zero_or_more) * zero_or_once

        // primarily check that the direct recursion is callable
        "" doesnt match
    }

    @Test fun `c-b-block-header()`() {
        func = ::`c-b-block-header` // [->c-indentation-indicator(m) + ->c-chomping-indicator(t) | ->c-chomping-indicator(t) + ->c-indentation-indicator(m)] + ->s-b-comment

        "" matches (0 to clip) leaving ""
        "1" matches (1 to clip) leaving ""
        "x" matches (0 to clip) leaving "x"
        "-" matches (0 to strip) leaving ""
        "-x" matches (0 to strip) leaving "x"
        "1-" matches (1 to strip) leaving ""
        "1--" matches (1 to strip) leaving "-"
        "+" matches (0 to keep) leaving ""
        "1+" matches (1 to keep) leaving ""
        "1++" matches (1 to keep) leaving "+"
        "1+-" matches (1 to keep) leaving "-"
        "-0" matches (0 to strip) leaving ""
        "-0x" matches (0 to strip) leaving "x"
        "-1" matches (1 to strip) leaving ""
        "-1-" matches (1 to strip) leaving "-"
        "+1" matches (1 to keep) leaving ""
        "+1+" matches (1 to keep) leaving "+"
        "+1-" matches (1 to keep) leaving "-"
    }

    @Test fun `c-indentation-indicator()`() {
        func = ::`c-indentation-indicator`

        "" matches 0 leaving ""
        "x" matches 0 leaving "x"
        "0" matches 0 leaving ""
        "1" matches 1 leaving ""
        "9" matches 9 leaving ""
        "10" matches 1 leaving "0"
        "23" matches 2 leaving "3"
    }

    @Test fun `c-chomping-indicator()`() {
        func = ::`c-chomping-indicator`

        "" matches clip leaving ""
        "x" matches clip leaving "x"
        "xx" matches clip leaving "xx"
        "-" matches strip leaving ""
        "--" matches strip leaving "-"
        "-x" matches strip leaving "x"
        "+" matches keep leaving ""
        "++" matches keep leaving "+"
        "+x" matches keep leaving "x"
    }
}
