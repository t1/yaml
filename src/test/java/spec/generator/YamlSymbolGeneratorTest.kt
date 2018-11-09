package spec.generator

import com.github.t1.yaml.parser.`s-indent`
import com.github.t1.yaml.parser.`s-indent≤`
import com.github.t1.yaml.parser.`s-indent≪`
import com.github.t1.yaml.tools.CodePoint
import com.github.t1.yaml.tools.CodePointReader
import com.github.t1.yaml.tools.Match
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression
import spec.generator.Expression.VariableExpression
import java.io.StringWriter
import java.util.Arrays.asList

class YamlSymbolGeneratorTest {
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

    @Test fun shouldGenerateEmptySource() {
        val written = generate()

        assertThat(written).isEqualTo(enumSource(""))
    }

    @Test fun shouldGenerateCodePointProduction() {
        val production = production(codePoint('x'))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            "     */\n" +
            "    `foo`('x'),\n"))
    }

    @Test fun shouldGenerateBigCodePointProduction() {
        val production = production(CodePointExpression(CodePoint.of(0x10428)))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * <[\\uD801\\uDC28][DESERET SMALL LETTER LONG I][0x10428]>\n" +
            "     */\n" +
            "    `foo`(\"\\uD801\\uDC28\"),\n"))
    }

    @Test fun shouldGenerateThreeCodePointSequenceProduction() {
        val production = production(seq(codePoint('a'), codePoint('b'), codePoint('c')))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * <[a][LATIN SMALL LETTER A][0x61]> + <[b][LATIN SMALL LETTER B][0x62]> + <[c][LATIN SMALL LETTER C][0x63]>\n" +
            "     */\n" +
            "    `foo`('a' + 'b' + 'c'),\n"))
    }

    @Test fun shouldGenerateRefProduction() {
        val bar = Production(1, "bar", listOf(), codePoint('x'))
        val foo = production(ref(bar))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * ->bar\n" +
            "     */\n" +
            "    `foo`(`bar`),\n" +
            "\n" +
            "    /**\n" +
            "     * `1` : bar:\n" +
            "     * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            "     */\n" +
            "    `bar`('x'),\n"))
    }

    @Test fun shouldGenerateCodePointRangeProduction() {
        val production = production(range('0', '9'))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * [<[0][DIGIT ZERO][0x30]>-<[9][DIGIT NINE][0x39]>]\n" +
            "     */\n" +
            "    `foo`('0'..'9'),\n"))
    }

    @Test fun shouldGenerateRepeatProduction() {
        val production = production(RepeatedExpression(codePoint('x'), "4"))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * (<[x][LATIN SMALL LETTER X][0x78]> × 4)\n" +
            "     */\n" +
            "    `foo`('x' * 4),\n"))
    }

    @Test fun shouldGenerateSwitchProduction() {
        val production = Production(0, "foo", listOf("c"), switch(
            "c = foo1" to codePoint('1'),
            "c = foo2" to codePoint('2'),
            "c = foo3" to codePoint('3'),
            "c = foo4" to codePoint('4')
        ))

        val written = generate(production)

        assertThat(written).isEqualTo(factoryFunSource("\n" +
            "/**\n" +
            " * `0` : foo(c):\n" +
            " * <c = foo1> ⇒ <[1][DIGIT ONE][0x31]>\n" +
            " * <c = foo2> ⇒ <[2][DIGIT TWO][0x32]>\n" +
            " * <c = foo3> ⇒ <[3][DIGIT THREE][0x33]>\n" +
            " * <c = foo4> ⇒ <[4][DIGIT FOUR][0x34]>\n" +
            " */\n" +
            "fun `foo`(c: InOutMode) = when (c) {\n" +
            "    `foo1` -> '1' describedAs \"foo(\$c)\"\n" +
            "    `foo2` -> '2' describedAs \"foo(\$c)\"\n" +
            "    `foo3` -> '3' describedAs \"foo(\$c)\"\n" +
            "    `foo4` -> '4' describedAs \"foo(\$c)\"\n" +
            "}\n"))
    }

    @Test fun shouldGenerateSwitchProductionToRefWithArgs() {
        val bar = Production(1, "bar", listOf("n"), RepeatedExpression(codePoint('x'), "n"))
        val foo = Production(0, "foo", listOf("n", "c"), switch(
            "c = foo1" to ref(bar),
            "c = foo2" to ref(bar),
            "c = foo3" to ref(bar),
            "c = foo4" to ref(bar)
        ))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(factoryFunSource("\n" +
            "/**\n" +
            " * `0` : foo(n,c):\n" +
            " * <c = foo1> ⇒ ->bar(n)\n" +
            " * <c = foo2> ⇒ ->bar(n)\n" +
            " * <c = foo3> ⇒ ->bar(n)\n" +
            " * <c = foo4> ⇒ ->bar(n)\n" +
            " */\n" +
            "fun `foo`(n: Int, c: InOutMode) = when (c) {\n" +
            "    `foo1` -> `bar`(n) describedAs \"foo(\$c)\"\n" +
            "    `foo2` -> `bar`(n) describedAs \"foo(\$c)\"\n" +
            "    `foo3` -> `bar`(n) describedAs \"foo(\$c)\"\n" +
            "    `foo4` -> `bar`(n) describedAs \"foo(\$c)\"\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `bar`(n: Int): Token {\n" +
            "    val token = 'x' * n\n" +
            "    return token(\"bar(\$n)\") { token.match(it) }\n" +
            "}\n"))
    }

    @Test fun shouldGenerateProductionWithOneArg() {
        val production = Production(0, "foo", listOf("n"), RepeatedExpression(codePoint('x'), "n"))

        val written = generate(production)

        assertThat(written).isEqualTo(factoryFunSource("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `foo`(n: Int): Token {\n" +
            "    val token = 'x' * n\n" +
            "    return token(\"foo(\$n)\") { token.match(it) }\n" +
            "}\n"))
    }

    @Test fun shouldGenerateProductionWithLessArg() {
        val production = Production(0, "foo", listOf("<n"), RepeatedExpression(codePoint('x'), "m", "Where m < n"))

        val written = generate(production)

        assertThat(written).isEqualTo(factoryFunSource("\n" +
            "/**\n" +
            " * `0` : foo(<n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × m /* Where m < n */)\n" +
            " */\n" +
            "fun `foo≪`(n: Int) = token(\"foo(<\$n)\") { reader ->\n" +
            "    val match = reader.mark { reader.readWhile { reader -> 'x'.match(reader).codePoints } }\n" +
            "    if (match.size >= n) return@token Match(matches = false)\n" +
            "    reader.read(match.size)\n" +
            "    return@token Match(matches = true, codePoints = match)\n" +
            "}\n"))
    }

    @Test fun shouldGenerateProductionWithLessEqArg() {
        val production = Production(0, "foo", listOf("≤n"), RepeatedExpression(codePoint('x'), "m", "Where m ≤ n"))

        val written = generate(production)

        assertThat(written).isEqualTo(factoryFunSource("\n" +
            "/**\n" +
            " * `0` : foo(≤n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × m /* Where m ≤ n */)\n" +
            " */\n" +
            "fun `foo≤`(n: Int) = token(\"foo(≤\$n)\") { reader ->\n" +
            "    val match = reader.mark { reader.readWhile { reader -> 'x'.match(reader).codePoints } }\n" +
            "    if (match.size > n) return@token Match(matches = false)\n" +
            "    reader.read(match.size)\n" +
            "    return@token Match(matches = true, codePoints = match)\n" +
            "}\n"))
    }

    @Test fun shouldGenerateProductionWithTwoArgs() {
        val production = Production(0, "foo", listOf("c", "n"), RepeatedExpression(codePoint('x'), "n"))

        val written = generate(production)

        assertThat(written).isEqualTo(factoryFunSource("\n" +
            "/**\n" +
            " * `0` : foo(c,n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `foo`(c: InOutMode, n: Int): Token {\n" +
            "    val token = 'x' * n\n" +
            "    return token(\"foo(\$c, \$n)\") { token.match(it) }\n" +
            "}\n"))
    }

    @Test fun shouldGenerateReferenceWithArgsProduction() {
        val bar = Production(1, "bar", listOf("n"), RepeatedExpression(codePoint('x'), "n"))
        val foo = Production(0, "foo", listOf("n"), ref(bar))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(factoryFunSource("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar(n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = `bar`(n)\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `bar`(n: Int): Token {\n" +
            "    val token = 'x' * n\n" +
            "    return token(\"bar(\$n)\") { token.match(it) }\n" +
            "}\n"))
    }

    @Disabled @Test fun shouldGenerateProductionWithMinus() {
        val production = Production(0, "c-foo", listOf(), VariableExpression("bar"))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : c-foo:\n" +
            "     * <bar>\n" +
            "     */\n" +
            "    `c_foo`() {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }

    @Disabled @Test fun shouldGenerateProductionWithPlus() {
        val production = Production(0, "c+foo", listOf(), VariableExpression("bar"))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : c+foo:\n" +
            "     * <bar>\n" +
            "     */\n" +
            "    `c_foo`() {\n" +
            "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
            "    }\n"))
    }


    @Test fun shouldGenerateAlternativeReferencesProduction() {
        val ref1 = Production(1, "ref1", listOf(), codePoint('x'))
        val ref2 = Production(2, "ref2", listOf(), codePoint('y'))
        val foo = production(alt(ref(ref1), ref(ref2)))

        val written = generate(foo, ref1, ref2)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * [->ref1 |\n" +
            "     *    ->ref2]\n" +
            "     */\n" +
            "    `foo`(`ref1` or `ref2`),\n" +
            "\n" +
            "    /**\n" +
            "     * `1` : ref1:\n" +
            "     * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            "     */\n" +
            "    `ref1`('x'),\n" +
            "\n" +
            "    /**\n" +
            "     * `2` : ref2:\n" +
            "     * <[y][LATIN SMALL LETTER Y][0x79]>\n" +
            "     */\n" +
            "    `ref2`('y'),\n"))
    }


    @Test fun shouldGenerateAlternativeCodePointsProduction() {
        val production = production(alt(codePoint('a'), codePoint('b')))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * [<[a][LATIN SMALL LETTER A][0x61]> |\n" +
            "     *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            "     */\n" +
            "    `foo`('a' or 'b'),\n"))
    }

    @Test fun shouldGenerateMixedAlternativesProduction() {
        val bar = Production(1, "bar", listOf(), codePoint('x'))
        val foo = production(alt(ref(bar), codePoint('b')))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * [->bar |\n" +
            "     *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            "     */\n" +
            "    `foo`(`bar` or 'b'),\n" +
            "\n" +
            "    /**\n" +
            "     * `1` : bar:\n" +
            "     * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            "     */\n" +
            "    `bar`('x'),\n"))
    }

    @Test fun shouldGenerateAlternativeOfCodePointOrRangeProduction() {
        val production = production(alt(codePoint('\t'), range(CodePoint.of(' '), CodePoint.of(0x10ffff))))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * [<[\\t][CHARACTER TABULATION][0x9]> |\n" +
            "     *    [<[ ][SPACE][0x20]>-<[\\uDBFF\\uDFFF][?][0x10ffff]>]]\n" +
            "     */\n" +
            "    `foo`('\\t' or (' '..\"\\uDBFF\\uDFFF\")),\n"))
    }

    @Test fun shouldGenerateAlternativeOfSequenceAndReferenceProduction() {
        val a = Production(1, "a", listOf(), codePoint('a'))
        val b = Production(2, "b", listOf(), codePoint('b'))
        val foo = production(alt(seq(ref(a), ref(b)), ref(a), ref(b)))

        val written = generate(foo, a, b)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * [->a + ->b |\n" +
            "     *    ->a |\n" +
            "     *    ->b]\n" +
            "     */\n" +
            "    `foo`((`a` + `b`) or `a` or `b`),\n" +
            "\n" +
            "    /**\n" +
            "     * `1` : a:\n" +
            "     * <[a][LATIN SMALL LETTER A][0x61]>\n" +
            "     */\n" +
            "    `a`('a'),\n" +
            "\n" +
            "    /**\n" +
            "     * `2` : b:\n" +
            "     * <[b][LATIN SMALL LETTER B][0x62]>\n" +
            "     */\n" +
            "    `b`('b'),\n"))
    }

    @Test fun shouldGenerateMinusRefProduction() {
        val production = production(MinusExpression(codePoint('a')).minus(codePoint('b')).minus(codePoint('c')))

        val written = generate(production)

        assertThat(written).isEqualTo(enumSource("\n" +
            "    /**\n" +
            "     * `0` : foo:\n" +
            "     * (<[a][LATIN SMALL LETTER A][0x61]> - <[b][LATIN SMALL LETTER B][0x62]> - <[c][LATIN SMALL LETTER C][0x63]>)\n" +
            "     */\n" +
            "    `foo`('a' - 'b' - 'c'),\n"))
    }

    private fun production(expression: Expression) = Production(0, "foo", listOf(), expression)

    private fun codePoint(char: Char) = codePoint(CodePoint.of(char))
    private fun codePoint(codePoint: CodePoint) = CodePointExpression(codePoint)

    private fun range(min: Char, max: Char) = range(CodePoint.of(min), CodePoint.of(max))
    private fun range(min: CodePoint, max: CodePoint) = RangeExpression(codePoint(min), codePoint(max))
    private fun ref(ref: Production) = ReferenceExpression(ref.key)

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

    private fun switch(vararg pairs: Pair<String, Expression>): SwitchExpression {
        val out = SwitchExpression()
        for (pair in pairs)
            out.addCase(VariableExpression(pair.first)).merge(pair.second)
        return out
    }


    private fun generate(vararg productions: Production): String {
        val writer = StringWriter()

        val spec = Spec(asList(*productions))
        val generator = YamlSymbolGenerator(spec)
        generator.generateCode("FooParser", writer)

        return writer.toString()
    }

    private fun enumSource(body: String): String {
        return YamlSymbolGenerator.PREFIX +
            ENUM_CLASS +
            body +
            YamlSymbolGenerator.SUFFIX
    }

    private fun factoryFunSource(body: String): String {
        return YamlSymbolGenerator.PREFIX +
            ENUM_CLASS +
            YamlSymbolGenerator.SUFFIX +
            body
    }

    companion object {
        private const val ENUM_CLASS = "enum class FooParser(private val token: Token) : Token {\n"
    }
}
