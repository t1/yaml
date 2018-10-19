package test

import com.github.t1.yaml.Yaml
import com.github.t1.yaml.model.Stream
import org.assertj.core.api.Assertions.assertThat

private const val BOM = "⇔"
private const val EMPTY = "°"

fun toStringWithoutTrailingNl(o: Any?): String? {
    if (o == null)
        return null
    var string = o.toString()
    if (!string.isEmpty() && string[string.length - 1] == '\n')
        string = string.substring(0, string.length - 1)
    return string
}

fun parseAndCheck(input: String, expectedCanonical: String) {
    val stream = parse(input)
    assertThat(toStringWithoutTrailingNl(stream)).describedAs("stream toString")
        .isEqualTo(input.replace(BOM, ""))
    assertThat(stream.canonicalize().toString()).describedAs("canonicalized stream")
        .isEqualTo(expectedCanonical)
}

fun parse(yaml: String): Stream {
    return Yaml.parseAll(yaml
        .replace(BOM, "\uFEFF")
        .replace(EMPTY, ""))
}
