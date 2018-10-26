package spec.generator

import com.github.t1.yaml.tools.CodePoint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.LiteralExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.SequenceExpression
import java.io.StringWriter
import java.util.Arrays.asList

class YamlSymbolGeneratorTest {
    @Test fun shouldGenerateEmptySource() {
        val written = generate()

        assertThat(written).isEqualTo(source(""))
    }

    @Test fun shouldGenerateCodePointProduction() {
        val written = generate(Production(0, "foo", null, codePoint('x')))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <[x][LATIN SMALL LETTER X][0x78]>\n" +
            "     */\n" +
            "    `foo`('x'),\n"))
    }

    @Test fun shouldGenerateThreeCodePointSequenceProduction() {
        val expression = SequenceExpression.of(codePoint('a'), SequenceExpression.of(codePoint('b'), codePoint('c')))

        val written = generate(Production(0, "foo", null, expression))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <[a][LATIN SMALL LETTER A][0x61]> + <[b][LATIN SMALL LETTER B][0x62]> + <[c][LATIN SMALL LETTER C][0x63]>\n" +
            "     */\n" +
            "    `foo`(symbol('a') + symbol('b') + symbol('c')),\n"))
    }

    @Disabled @Test fun shouldGenerateOneCharLiteralProduction() {
        val written = generate(Production(0, "foo", null, LiteralExpression("x")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <x>\n" +
            "     */\n" +
            "    `foo`(something with x),\n"))
    }

    @Disabled @Test fun shouldGenerateThreeCharLiteralProduction() {
        val written = generate(Production(0, "foo", null,
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    `foo`(something with bar),\n"))
    }

    @Disabled @Test fun shouldGenerateLiteralProductionWithOneArg() {
        val written = generate(Production(0, "foo", "n",
            LiteralExpression("baz")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo [n]:\n" +
            "     *   <baz>\n" +
            "     */\n" +
            "    `foo(`int n) {\n" +
            "        return next.accept(\"baz\") ? \"baz\" : null;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateLiteralProductionWithLessArg() {
        val written = generate(Production(0, "foo", "<n",
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo [<n]:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    `foo_less`(int n) {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateLiteralProductionWithLessEqArg() {
        val written = generate(Production(0, "foo", "≤n",
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo [≤n]:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    `foo_less`Eq(int n) {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateLiteralProductionWithTwoArgs() {
        val written = generate(Production(0, "foo", "c,n",
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo [c,n]:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    `foo(`int c, int n) {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateLiteralProductionWithMinus() {
        val written = generate(Production(0, "c-foo", null,
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : c-foo:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    `c_foo`() {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateLiteralProductionWithPlus() {
        val written = generate(Production(0, "c+foo", null,
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : c+foo:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    `c_foo`() {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }


    @Test fun shouldGenerateRefProduction() {
        val written = generate(Production(0, "foo", null, ReferenceExpression("bar")))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   ->bar\n" +
            "     */\n" +
            "    `foo`(`bar`),\n"))
    }


    @Disabled @Test fun shouldGenerateAlternativeReferencesProduction() {
        val written = generate(Production(0, "ref-alternatives", null,
            AlternativesExpression.of(ReferenceExpression("ref1"), ReferenceExpression("ref2"))))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : ref-alternatives:\n" +
            "     *   [->ref1 ||\n" +
            "     *    ->ref2]\n" +
            "     */\n" +
            "    `ref_alternatives`() {\n" +
            "        Object result = ref1();\n" +
            "        if (result == null)\n" +
            "            result = ref2();\n" +
            "        if (result == null)\n" +
            "            throw new YamlParseException(\"can't find ref-alternatives\" + next);\n" +
            "        return result;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateAlternativeCodePointsProduction() {
        val written = generate(Production(0, "c-alternatives", null,
            AlternativesExpression.of(codePoint('a'), codePoint('b'))))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : c-alternatives:\n" +
            "     *   [<[a][LATIN SMALL LETTER A][0x61]> ||\n" +
            "     *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            "     */\n" +
            "    `c_alternatives`() {\n" +
            "        Object result = next.is(CodePoint.of(0x61)) ? CodePoint.of(0x61) : null;\n" +
            "        if (result == null)\n" +
            "            result = next.is(CodePoint.of(0x62)) ? CodePoint.of(0x62) : null;\n" +
            "        if (result == null)\n" +
            "            throw new YamlParseException(\"can't find c-alternatives\" + next);\n" +
            "        return result;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateMixedAlternativesProduction() {
        val written = generate(Production(0, "mixed-alternatives", null,
            AlternativesExpression.of(
                ReferenceExpression("ref1"),
                codePoint('b'))))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : mixed-alternatives:\n" +
            "     *   [->ref1 ||\n" +
            "     *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            "     */\n" +
            "    `mixed_alternatives`() {\n" +
            "        Object result = ref1();\n" +
            "        if (result == null)\n" +
            "            result = next.is(CodePoint.of(0x62)) ? CodePoint.of(0x62) : null;\n" +
            "        if (result == null)\n" +
            "            throw new YamlParseException(\"can't find mixed-alternatives\" + next);\n" +
            "        return result;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateMinusRefProduction() {
        val written = generate(Production(27, "nb-char", null,
            MinusExpression(ReferenceExpression("c-printable"))
                .minus(ReferenceExpression("b-char"))
                .minus(ReferenceExpression("c-byte-order-mark"))
        ))

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `27` : nb-char:\n" +
            "     *   ->c-printable - ->b-char - ->c-byte-order-mark\n" +
            "     */\n" +
            "    `nb_char`() {\n" +
            "" +
            "    }\n"))
    }

    private fun codePoint(char: Char) = CodePointExpression(CodePoint.of(char))

    private fun generate(vararg productions: Production): String {
        val writer = StringWriter()

        val spec = Spec(asList(*productions))
        val generator = YamlSymbolGenerator(spec)
        generator.generateCode("FooParser", writer)

        return writer.toString()
    }

    private fun source(body: String): String {
        return "package com.github.t1.yaml.parser\n" +
            "\n" +
            "import com.github.t1.yaml.tools.CodePoint\n" +
            "import com.github.t1.yaml.tools.Symbol\n" +
            "import com.github.t1.yaml.tools.Token\n" +
            "import com.github.t1.yaml.tools.symbol\n" +
            "import javax.annotation.Generated\n" +
            "\n" +
            "/**\n" +
            " * The productions as specified in the YAML spec\n" +
            " *\n" +
            " * e-        A production matching no characters.\n" +
            " * c-        A production starting and ending with a special character.\n" +
            " * b-        A production matching a single line break.\n" +
            " * nb-       A production starting and ending with a non-break character.\n" +
            " * s-        A production starting and ending with a white space character.\n" +
            " * ns-       A production starting and ending with a non-space character.\n" +
            " * l-        A production matching complete line(s).\n" +
            " * X-Y-      A production starting with an X- character and ending with a Y- character, where X- and Y- are any of the above prefixes.\n" +
            " * X+, X-Y+  A production as above, with the additional property that the matched content indentation level is greater than the specified n parameter.\n" +
            " */\n" +
            "@Generated(\"${YamlSymbolGenerator::class.qualifiedName}\")\n" +
            "@Suppress(\"unused\", \"EnumEntryName\", \"NonAsciiCharacters\")\n" +
            "enum class FooParser(override val predicates: List<(CodePoint) -> Boolean>) : Token {\n" +
            body +
            "    ;\n" +
            "\n" +
            "    constructor(codePoint: Char) : this(symbol(codePoint))\n" +
            "    constructor(symbol: Symbol) : this(listOf(symbol.predicate))\n" +
            "    constructor(token: Token) : this(token.predicates)\n" +
            "    @Deprecated(\"missing production visitor\") constructor() : this(listOf())\n" +
            "}\n"
    }
}
