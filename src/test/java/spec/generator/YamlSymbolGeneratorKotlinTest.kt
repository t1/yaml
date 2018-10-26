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
import java.io.StringWriter
import java.util.Arrays.asList

@Disabled
class YamlSymbolGeneratorKotlinTest {
    private fun generate(vararg productions: Production): String {
        val writer = StringWriter()

        val spec = Spec(asList(*productions))
        val generator = YamlSymbolGenerator(spec)
        generator.generateCode("FooParser", writer)

        return writer.toString()
    }

    private fun java(body: String): String {
        return "package com.github.t1.yaml.parser;\n" +
            "\n" +
            "import com.github.t1.yaml.dump.CodePoint;\n" +
            "import com.github.t1.yaml.model.Document;\n" +
            "\n" +
            "import javax.annotation.Generated;\n" +
            "import java.io.BufferedReader;\n" +
            "import java.io.InputStream;\n" +
            "import java.io.InputStreamReader;\n" +
            "import java.io.Reader;\n" +
            "import java.io.StringReader;\n" +
            "import java.util.Optional;\n" +
            "\n" +
            "import static java.nio.charset.StandardCharsets.UTF_8;\n" +
            "\n" +
            "@Generated(\"" + YamlSymbolGenerator::class.java.name + "\")\n" +
            "public class FooParser {\n" +
            "    private final Scanner next;\n" +
            "    private Document document;\n" +
            "\n" +
            "    public FooParser(String yaml) { this(new StringReader(yaml)); }\n" +
            "\n" +
            "    public FooParser(InputStream inputStream) { this(new BufferedReader(new InputStreamReader(inputStream, UTF_8))); }\n" +
            "\n" +
            "    public FooParser(Reader reader) { this.next = new Scanner(reader); }\n" +
            "\n" +
            "    public Optional<Document> document() {\n" +
            "        l_bare_document();\n" +
            "        return Optional.ofNullable(document);\n" +
            "    }\n" +
            "\n" +
            "    public boolean more() { return next.more(); }\n" +
            body +
            "}\n"
    }

    @Test fun shouldGenerateEmptySource() {
        val written = generate()

        assertThat(written).isEqualTo(java(""))
    }

    @Test fun shouldGenerateSimpleLiteralProduction() {
        val written = generate(Production(0, "foo", null,
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : foo:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    private Object foo() {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Test fun shouldGenerateLiteralProductionWithOneArg() {
        val written = generate(Production(0, "foo", "n",
            LiteralExpression("baz")))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : foo [n]:\n" +
            "     *   <baz>\n" +
            "     */\n" +
            "    private Object foo(int n) {\n" +
            "        return next.accept(\"baz\") ? \"baz\" : null;\n" +
            "    }\n"))
    }

    @Test fun shouldGenerateLiteralProductionWithLessArg() {
        val written = generate(Production(0, "foo", "<n",
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : foo [<n]:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    private Object foo_less(int n) {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Test fun shouldGenerateLiteralProductionWithLessEqArg() {
        val written = generate(Production(0, "foo", "≤n",
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : foo [≤n]:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    private Object foo_lessEq(int n) {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Test fun shouldGenerateLiteralProductionWithTwoArgs() {
        val written = generate(Production(0, "foo", "c,n",
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : foo [c,n]:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    private Object foo(int c, int n) {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Test fun shouldGenerateLiteralProductionWithMinus() {
        val written = generate(Production(0, "c-foo", null,
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : c-foo:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    private Object c_foo() {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Test fun shouldGenerateLiteralProductionWithPlus() {
        val written = generate(Production(0, "c+foo", null,
            LiteralExpression("bar")))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : c+foo:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    private Object c_foo() {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }


    @Test fun shouldGenerateRefProduction() {
        val written = generate(Production(0, "foo", null,
            ReferenceExpression("bar")))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : foo:\n" +
            "     *   ->bar\n" +
            "     */\n" +
            "    private Object foo() {\n" +
            "        return bar();\n" +
            "    }\n"))
    }


    @Test fun shouldGenerateAlternativeReferencesProduction() {
        val written = generate(Production(0, "ref-alternatives", null,
            AlternativesExpression.of(ReferenceExpression("ref1"), ReferenceExpression("ref2"))))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : ref-alternatives:\n" +
            "     *   [->ref1 ||\n" +
            "     *    ->ref2]\n" +
            "     */\n" +
            "    private Object ref_alternatives() {\n" +
            "        Object result = ref1();\n" +
            "        if (result == null)\n" +
            "            result = ref2();\n" +
            "        if (result == null)\n" +
            "            throw new YamlParseException(\"can't find ref-alternatives\" + next);\n" +
            "        return result;\n" +
            "    }\n"))
    }

    @Test fun shouldGenerateAlternativeCodePointsProduction() {
        val written = generate(Production(0, "c-alternatives", null,
            AlternativesExpression.of(
                CodePointExpression(CodePoint.of("a")),
                CodePointExpression(CodePoint.of("b")))))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : c-alternatives:\n" +
            "     *   [<[a][LATIN SMALL LETTER A][0x61]> ||\n" +
            "     *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            "     */\n" +
            "    private Object c_alternatives() {\n" +
            "        Object result = next.is(CodePoint.of(0x61)) ? CodePoint.of(0x61) : null;\n" +
            "        if (result == null)\n" +
            "            result = next.is(CodePoint.of(0x62)) ? CodePoint.of(0x62) : null;\n" +
            "        if (result == null)\n" +
            "            throw new YamlParseException(\"can't find c-alternatives\" + next);\n" +
            "        return result;\n" +
            "    }\n"))
    }

    @Test fun shouldGenerateMixedAlternativesProduction() {
        val written = generate(Production(0, "mixed-alternatives", null,
            AlternativesExpression.of(
                ReferenceExpression("ref1"),
                CodePointExpression(CodePoint.of("b")))))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [0] : mixed-alternatives:\n" +
            "     *   [->ref1 ||\n" +
            "     *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            "     */\n" +
            "    private Object mixed_alternatives() {\n" +
            "        Object result = ref1();\n" +
            "        if (result == null)\n" +
            "            result = next.is(CodePoint.of(0x62)) ? CodePoint.of(0x62) : null;\n" +
            "        if (result == null)\n" +
            "            throw new YamlParseException(\"can't find mixed-alternatives\" + next);\n" +
            "        return result;\n" +
            "    }\n"))
    }

    @Disabled("not yet implemented")
    @Test fun shouldGenerateMinusRefProduction() {
        val written = generate(Production(27, "nb-char", null,
            MinusExpression(ReferenceExpression("c-printable"))
                .minus(ReferenceExpression("b-char"))
                .minus(ReferenceExpression("c-byte-order-mark"))
        ))

        assertThat(written).isEqualTo(java("\n" +
            "    /**\n" +
            "     * [27] : nb-char:\n" +
            "     *   ->c-printable - ->b-char - ->c-byte-order-mark\n" +
            "     */\n" +
            "    private Object nb_char() {\n" +
            "" +
            "    }\n"))
    }
}
