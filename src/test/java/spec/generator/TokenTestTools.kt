package spec.generator

import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Match
import com.github.t1.yaml.tools.Token
import org.assertj.core.api.Assertions

open class TokenTestTools {
    lateinit var token: Token

    class DummyConstant

    val match = DummyConstant()

    infix fun String.matches(that: String) = CodePointReader(this) matches (that)
    infix fun CodePointReader.matches(that: String) {
        Assertions.assertThat(token.match(this)).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(that)))
    }

    infix fun String.doesnt(dummy: DummyConstant) = CodePointReader(this).doesnt(dummy)
    infix fun CodePointReader.doesnt(@Suppress("UNUSED_PARAMETER") dummy: DummyConstant) {
        Assertions.assertThat(token.match(this)).isEqualTo(Match(matches = false))
    }

    fun String.afterRead(expected: Char) = CodePointReader(this).apply { Assertions.assertThat(read()).isEqualTo(CodePoint.of(expected)) }
}
