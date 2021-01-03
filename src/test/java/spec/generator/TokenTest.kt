package spec.generator

import com.github.t1.codepoint.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Match
import com.github.t1.yaml.tools.Token
import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once
import com.github.t1.yaml.tools.empty
import com.github.t1.yaml.tools.endOfFile
import com.github.t1.yaml.tools.startOfLine
import com.github.t1.yaml.tools.symbol
import com.github.t1.yaml.tools.token
import com.github.t1.yaml.tools.undefined
import com.github.t1.yaml.tools.whitespace
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class TokenTest : TokenTestTools() {
    @Test fun undefined() {
        token = undefined

        val throwable = Assertions.catchThrowable { undefined.match(CodePointReader("x")) }

        assertThat(throwable)
            .isInstanceOf(UnsupportedOperationException::class.java)
            .hasMessage("undefined token")
    }

    @Test fun `simple symbol`() {
        token = symbol("x")

        "x" matches "x" leaving ""
        "xx" matches "x" leaving "x"
        "y" doesnt match leaving "y"

        val match = token.match(CodePointReader("xx"))
        assertThat(match).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(("x"))))
        assertThat(match.size).isEqualTo(1)
    }

    @Test fun `two chars symbol`() {
        token = symbol("😀")

        "😀" matches "😀"
        "😀x" matches "😀" leaving "x"
        "x" doesnt match leaving "x"
        "🤪" doesnt match // goofy face
        "\uD83D\uDE00" matches "\uD83D\uDE00"
        "\uD83D\uDE00x" matches "\uD83D\uDE00"
        "\uD83D\uDE00" matches "😀"
        "\uD83D\uDE00x" matches "😀"
    }

    @Test fun `two chars codePoint symbol`() {
        val codePoint = CodePoint.of("😀")
        assertThat("😀").isEqualTo("\uD83D\uDE00")
        assertThat(codePoint.info).isEqualTo("[\\uD83D\\uDE00][GRINNING FACE][0x1f600]")
        assertThat(codePoint.toString()).isEqualTo("😀")
        assertThat(codePoint.toString()).isEqualTo("\uD83D\uDE00")
        token = symbol(codePoint)

        "😀" matches "😀"
        "😀x" matches "😀" leaving "x"
        "x" doesnt match leaving "x"
        "🤪" doesnt match // goofy face
        "\uD83D\uDE00" matches "\uD83D\uDE00"
        "\uD83D\uDE00x" matches "\uD83D\uDE00"
        "\uD83D\uDE00" matches "😀"
        "\uD83D\uDE00x" matches "😀"
    }

    @Test fun `two chars token`() {
        token = token("😀")

        "🤪" doesnt match
        "😀" matches "😀" leaving ""
        "😀x" matches "😀" leaving "x"
        "x" doesnt match leaving "x"
        "\uD83D\uDE00" matches "\uD83D\uDE00"
        "\uD83D\uDE00x" matches "\uD83D\uDE00"
        "\uD83D\uDE00" matches "😀"
        "\uD83D\uDE00x" matches "😀"
    }

    @Test fun `empty token`() {
        token = token("")
        assertThat(token).isEqualTo(empty)

        "" matches "" leaving ""
        "x" matches "" leaving "x"
        "xy" matches "" leaving "xy"
        "😀" matches ""
    }

    @Test fun startOfLine() {
        token = startOfLine

        "xy".afterRead('x') doesnt match leaving "y"
        "x".afterRead('x') doesnt match leaving ""
        "" matches "" leaving ""
        "x" matches "" leaving "x"
    }

    @Test fun whitespace() {
        token = whitespace

        "" doesnt match
        "x " doesnt match
        " " matches " " leaving ""
        " x" matches " " leaving "x"
        "  " matches " " leaving " "
        "\t" matches "\t"
        "\tx" matches "\t"
        "\t\t" matches "\t"
        "\n" matches "\n"
        "\nx" matches "\n"
        "\n\n" matches "\n"
        "\r" matches "\r"
        "\rx" matches "\r"
        "\r\r" matches "\r"
    }

    @Test fun `three code points token`() {
        token = token("xyz")

        "" doesnt match leaving ""
        "x" doesnt match leaving "x"
        "xy" doesnt match leaving "xy"
        "xzy" doesnt match
        "zxy" doesnt match
        "xyz" matches "xyz" leaving ""
        "xyzx" matches "xyz" leaving "x"
        "xxyz" doesnt match
    }

    @Test fun `code point range token`() {
        token = symbol(CodePoint.of('x')..CodePoint.of('z'))

        "w" doesnt match
        "x" matches "x"
        "y" matches "y"
        "z" matches "z"
        "xa" matches "x" leaving "a"
        "ya" matches "y"
        "za" matches "z"
        "ax" doesnt match leaving "ax"
        "ay" doesnt match
        "az" doesnt match
    }

    @Test fun `code point until range token`() {
        token = symbol(CodePoint.of('x') until CodePoint.of('z'))

        "w" doesnt match leaving "w"
        "x" matches "x"
        "y" matches "y"
        "z" doesnt match
        "xa" matches "x" leaving "a"
        "ya" matches "y" leaving "a"
        "za" doesnt match
        "ax" doesnt match
        "ay" doesnt match
        "az" doesnt match
    }

    @Test fun `token or token`() {
        token = symbol('x') or symbol('y')

        "x" matches "x" leaving ""
        "y" matches "y" leaving ""
        "z" doesnt match leaving "z"
        "xa" matches "x" leaving "a"
        "ya" matches "y"
        "xy" matches "x" leaving "y"
        "za" doesnt match
        "ax" doesnt match
        "ay" doesnt match
        "az" doesnt match
    }

    @Test fun `token and token`() {
        token = whitespace and symbol(' ')

        " " matches " " leaving ""
        "\t" doesnt match
        "x" doesnt match
        " x" matches " " leaving "x"
        "\tx" doesnt match
        "xy" doesnt match
        " xy" matches " "
        "\txy" doesnt match
    }

    @Test fun `not token`() {
        token = !whitespace

        "x" matches "x" leaving ""
        "x " matches "x" leaving " "
        " " doesnt match
        "\n" doesnt match
        "\r" doesnt match
        "\t" doesnt match
        "\uFEFF" matches "\uFEFF"
        "  " doesnt match
    }

    @Test fun `token minus token`() {
        token = whitespace - symbol(' ')

        "\t" matches "\t" leaving ""
        "\n" matches "\n"
        " " doesnt match
        "x" doesnt match
        "\tx" matches "\t" leaving "x"
        "\nx" matches "\n"
        " x" doesnt match
        "xy" doesnt match
        "\txy" matches "\t"
        "\nxy" matches "\n"
        " xy" doesnt match
    }

    @Test fun `token minus token minus token`() {
        token = whitespace - symbol(' ') - symbol('\n')

        "\t" matches "\t" leaving ""
        "\n" doesnt match
        " " doesnt match
        "x" doesnt match
        " \t" doesnt match
        "\n\t" doesnt match
        "\t\n" matches "\t" leaving "\n"
        "\tx" matches "\t"
        "\nx" doesnt match
        " x" doesnt match
        "xy" doesnt match
        "\txy" matches "\t"
        "\nxy" doesnt match
        " xy" doesnt match
    }

    @Test fun `token plus token`() {
        token = symbol('x') + symbol('y')

        "" doesnt match
        "x" doesnt match
        "y" doesnt match
        "xy" matches "xy" leaving ""
        "xyz" matches "xy" leaving "z"
    }

    @Test fun `token plus token plus token`() {
        token = whitespace + symbol('x') + symbol('y')

        "" doesnt match
        " " doesnt match
        "x" doesnt match
        "y" doesnt match
        " x" doesnt match
        " y" doesnt match
        " yx" doesnt match
        " xy" matches " xy" leaving ""
        "\txy" matches "\txy" leaving ""
        " xyz" matches " xy" leaving "z"
    }

    @Test fun `times 2`() {
        token = symbol('x') * 2

        "xx" matches "xx" leaving ""
        "xxx" matches "xx" leaving "x"
        "xxy" matches "xx" leaving "y"
        "yxx" doesnt match
        "x" doesnt match
        "y" doesnt match leaving "y"
    }

    @Test fun `times zero_or_once`() {
        token = symbol('x') * zero_or_once

        "" matches "" leaving ""
        "x" matches "x" leaving ""
        "xx" matches "x" leaving "x"
        "xxx" matches "x" leaving "xx"
        "xy" matches "x" leaving "y"
        "yx" matches "" leaving "yx"
        "y" matches "" leaving "y"
    }

    @Test fun `times zero_or_more`() {
        token = symbol('x') * zero_or_more

        "" matches "" leaving ""
        "x" matches "x" leaving ""
        "xx" matches "xx" leaving ""
        "xxx" matches "xxx" leaving ""
        "xy" matches "x" leaving "y"
        "yx" matches "" leaving "yx"
        "y" matches "" leaving "y"
    }

    @Test fun `times once_or_more`() {
        token = symbol('x') * once_or_more

        "" doesnt match leaving ""
        "x" matches "x" leaving ""
        "xx" matches "xx" leaving ""
        "xxx" matches "xxx" leaving ""
        "xy" matches "x" leaving "y"
        "yx" doesnt match leaving "yx"
        "y" doesnt match leaving "y"
    }

    @Test fun `symbol name is info`() {
        token = symbol('x')

        assertThat(token.toString()).isEqualTo("[x][LATIN SMALL LETTER X][0x78]")
    }

    @Test fun `should rename token`() {
        token = symbol('x') named "foo"

        assertThat(token.toString()).isEqualTo("foo")
    }

    @Test fun `EOF should match empty`() {
        token = endOfFile

        "x" doesnt match leaving "x"
        "" matches "" leaving ""
    }

    @Disabled @Test fun `recursive factory`() {
        fun foo(n: Int): Token = symbol('x') + foo(n) or symbol('y')
        token = foo(3)

        "y" matches "y"
        "xy" matches "xy"
        "yx" matches "y"
        "z" doesnt match
    }
}
