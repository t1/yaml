package spec.generator

import com.github.t1.yaml.tools.CodePoint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.EqualsExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression
import spec.generator.Expression.VariableExpression
import spec.generator.YamlSymbolGenerator.Companion.HEADER
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
            "/**\n" +
            " * `0` : foo:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x')\n"))
    }

    @Test fun shouldGenerateBigCodePointProduction() {
        val production = production(CodePointExpression(CodePoint.of(0x10428)))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * <[\\uD801\\uDC28][DESERET SMALL LETTER LONG I][0x10428]>\n" +
            " */\n" +
            "val `foo` = token(\"foo\", \"\\uD801\\uDC28\")\n"))
    }

    @Test fun shouldGenerateThreeCodePointSequenceProduction() {
        val production = production(seq(codePoint('a'), codePoint('b'), codePoint('c')))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * <[a][LATIN SMALL LETTER A][0x61]> + <[b][LATIN SMALL LETTER B][0x62]> + <[c][LATIN SMALL LETTER C][0x63]>\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'a' + 'b' + 'c')\n"))
    }

    @Test fun shouldGenerateRefProduction() {
        val bar = Production(1, "bar", listOf(), codePoint('x'))
        val foo = production(ref(bar))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "// 0: foo -> [1]\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `bar` = token(\"bar\", 'x')\n" +
            "\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * ->bar\n" +
            " */\n" +
            "val `foo` = token(\"foo\", `bar`)\n"))
    }

    @Test fun shouldGenerateRecursiveRefProduction() {
        val production = Production(0, "foo", listOf("n"), ReferenceExpression("foo", listOf("n" to "n")))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->foo(n)\n" +
            " */\n" +
            "fun `foo`(n: Int): Token = `foo`(n)\n")) // Kotlin needs the explicit return type here
    }

    @Test fun shouldGenerateCodePointRangeProduction() {
        val production = production(range('0', '9'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * [<[0][DIGIT ZERO][0x30]>-<[9][DIGIT NINE][0x39]>]\n" +
            " */\n" +
            "val `foo` = token(\"foo\", '0'..'9')\n"))
    }

    @Test fun shouldGenerateRepeatProduction() {
        val production = production(RepeatedExpression(codePoint('x'), "4"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × 4)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * 4)\n"))
    }

    @Test fun shouldGenerateSwitchProduction() {
        val x = Production(1, "x", listOf(), codePoint('x'))
        val y = Production(2, "y", listOf(), codePoint('y'))
        val z = Production(3, "z", listOf(), codePoint('z'))
        val foo = Production(0, "foo", listOf("c"), switch(
            eq(variable("c"), ref(x)) to codePoint('1'),
            eq(variable("c"), ref(y)) to codePoint('2'),
            eq(variable("c"), ref(z)) to codePoint('3')
        ))

        val written = generate(foo, x, y, z)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(c):\n" +
            " * <c> = ->x ⇒ <[1][DIGIT ONE][0x31]>\n" +
            " * <c> = ->y ⇒ <[2][DIGIT TWO][0x32]>\n" +
            " * <c> = ->z ⇒ <[3][DIGIT THREE][0x33]>\n" +
            " */\n" +
            "fun `foo`(c: InOutMode) = when (c) {\n" +
            "    `x` -> '1' named \"foo(\$c)\"\n" +
            "    `y` -> '2' named \"foo(\$c)\"\n" +
            "    `z` -> '3' named \"foo(\$c)\"\n" +
            "    else -> error(\"unexpected `c` value `\$c`\")\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * `1` : x:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `x` = token(\"x\", 'x')\n" +
            "\n" +
            "/**\n" +
            " * `2` : y:\n" +
            " * <[y][LATIN SMALL LETTER Y][0x79]>\n" +
            " */\n" +
            "val `y` = token(\"y\", 'y')\n" +
            "\n" +
            "/**\n" +
            " * `3` : z:\n" +
            " * <[z][LATIN SMALL LETTER Z][0x7a]>\n" +
            " */\n" +
            "val `z` = token(\"z\", 'z')\n"))
    }

    @Test fun shouldGenerateSwitchProductionToRefWithArgs() {
        val x = Production(1, "x", listOf(), codePoint('x'))
        val y = Production(2, "y", listOf(), codePoint('y'))
        val z = Production(3, "z", listOf(), codePoint('z'))
        val bar = Production(4, "bar", listOf("n"), RepeatedExpression(codePoint('x'), "n"))
        val foo = Production(0, "foo", listOf("n", "c"), switch(
            eq(variable("d"), ref(x)) to ref(bar),
            eq(variable("d"), ref(y)) to ref(bar),
            eq(variable("d"), ref(z)) to ref(bar)
        ))

        val written = generate(foo, x, y, z, bar)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(n,c):\n" +
            " * <d> = ->x ⇒ ->bar(n)\n" +
            " * <d> = ->y ⇒ ->bar(n)\n" +
            " * <d> = ->z ⇒ ->bar(n)\n" +
            " */\n" +
            "fun `foo`(n: Int, c: InOutMode) = when (d) {\n" +
            "    `x` -> `bar`(n) named \"foo(\$d)\"\n" +
            "    `y` -> `bar`(n) named \"foo(\$d)\"\n" +
            "    `z` -> `bar`(n) named \"foo(\$d)\"\n" +
            "    else -> error(\"unexpected `d` value `\$d`\")\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * `1` : x:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `x` = token(\"x\", 'x')\n" +
            "\n" +
            "/**\n" +
            " * `2` : y:\n" +
            " * <[y][LATIN SMALL LETTER Y][0x79]>\n" +
            " */\n" +
            "val `y` = token(\"y\", 'y')\n" +
            "\n" +
            "/**\n" +
            " * `3` : z:\n" +
            " * <[z][LATIN SMALL LETTER Z][0x7a]>\n" +
            " */\n" +
            "val `z` = token(\"z\", 'z')\n" +
            "\n" +
            "/**\n" +
            " * `4` : bar(n):\n" +
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

        assertThat(written).isEqualTo(source("\n" +
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
        val production = Production(0, "foo<", listOf("n"), RepeatedExpression(codePoint('x'), "m", "Where m < n"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo<(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × m /* Where m < n */)\n" +
            " */\n" +
            "fun `foo≪`(n: Int) = token(\"foo<(n)\") { reader ->\n" +
            "    val match = reader.mark { reader.readWhile { reader -> 'x'.match(reader).codePoints } }\n" +
            "    if (match.size >= n) return@token Match(matches = false)\n" +
            "    reader.expect(match)\n" +
            "    return@token Match(matches = true, codePoints = match)\n" +
            "}\n"))
    }

    @Test fun shouldGenerateProductionWithLessEqArg() {
        val production = Production(0, "foo≤", listOf("n"), RepeatedExpression(codePoint('x'), "m", "Where m ≤ n"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo≤(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × m /* Where m ≤ n */)\n" +
            " */\n" +
            "fun `foo≤`(n: Int) = token(\"foo≤(n)\") { reader ->\n" +
            "    val match = reader.mark { reader.readWhile { reader -> 'x'.match(reader).codePoints } }\n" +
            "    if (match.size > n) return@token Match(matches = false)\n" +
            "    reader.expect(match)\n" +
            "    return@token Match(matches = true, codePoints = match)\n" +
            "}\n"))
    }

    @Test fun shouldGenerateProductionWithTwoArgs() {
        val production = Production(0, "foo", listOf("c", "n"), RepeatedExpression(codePoint('x'), "n"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
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

        assertThat(written).isEqualTo(source("" +
            "\n" +
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

    @Test fun shouldGenerateReferenceToProductionWithLessThanArg() {
        val bar = Production(1, "bar<", listOf("n"), RepeatedExpression(codePoint('x'), "m", "Where m < n"))
        val foo = Production(0, "foo", listOf("n"), ref(bar))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar<(n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = `bar≪`(n)\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar<(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × m /* Where m < n */)\n" +
            " */\n" +
            "fun `bar≪`(n: Int) = token(\"bar<(n)\") { reader ->\n" +
            "    val match = reader.mark { reader.readWhile { reader -> 'x'.match(reader).codePoints } }\n" +
            "    if (match.size >= n) return@token Match(matches = false)\n" +
            "    reader.expect(match)\n" +
            "    return@token Match(matches = true, codePoints = match)\n" +
            "}\n"))
    }

    @Test fun shouldGenerateProductionWithMinus() {
        val production = Production(0, "c-foo", listOf(), codePoint('x'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : c-foo:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `c-foo` = token(\"c-foo\", 'x')\n"))
    }

    @Test fun shouldGenerateProductionWithPlus() {
        val production = Production(0, "c+foo", listOf(), codePoint('x'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : c+foo:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `c+foo` = token(\"c+foo\", 'x')\n"))
    }


    @Test fun shouldGenerateAlternativeReferencesProduction() {
        val ref1 = Production(1, "ref1", listOf(), codePoint('x'))
        val ref2 = Production(2, "ref2", listOf(), codePoint('y'))
        val foo = production(alt(ref(ref1), ref(ref2)))

        val written = generate(foo, ref1, ref2)

        assertThat(written).isEqualTo(source("\n" +
            "// 0: foo -> [1, 2]\n" +
            "\n" +
            "/**\n" +
            " * `1` : ref1:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `ref1` = token(\"ref1\", 'x')\n" +
            "\n" +
            "/**\n" +
            " * `2` : ref2:\n" +
            " * <[y][LATIN SMALL LETTER Y][0x79]>\n" +
            " */\n" +
            "val `ref2` = token(\"ref2\", 'y')\n" +
            "\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * [->ref1 |\n" +
            " *    ->ref2]\n" +
            " */\n" +
            "val `foo` = token(\"foo\", `ref1` or `ref2`)\n"
        ))
    }


    @Test fun shouldGenerateAlternativeCodePointsProduction() {
        val production = production(alt(codePoint('a'), codePoint('b')))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * [<[a][LATIN SMALL LETTER A][0x61]> |\n" +
            " *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'a' or 'b')\n"))
    }

    @Test fun shouldGenerateMixedAlternativesProduction() {
        val bar = Production(1, "bar", listOf(), codePoint('x'))
        val foo = production(alt(ref(bar), codePoint('b')))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "// 0: foo -> [1]\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `bar` = token(\"bar\", 'x')\n" +
            "\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * [->bar |\n" +
            " *    <[b][LATIN SMALL LETTER B][0x62]>]\n" +
            " */\n" +
            "val `foo` = token(\"foo\", `bar` or 'b')\n"))
    }

    @Test fun shouldGenerateAlternativeOfCodePointOrRangeProduction() {
        val production = production(alt(codePoint('\t'), range(CodePoint.of(' '), CodePoint.of(0x10ffff))))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * [<[\\t][CHARACTER TABULATION][0x9]> |\n" +
            " *    [<[ ][SPACE][0x20]>-<[\\uDBFF\\uDFFF][?][0x10ffff]>]]\n" +
            " */\n" +
            "val `foo` = token(\"foo\", '\\t' or ' '..\"\\uDBFF\\uDFFF\")\n"))
    }

    @Test fun shouldGenerateAlternativeOfSequenceAndReferenceProduction() {
        val a = Production(1, "a", listOf(), codePoint('a'))
        val b = Production(2, "b", listOf(), codePoint('b'))
        val foo = production(alt(seq(ref(a), ref(b)), ref(a), ref(b)))

        val written = generate(foo, a, b)

        assertThat(written).isEqualTo(source("\n" +
            "// 0: foo -> [1, 2]\n" +
            "\n" +
            "/**\n" +
            " * `1` : a:\n" +
            " * <[a][LATIN SMALL LETTER A][0x61]>\n" +
            " */\n" +
            "val `a` = token(\"a\", 'a')\n" +
            "\n" +
            "/**\n" +
            " * `2` : b:\n" +
            " * <[b][LATIN SMALL LETTER B][0x62]>\n" +
            " */\n" +
            "val `b` = token(\"b\", 'b')\n" +
            "\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * [->a + ->b |\n" +
            " *    ->a |\n" +
            " *    ->b]\n" +
            " */\n" +
            "val `foo` = token(\"foo\", (`a` + `b`) or `a` or `b`)\n"))
    }

    @Test fun shouldGenerateSequenceOfReferenceAndRepeatProduction() {
        val a = Production(1, "a", listOf(), codePoint('a'))
        val b = Production(2, "b", listOf(), codePoint('b'))
        val foo = production(seq(ref(a), RepeatedExpression(ref(b), "?")))

        val written = generate(foo, a, b)

        assertThat(written).isEqualTo(source("\n" +
            "// 0: foo -> [1, 2]\n" +
            "\n" +
            "/**\n" +
            " * `1` : a:\n" +
            " * <[a][LATIN SMALL LETTER A][0x61]>\n" +
            " */\n" +
            "val `a` = token(\"a\", 'a')\n" +
            "\n" +
            "/**\n" +
            " * `2` : b:\n" +
            " * <[b][LATIN SMALL LETTER B][0x62]>\n" +
            " */\n" +
            "val `b` = token(\"b\", 'b')\n" +
            "\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * ->a + (->b × ?)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", `a` + `b` * zero_or_once)\n"))
    }

    @Test fun shouldGenerateMinusRefProduction() {
        val production = production(MinusExpression(codePoint('a')).minus(codePoint('b')).minus(codePoint('c')))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[a][LATIN SMALL LETTER A][0x61]> - <[b][LATIN SMALL LETTER B][0x62]> - <[c][LATIN SMALL LETTER C][0x63]>)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'a' - 'b' - 'c')\n"))
    }

    @Test fun shouldGenerateEndOfFileRefProduction() {
        val production = production(ReferenceExpression("End of file"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * ->End of file\n" +
            " */\n" +
            "val `foo` = token(\"foo\", endOfFile)\n"))
    }

    @Test fun shouldGenerateEmptyRefProduction() {
        val production = production(ReferenceExpression("Empty"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * ->Empty\n" +
            " */\n" +
            "val `foo` = token(\"foo\", empty)\n"))
    }

    @Test fun shouldGenerateRepeatThreeTimesProduction() {
        val production = production(RepeatedExpression(codePoint('x'), "3"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × 3)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * 3)\n"))
    }

    @Test fun shouldGenerateRepeatZeroOrOneTimesProduction() {
        val production = production(RepeatedExpression(codePoint('x'), "?"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × ?)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * zero_or_once)\n"))
    }

    @Test fun shouldGenerateRepeatZeroOrMoreTimesProduction() {
        val production = production(RepeatedExpression(codePoint('x'), "*"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × *)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * zero_or_more)\n"))
    }

    @Test fun shouldGenerateRepeatOnceOrMoreTimesProduction() {
        val production = production(RepeatedExpression(codePoint('x'), "+"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × +)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * once_or_more)\n"))
    }

    @Test fun shouldGenerateSequenceWithRepeatTwiceProduction() {
        val production = production(seq(codePoint('x'), RepeatedExpression(codePoint('y'), "2")))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]> + (<[y][LATIN SMALL LETTER Y][0x79]> × 2)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' + 'y' * 2)\n"))
    }

    @Test fun shouldGenerateParameterizedSequenceWithRepeatTwiceProduction() {
        val production = Production(0, "foo", listOf("n"), seq(codePoint('x'), RepeatedExpression(codePoint('y'), "n")))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]> + (<[y][LATIN SMALL LETTER Y][0x79]> × n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = 'x' + 'y' * n\n"))
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

    private fun switch(vararg pairs: Pair<Expression, Expression>): SwitchExpression {
        val out = SwitchExpression()
        for (pair in pairs)
            out.addCase(pair.first).merge(pair.second)
        return out
    }

    private fun eq(left: Expression, right: Expression) = EqualsExpression(left, right)
    private fun variable(name: String) = VariableExpression(name)


    private fun generate(vararg productions: Production): String {
        val writer = StringWriter()

        val spec = Spec(asList(*productions))
        val generator = YamlSymbolGenerator(spec)
        generator.generateCode(writer)

        return writer.toString()
    }

    private fun source(body: String): String = "$HEADER$body"
}
