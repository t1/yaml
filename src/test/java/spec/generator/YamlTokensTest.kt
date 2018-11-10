package spec.generator

import com.github.t1.yaml.parser.`s-indent`
import com.github.t1.yaml.parser.`s-indent≤`
import com.github.t1.yaml.parser.`s-indent≪`
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Match
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class YamlTokensTest {
    @Test fun shouldMatchIndent() {
        fun indent(n: Int, string: String) = `s-indent`(n).match(CodePointReader(string))

        assertThat(indent(0, "")).isEqualTo(Match(matches = false))
        assertThat(indent(0, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(0, "  ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(0, "   ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))

        assertThat(indent(1, "")).isEqualTo(Match(matches = false))
        assertThat(indent(1, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(1, "  ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(1, "   ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))

        assertThat(indent(2, "")).isEqualTo(Match(matches = false))
        assertThat(indent(2, " ")).isEqualTo(Match(matches = false))
        assertThat(indent(2, "  ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("  ")))
        assertThat(indent(2, "   ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("  ")))

        assertThat(indent(3, "")).isEqualTo(Match(matches = false))
        assertThat(indent(3, " ")).isEqualTo(Match(matches = false))
        assertThat(indent(3, "  ")).isEqualTo(Match(matches = false))
        assertThat(indent(3, "   ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("   ")))

        assertThat(indent(4, "")).isEqualTo(Match(matches = false))
        assertThat(indent(4, " ")).isEqualTo(Match(matches = false))
        assertThat(indent(4, "  ")).isEqualTo(Match(matches = false))
        assertThat(indent(4, "   ")).isEqualTo(Match(matches = false))
    }

    @Test fun shouldMatchIndentLess() {
        fun indent(n: Int, string: String) = `s-indent≪`(n).match(CodePointReader(string))

        assertThat(indent(0, "")).isEqualTo(Match(matches = false))
        assertThat(indent(0, " ")).isEqualTo(Match(matches = false))
        assertThat(indent(0, "  ")).isEqualTo(Match(matches = false))
        assertThat(indent(0, "   ")).isEqualTo(Match(matches = false))

        assertThat(indent(1, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(1, " ")).isEqualTo(Match(matches = false))
        assertThat(indent(1, "  ")).isEqualTo(Match(matches = false))
        assertThat(indent(1, "   ")).isEqualTo(Match(matches = false))

        assertThat(indent(2, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(2, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(2, "  ")).isEqualTo(Match(matches = false))
        assertThat(indent(2, "   ")).isEqualTo(Match(matches = false))

        assertThat(indent(3, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(3, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(3, "  ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("  ")))
        assertThat(indent(3, "   ")).isEqualTo(Match(matches = false))

        assertThat(indent(4, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(4, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(4, "  ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("  ")))
        assertThat(indent(4, "   ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("   ")))
    }

    @Test fun shouldMatchIndentLessOrEq() {
        fun indent(n: Int, string: String) = `s-indent≤`(n).match(CodePointReader(string))

        assertThat(indent(0, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(0, " ")).isEqualTo(Match(matches = false))
        assertThat(indent(0, "  ")).isEqualTo(Match(matches = false))
        assertThat(indent(0, "   ")).isEqualTo(Match(matches = false))

        assertThat(indent(1, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(1, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(1, "  ")).isEqualTo(Match(matches = false))
        assertThat(indent(1, "   ")).isEqualTo(Match(matches = false))

        assertThat(indent(2, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(2, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(2, "  ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("  ")))
        assertThat(indent(2, "   ")).isEqualTo(Match(matches = false))

        assertThat(indent(3, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(3, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(3, "  ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("  ")))
        assertThat(indent(3, "   ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("   ")))

        assertThat(indent(4, "")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("")))
        assertThat(indent(4, " ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(" ")))
        assertThat(indent(4, "  ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("  ")))
        assertThat(indent(4, "   ")).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf("   ")))
    }
}
