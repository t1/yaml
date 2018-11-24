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
    @Test fun `empty source`() {
        val written = generate()

        assertThat(written).isEqualTo(source(""))
    }

    @Test fun `simple code point`() {
        val production = production(codePoint('x'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x')\n"))
    }

    @Test fun `big code point`() {
        val production = production(CodePointExpression(CodePoint.of(0x10428)))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * <[\\uD801\\uDC28][DESERET SMALL LETTER LONG I][0x10428]>\n" +
            " */\n" +
            "val `foo` = token(\"foo\", \"\\uD801\\uDC28\")\n"))
    }

    @Test fun `three code point sequence`() {
        val production = production(seq(codePoint('a'), codePoint('b'), codePoint('c')))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * <[a][LATIN SMALL LETTER A][0x61]> + <[b][LATIN SMALL LETTER B][0x62]> + <[c][LATIN SMALL LETTER C][0x63]>\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'a' + 'b' + 'c')\n"))
    }

    @Test fun `code point range`() {
        val production = production(range('0', '9'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * [<[0][DIGIT ZERO][0x30]>-<[9][DIGIT NINE][0x39]>]\n" +
            " */\n" +
            "val `foo` = token(\"foo\", '0'..'9')\n"))
    }

    @Test fun `simple ref`() {
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

    @Test fun `recursive ref`() {
        val production = Production(0, "foo", listOf("n"), ReferenceExpression("foo", listOf("n" to VariableExpression("n"))))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->foo(n)\n" +
            " */\n" +
            "fun `foo`(n: Int) : Token = tokenGenerator(\"foo\") { `foo`(n) }\n"))
    }

    @Test fun `ref calling ref`() {
        val bar = Production(1, "bar", listOf("n"), codePoint('x') * 'n')
        val foo = Production(0, "foo", listOf("n"), ref(bar))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar(n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { `bar`(n) }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `bar`(n: Int) = 'x' * n\n"))
    }

    @Test fun `switch constants to code points`() {
        val foo = Production(0, "foo", listOf("c"), switch(
            eq(variable("c"), ReferenceExpression("flow-in")) to codePoint('1'),
            eq(variable("c"), ReferenceExpression("flow-key")) to codePoint('2'),
            eq(variable("c"), ReferenceExpression("flow-out")) to codePoint('3')
        ))

        val written = generate(foo)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(c):\n" +
            " * <c> = ->flow-in ⇒ <[1][DIGIT ONE][0x31]>\n" +
            " * <c> = ->flow-key ⇒ <[2][DIGIT TWO][0x32]>\n" +
            " * <c> = ->flow-out ⇒ <[3][DIGIT THREE][0x33]>\n" +
            " */\n" +
            "fun `foo`(c: InOutMode) = when (c) {\n" +
            "    `flow-in` -> '1' named \"foo(\$c)\"\n" +
            "    `flow-key` -> '2' named \"foo(\$c)\"\n" +
            "    `flow-out` -> '3' named \"foo(\$c)\"\n" +
            "    else -> error(\"unexpected `c` value `\$c`\")\n" +
            "}\n"))
    }

    @Test fun `switch constants to ref with args`() {
        val bar = Production(1, "bar", listOf("n"), codePoint('x') * 'n')
        val foo = Production(0, "foo", listOf("n", "c"), switch(
            eq(variable("d"), ReferenceExpression("flow-in")) to ref(bar),
            eq(variable("d"), ReferenceExpression("flow-key")) to ref(bar),
            eq(variable("d"), ReferenceExpression("flow-out")) to ref(bar)
        ))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(n,c):\n" +
            " * <d> = ->flow-in ⇒ ->bar(n)\n" +
            " * <d> = ->flow-key ⇒ ->bar(n)\n" +
            " * <d> = ->flow-out ⇒ ->bar(n)\n" +
            " */\n" +
            "fun `foo`(n: Int, c: InOutMode) = tokenGenerator(\"foo\") { when (d) {\n" +
            "    `flow-in` -> `bar`(n) named \"foo(\$d)\"\n" +
            "    `flow-key` -> `bar`(n) named \"foo(\$d)\"\n" +
            "    `flow-out` -> `bar`(n) named \"foo(\$d)\"\n" +
            "    else -> error(\"unexpected `d` value `\$d`\")\n" +
            "} }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `bar`(n: Int) = 'x' * n\n"))
    }

    @Test fun `with one arg`() {
        val production = Production(0, "foo", listOf("n"), codePoint('x') * 'n')

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = 'x' * n\n"))
    }

    @Test fun `with less-then arg`() {
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

    @Test fun `with less-then-or-equal arg`() {
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

    @Test fun `with two args`() {
        val production = Production(0, "foo", listOf("c", "n"), codePoint('x') * 'n')

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(c,n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `foo`(c: InOutMode, n: Int) = 'x' * n\n"))
    }

    @Test fun `ref with args`() {
        val bar = Production(1, "bar", listOf("n"), codePoint('x') * 'n')
        val foo = Production(0, "foo", listOf("n"), ref(bar))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar(n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { `bar`(n) }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `bar`(n: Int) = 'x' * n\n"))
    }

    @Test fun `ref to production with less-than arg`() {
        val bar = Production(1, "bar<", listOf("n"), RepeatedExpression(codePoint('x'), "m", "Where m < n"))
        val foo = Production(0, "foo", listOf("n"), ref(bar))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar<(n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { `bar≪`(n) }\n" +
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

    @Test fun `ref arg with fun result`() {
        val baz = Production(2, "baz", listOf("n"), codePoint('x') * 'n')
        val bar = Production(1, "bar", listOf("n"), ref(baz))
        val foo = Production(0, "foo", listOf("n"), ReferenceExpression(bar.name, listOf("n" to ref(baz))))

        val written = generate(foo, bar, baz)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar(n = ->baz(n))\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { `bar`(`baz`(n)) }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * ->baz(n)\n" +
            " */\n" +
            "fun `bar`(n: Int) = tokenGenerator(\"bar\") { `baz`(n) }\n" +
            "\n" +
            "/**\n" +
            " * `2` : baz(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `baz`(n: Int) = 'x' * n\n"))
    }

    @Test fun `repeated ref`() {
        val bar = Production(1, "bar", listOf(), codePoint('x'))
        val foo = Production(0, "foo", listOf("n"), ref(bar) * 'n')

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * (->bar × n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = `bar` * n\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `bar` = token(\"bar\", 'x')\n"))
    }

    @Test fun `repeated fun ref`() {
        val bar = Production(1, "bar", listOf("n"), codePoint('x') * 'n')
        val foo = Production(0, "foo", listOf("n"), ref(bar) * '2')

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * (->bar(n) × 2)\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { `bar`(n) * 2 }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `bar`(n: Int) = 'x' * n\n"))
    }

    @Test fun `alternative with fun ref`() {
        val bar = Production(1, "bar", listOf("n"), codePoint('x') * 'n')
        val foo = Production(0, "foo", listOf("n"), alt(codePoint('x'), ref(bar)))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("" +
            "\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * [<[x][LATIN SMALL LETTER X][0x78]> |\n" +
            " *    ->bar(n)]\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { 'x' or `bar`(n) }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × n)\n" +
            " */\n" +
            "fun `bar`(n: Int) = 'x' * n\n"))
    }

    @Test fun `non-fun ref`() {
        val bar = Production(1, "bar", listOf(), codePoint('x'))
        val foo = Production(0, "foo", listOf("n"), ref(bar) * 'n')

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * (->bar × n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = `bar` * n\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `bar` = token(\"bar\", 'x')\n"))
    }

    @Test fun `with minus in name`() {
        val production = Production(0, "c-foo", listOf(), codePoint('x'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : c-foo:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `c-foo` = token(\"c-foo\", 'x')\n"))
    }

    @Test fun `with plus in name`() {
        val production = Production(0, "c+foo", listOf(), codePoint('x'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : c+foo:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]>\n" +
            " */\n" +
            "val `c+foo` = token(\"c+foo\", 'x')\n"))
    }


    @Test fun `alternative references`() {
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


    @Test fun `alternative code points`() {
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

    @Test fun `alternative of ref or code point`() {
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

    @Test fun `alternative of code point or range`() {
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

    @Test fun `alternative of sequence and reference or references`() {
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

    @Test fun `sequence of reference and repeat`() {
        val a = Production(1, "a", listOf(), codePoint('a'))
        val b = Production(2, "b", listOf(), codePoint('b'))
        val foo = production(seq(ref(a), ref(b) * '?'))

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

    @Test fun `code point minus minus`() {
        val production = production(MinusExpression(codePoint('a')).minus(codePoint('b')).minus(codePoint('c')))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[a][LATIN SMALL LETTER A][0x61]> - <[b][LATIN SMALL LETTER B][0x62]> - <[c][LATIN SMALL LETTER C][0x63]>)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'a' - 'b' - 'c')\n"))
    }

    @Test fun `end of file ref`() {
        val production = production(ReferenceExpression("End of file"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * ->End of file\n" +
            " */\n" +
            "val `foo` = token(\"foo\", endOfFile)\n"))
    }

    @Test fun `empty ref`() {
        val production = production(ReferenceExpression("Empty"))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * ->Empty\n" +
            " */\n" +
            "val `foo` = token(\"foo\", empty)\n"))
    }

    @Test fun `repeat three times`() {
        val production = production(codePoint('x') * '3')

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × 3)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * 3)\n"))
    }

    @Test fun `repeat zero or one times`() {
        val production = production(codePoint('x') * '?')

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × ?)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * zero_or_once)\n"))
    }

    @Test fun `repeat zero or more times`() {
        val production = production(codePoint('x') * '*')

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × *)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * zero_or_more)\n"))
    }

    @Test fun `repeat once or more times`() {
        val production = production(codePoint('x') * '+')

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × +)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' * once_or_more)\n"))
    }

    @Test fun `sequence with repeat twice`() {
        val production = production(seq(codePoint('x'), codePoint('y') * '2'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]> + (<[y][LATIN SMALL LETTER Y][0x79]> × 2)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", 'x' + 'y' * 2)\n"))
    }

    @Test fun `parameterized sequence with repeat twice`() {
        val production = Production(0, "foo", listOf("n"), seq(codePoint('x'), codePoint('y') * 'n'))

        val written = generate(production)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * <[x][LATIN SMALL LETTER X][0x78]> + (<[y][LATIN SMALL LETTER Y][0x79]> × n)\n" +
            " */\n" +
            "fun `foo`(n: Int) = 'x' + 'y' * n\n"))
    }

    @Test fun `reference with n+1 arg`() {
        val bar = Production(1, "bar", listOf("n"), codePoint('x') * '?')
        val foo = Production(0, "foo", listOf("n"), ReferenceExpression(bar.name, listOf("n" to VariableExpression("n+1"))))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar(n = <n+1>)\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { `bar`(n + 1) }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × ?)\n" +
            " */\n" +
            "fun `bar`(n: Int) = 'x' * zero_or_once\n"))
    }

    @Test fun `reference with n-a arg`() {
        val bar = Production(1, "bar", listOf("n"), codePoint('x') * '?')
        val foo = Production(0, "foo", listOf("n"), ReferenceExpression(bar.name, listOf("n" to VariableExpression("n/a"))))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar(n = <n/a>)\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { `bar`(-1) }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × ?)\n" +
            " */\n" +
            "fun `bar`(n: Int) = 'x' * zero_or_once\n"))
    }

    @Test fun `reference with n-1 arg`() {
        val bar = Production(1, "bar", listOf("n"), codePoint('x') * '?')
        val foo = Production(0, "foo", listOf("n"), ReferenceExpression(bar.name, listOf("n" to VariableExpression("n-1"))))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "/**\n" +
            " * `0` : foo(n):\n" +
            " * ->bar(n = <n-1>)\n" +
            " */\n" +
            "fun `foo`(n: Int) = tokenGenerator(\"foo\") { `bar`(n - 1) }\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(n):\n" +
            " * (<[x][LATIN SMALL LETTER X][0x78]> × ?)\n" +
            " */\n" +
            "fun `bar`(n: Int) = 'x' * zero_or_once\n"))
    }

    @Test fun `reference with flow-in arg`() {
        val bar = Production(1, "bar", listOf("c"), switch(
            eq(variable("c"), ReferenceExpression("flow-in")) to ReferenceExpression("flow-out"),
            eq(variable("c"), ReferenceExpression("flow-out")) to ReferenceExpression("flow-in")))
        val foo = Production(0, "foo", listOf(), ReferenceExpression(bar.name, listOf("c" to ReferenceExpression("flow-in"))))

        val written = generate(foo, bar)

        assertThat(written).isEqualTo(source("\n" +
            "// 0: foo -> [1]\n" +
            "\n" +
            "/**\n" +
            " * `1` : bar(c):\n" +
            " * <c> = ->flow-in ⇒ ->flow-out\n" +
            " * <c> = ->flow-out ⇒ ->flow-in\n" +
            " */\n" +
            "fun `bar`(c: InOutMode) = when (c) {\n" +
            "    `flow-in` -> `flow-out`\n" +
            "    `flow-out` -> `flow-in`\n" +
            "    else -> error(\"unexpected `c` value `\$c`\")\n" +
            "}\n" +
            "\n" +
            "/**\n" +
            " * `0` : foo:\n" +
            " * ->bar(c = ->flow-in)\n" +
            " */\n" +
            "val `foo` = token(\"foo\", `bar`(`flow-in`))\n"))
    }


    private fun production(expression: Expression) = Production(0, "foo", listOf(), expression)

    private fun codePoint(char: Char) = codePoint(CodePoint.of(char))
    private fun codePoint(codePoint: CodePoint) = CodePointExpression(codePoint)

    private operator fun Expression.times(repetitions: Char) = RepeatedExpression(this, repetitions.toString())

    private fun range(min: Char, max: Char) = range(CodePoint.of(min), CodePoint.of(max))
    private fun range(min: CodePoint, max: CodePoint) = RangeExpression(codePoint(min), codePoint(max))
    private fun ref(ref: Production) = ReferenceExpression(ref.name, ref.args.map { it to VariableExpression(it) })

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
        val out = SwitchExpression.of(pairs[0].first, pairs[0].second)
        for (i in 1 until pairs.size)
            with(pairs[i]) { out.addCase(first).merge(second) }
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
