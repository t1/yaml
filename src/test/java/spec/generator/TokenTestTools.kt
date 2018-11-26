package spec.generator

import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Match
import com.github.t1.yaml.tools.Token
import org.assertj.core.api.Assertions.assertThat

open class TokenTestTools {
    lateinit var token: Token
    lateinit var func: (CodePointReader) -> Any

    infix fun String.afterRead(expected: Char) = CodePointReader(this).apply { assertThat(read()).isEqualTo(CodePoint.of(expected)) }

    infix fun String.matches(expected: Any) = CodePointReader(this) matches expected
    infix fun CodePointReader.matches(expected: Any): CodePointReader {
        assertThat(func(this)).isEqualTo(expected)
        return this
    }

    infix fun String.matches(that: String) = CodePointReader(this) matches (that)
    infix fun CodePointReader.matches(that: String): CodePointReader {
        assertThat(token.match(this)).isEqualTo(Match(matches = true, codePoints = CodePoint.allOf(that)))
        return this
    }

    class DummyConstant

    val match = DummyConstant()
    infix fun String.doesnt(dummy: DummyConstant) = CodePointReader(this).doesnt(dummy)
    infix fun CodePointReader.doesnt(@Suppress("UNUSED_PARAMETER") dummy: DummyConstant): CodePointReader {
        assertThat(token.match(this)).isEqualTo(Match(matches = false))
        return this
    }

    infix fun CodePointReader.leaving(text: String) {
        expect(CodePoint.allOf(text))
        assert(isEndOfFile) { "expected reader to end in '$text' but found more: '${read()}'" }
    }
}
