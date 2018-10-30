package spec.generator

import com.github.t1.yaml.tools.CodePoint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.LiteralExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.RangeExpression
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
        val production = production(codePoint('x'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <[x][LATIN SMALL LETTER X][0x78]>\n" +
            "     */\n" +
            "    `foo`('x'),\n"))
    }

    @Test fun shouldGenerateBigCodePointProduction() {
        val production = production(CodePointExpression(CodePoint.of(0x10428)))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <[\\uD801\\uDC28][DESERET SMALL LETTER LONG I][0x10428]>\n" +
            "     */\n" +
            "    `foo`(\"\\uD801\\uDC28\"),\n"))
    }

    @Test fun shouldGenerateThreeCodePointSequenceProduction() {
        val production = production(seq(codePoint('a'), codePoint('b'), codePoint('c')))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <[a][LATIN SMALL LETTER A][0x61]> + <[b][LATIN SMALL LETTER B][0x62]> + <[c][LATIN SMALL LETTER C][0x63]>\n" +
            "     */\n" +
            "    `foo`('a' + 'b' + 'c'),\n"))
    }

    @Test fun shouldGenerateCodePointRangeProduction() {
        val production = production(range('0', '9'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   [<[0][DIGIT ZERO][0x30]>-<[9][DIGIT NINE][0x39]>]\n" +
            "     */\n" +
            "    `foo`('0' .. '9'),\n"))
    }

    @Disabled @Test fun shouldGenerateOneCharLiteralProduction() {
        val production = production(LiteralExpression("x"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <x>\n" +
            "     */\n" +
            "    `foo`(something with x),\n"))
    }

    @Disabled @Test fun shouldGenerateThreeCharLiteralProduction() {
        val production = production(LiteralExpression("bar"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   <bar>\n" +
            "     */\n" +
            "    `foo`(something with bar),\n"))
    }

    @Disabled @Test fun shouldGenerateLiteralProductionWithOneArg() {
        val production = Production(0, "foo", "n", LiteralExpression("baz"))

        val written = generate(production)

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
        val production = Production(0, "foo", "<n", LiteralExpression("bar"))

        val written = generate(production)

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
        val production = Production(0, "foo", "≤n", LiteralExpression("bar"))

        val written = generate(production)

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
        val production = Production(0, "foo", "c,n", LiteralExpression("bar"))

        val written = generate(production)

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
        val production = Production(0, "c-foo", null, LiteralExpression("bar"))

        val written = generate(production)

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
        val production = Production(0, "c+foo", null, LiteralExpression("bar"))

        val written = generate(production)

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
        val production = production(ref("bar"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   ->bar\n" +
            "     */\n" +
            "    `foo`(`bar`),\n"))
    }


    @Test fun shouldGenerateAlternativeReferencesProduction() {
        val production = production(alt(ref("ref1"), ref("ref2")))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   [->ref1 ||\n" +
            "     *    ->ref2]\n" +
            "     */\n" +
            "    `foo`(`ref1` or `ref2`),\n"))
    }

    @Test fun shouldGenerateAlternativeCodePointsProduction() {
        val production = production(alt(codePoint('a'), codePoint('b')))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   [<[a][LATIN SMALL LETTER A][0x61]> ||\n" +
            "     *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            "     */\n" +
            "    `foo`('a' or 'b'),\n"))
    }

    @Test fun shouldGenerateMixedAlternativesProduction() {
        val production = production(alt(ref("ref1"), codePoint('b')))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   [->ref1 ||\n" +
            "     *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            "     */\n" +
            "    `foo`(`ref1` or 'b'),\n"))
    }

    @Test fun shouldGenerateAlternativeOfCodePointOrRangeProduction() {
        val production = production(alt(codePoint('\t'), range(CodePoint.of(' '), CodePoint.of(0x10ffff))))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   [<[\\t][CHARACTER TABULATION][0x9]> ||\n" +
            "     *    [<[ ][SPACE][0x20]>-<[\\uDBFF\\uDFFF][?][0x10ffff]>]]\n" +
            "     */\n" +
            "    `foo`('\\t' or (' ' .. \"\\uDBFF\\uDFFF\")),\n"))
    }

    @Test fun shouldGenerateAlternativeOfSequenceAndReferenceProduction() {
        val production = production(alt(seq(ref("a"), ref("b")), ref("a"), ref("b")))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     *   [->a + ->b ||\n" +
            "     *    ->a ||\n" +
            "     *    ->b]\n" +
            "     */\n" +
            "    `foo`((`a` + `b`) or `a` or `b`),\n"))
    }

    @Disabled @Test fun shouldGenerateMinusRefProduction() {
        val production = production(MinusExpression(ref("c-printable")).minus(ref("b-char")).minus(ref("c-byte-order-mark")))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "    /**\n" +
            "     * `27` : foo:\n" +
            "     *   ->c-printable - ->b-char - ->c-byte-order-mark\n" +
            "     */\n" +
            "    `foo`()\n"))
    }

    private fun production(expression: Expression) = Production(0, "foo", null, expression)

    private fun codePoint(char: Char) = codePoint(CodePoint.of(char))
    private fun codePoint(codePoint: CodePoint) = CodePointExpression(codePoint)

    private fun range(min: Char, max: Char) = range(CodePoint.of(min), CodePoint.of(max))
    private fun range(min: CodePoint, max: CodePoint) = RangeExpression(codePoint(min), codePoint(max))
    private fun ref(ref: String) = ReferenceExpression(ref)

    private fun seq(e1: Expression, e2: Expression, vararg more: Expression): SequenceExpression {
        var out = SequenceExpression.of(e1, e2)
        for (e in more)
            out = SequenceExpression.of(out, e)
        return out
    }

    private fun alt(e1: Expression, e2: Expression, vararg more: Expression): AlternativesExpression {
        var out = AlternativesExpression.of(e1, e2)
        for (e in more)
            out = AlternativesExpression.of(out, e)
        return out
    }

    private fun generate(vararg productions: Production): String {
        val writer = StringWriter()

        val spec = Spec(asList(*productions))
        val generator = YamlSymbolGenerator(spec)
        generator.generateCode("FooParser", writer)

        return writer.toString()
    }

    private fun source(body: String): String {
        return YamlSymbolGenerator.PREFIX +
            "enum class FooParser(private val token: Token) : Token {\n" +
            body +
            YamlSymbolGenerator.SUFFIX
    }
}
