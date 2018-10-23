package helpers

import com.github.t1.yaml.Yaml
import com.github.t1.yaml.model.Stream
import com.github.t1.yaml.parser.YamlParseException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.fail

private const val BOM = "⇔"
private const val EMPTY = "°"
private const val BREAK = "↓\n" // assert ↓ continues with \n
private const val TAB = "→"
private const val SPACE = "·"

private fun resolveMagic(yaml: String): String {
    return yaml
        .replace(BOM, "\uFEFF")
        .replace(EMPTY, "")
        .replace(SPACE, " ")
        .replace(TAB, "\t")
        .replace(BREAK, "\n")
}

fun parseAndCheck(input: String, expectedCanonical: String) {
    val stream = parse(input)
    assertStreamPresentation(input, stream)
    assertCanonicalStreamPresentation(expectedCanonical, stream)
}

fun assertCanonicalStreamPresentation(expectedCanonical: String, stream: Stream) {
    Yaml.canonicalize(stream)
    assertThat(Yaml.present(stream)).describedAs("canonicalized stream presentation")
        .isEqualTo(expectedCanonical)
}

fun assertStreamPresentation(input: String, stream: Stream) {
    assertThat(withoutTrailingNl(stream)).describedAs("stream presentation")
        .isEqualTo(resolveMagic(input.replace(BOM, "")))
}

fun withoutTrailingNl(stream: Stream?): String? {
    if (stream == null)
        return null
    var string = Yaml.present(stream)
    if (!string.isEmpty() && string[string.length - 1] == '\n')
        string = string.substring(0, string.length - 1)
    return string
}

fun parse(yaml: String): Stream = Yaml.parseAll(resolveMagic(yaml))

fun catchParseException(body: () -> Unit): YamlParseException = Assertions.catchThrowableOfType(body, YamlParseException::class.java)
    ?: fail("YamlParseException expected but nothing thrown")
