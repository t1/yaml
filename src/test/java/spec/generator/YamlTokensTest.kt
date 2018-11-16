package spec.generator

import com.github.t1.yaml.parser.`c-nb-comment-text`
import com.github.t1.yaml.parser.`l-document-prefix`
import com.github.t1.yaml.parser.`nb-char`
import com.github.t1.yaml.parser.`ns-directive-name`
import com.github.t1.yaml.parser.`ns-esc-16-bit`
import com.github.t1.yaml.parser.`ns-esc-32-bit`
import com.github.t1.yaml.parser.`ns-esc-8-bit`
import com.github.t1.yaml.parser.`s-indent`
import com.github.t1.yaml.parser.`s-indent≤`
import com.github.t1.yaml.parser.`s-indent≪`
import com.github.t1.yaml.parser.`s-separate-in-line`
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once
import com.github.t1.yaml.tools.symbol
import org.junit.jupiter.api.Disabled
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

    @Test fun `token with once_or_more or startOfLine`() {
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

    @Test fun `token with zero_or_more minus tokens`() {
        token = `ns-directive-name` // * (->ns-char × +)

        " x" doesnt match
        "x " matches "x"
        "xy " matches "xy"
    }

    @Test fun `token with comment and zero_or_more minus tokens`() {
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

    @Test fun `token with zero_or_once`() {
        token = symbol('x') * zero_or_once

        "y" matches ""
        "x" matches "x"
        "x#" matches "x"
        "#x" matches ""
    }

    @Disabled @Test fun `l-document-prefix`() {
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
}
