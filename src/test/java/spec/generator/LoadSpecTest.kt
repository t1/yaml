package spec.generator

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.contentOf
import org.assertj.core.api.SoftAssertions
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

class LoadSpecTest {
    @Test fun shouldParseFullSpec() {
        val spec = SpecLoader().load()

        val actual = StringBuilder()
        for (production in spec.productions)
            actual.append(production).append("\n\n")

        // not softly, so we get a diff from AssertJ
        assertThat(actual.toString()).isEqualTo(contentOf(LoadSpecTest::class.java.getResource("expected.txt")))

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(spec["c-printable"]).isEqualTo(spec.productions[0])
            softly.assertThat(spec["c-sequence-end"]).isEqualTo(spec.productions[8])
            softly.assertThat(spec["s-indent(<n)"]).isEqualTo(spec.productions[63])
            softly.assertThat(spec["ns-flow-node(n,c)"]).isEqualTo(spec.productions[160])

            softly.assertThat(spec["c-printable"].references).isEmpty()
            val bCharRefs = spec["b-char"].references
            softly.assertThat(bCharRefs.keys).containsOnly("b-line-feed", "b-carriage-return")
            softly.assertThat(bCharRefs.values).containsOnly(
                spec["b-line-feed"],
                spec["b-carriage-return"]
            )
            val lEmptyRefs = spec["l-empty(n,c)"].references
            softly.assertThat(lEmptyRefs.keys).containsOnly("s-line-prefix(n,c)", "s-indent(n)", "b-as-line-feed")
            softly.assertThat(lEmptyRefs.values).containsOnly(
                spec["s-line-prefix(n,c)"],
                spec["s-indent(n)"],
                spec["b-as-line-feed"]
            )
        }
    }

    private fun parse(counter: Int, name: String, expression: String): Production {
        val element = Jsoup.parse("<html><head></head><body>" +
            "<table>\n" +
            "  <tr>\n" +
            "    <td class=\"productioncounter\">[" + counter + "]</td> \n" +
            "    <td class=\"productionlhs\"><a id=\"" + name + "\"></a>" + name.replace("<", "&lt;") + "</td> \n" +
            "    <td class=\"productionrhs\"> " + expression + " </td> \n" +
            "  </tr>\n" +
            "</table>" +
            "</body></html>").selectFirst("tr")

        return SpecLoader().parse(element)
    }

    @Test fun shouldParseAlternatives() {
        val production = parse(1, "c-printable",
            "&nbsp;&nbsp;#x9 | #xA | #xD | [#x20-#x7E]&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/* 8 bit */<br>" +
                " | #x85 | [#xA0-#xD7FF] | [#xE000-#xFFFD] /* 16 bit */<br>" +
                " | [#x10000-#x10FFFF]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;/* 32 bit */")

        assertThat(production.toString()).isEqualTo("" +
            "Production(counter=1, name=c-printable, args=[], expression=[<[\\t][CHARACTER TABULATION][0x9]> ||\n" +
            "   <[\\n][LINE FEED (LF)][0xa]> ||\n" +
            "   <[\\r][CARRIAGE RETURN (CR)][0xd]> ||\n" +
            "   [<[ ][SPACE][0x20]>-<[~][TILDE][0x7e]>] ||\n" +
            "   <[\\u0085][NEXT LINE (NEL)][0x85]> ||\n" +
            "   [<[ ][NO-BREAK SPACE][0xa0]>-<[\uD7FF][?][0xd7ff]>] ||\n" +
            "   [<[\uE000][PRIVATE USE AREA E000][0xe000]>-<[�][REPLACEMENT CHARACTER][0xfffd]>] ||\n" +
            "   [<[\\uD800\\uDC00][LINEAR B SYLLABLE B008 A][0x10000]>-<[\\uDBFF\\uDFFF][?][0x10ffff]>]])")
    }

    @Test fun shouldParseHex() {
        val production = parse(3, "c-byte-order-mark", "#xFEFF")

        assertThat(production.toString()).isEqualTo("Production(counter=3, name=c-byte-order-mark, args=[], expression=<[\\uFEFF][ZERO WIDTH NO-BREAK SPACE][0xfeff]>)")
    }

    @Test fun shouldParseQuote() {
        val production = parse(4, "c-sequence-entry", "“<span class=\"quote\">-</span>”")

        assertThat(production.toString()).isEqualTo("Production(counter=4, name=c-sequence-entry, args=[], expression=<[-][HYPHEN-MINUS][0x2d]>)")
    }

    @Test fun shouldParseHref() {
        val production = parse(0, "foo", "<a href=\"#bar\">bar</a>")

        assertThat(production.toString()).isEqualTo("Production(counter=0, name=foo, args=[], expression=->bar)")
    }

    @Test fun shouldParseMinus() {
        val production = parse(27, "nb-char",
            "<a href=\"#c-printable\">c-printable</a> - <a href=\"#b-char\">b-char</a> - <a href=\"#c-byte-order-mark\">c-byte-order-mark</a>")

        assertThat(production.toString()).isEqualTo("Production(counter=27, name=nb-char, args=[], expression=->c-printable - ->b-char - ->c-byte-order-mark)")
    }

    @Test fun shouldParseMultiplication() {
        val production = parse(59, "ns-esc-8-bit",
            "“<span class=\"quote\">x</span>”<br> ( <a href=\"#ns-hex-digit\">ns-hex-digit</a> × 2 )")

        assertThat(production.toString()).isEqualTo("Production(counter=59, name=ns-esc-8-bit, args=[], expression=<[x][LATIN SMALL LETTER X][0x78]> + (->ns-hex-digit × 2))")
    }

    @Test fun shouldParsePlus() {
        val production = parse(66, "s-separate-in-line",
            "<a href=\"#s-white\">s-white</a>+ | /* Start of line */")

        assertThat(production.toString()).isEqualTo("Production(counter=66, name=s-separate-in-line, args=[], expression=(->s-white × +))")
    }

    @Test fun shouldParseOptional() {
        val production = parse(69, "s-flow-line-prefix(n)",
            "<a href=\"#s-indent(n)\">s-indent(n)</a> <a href=\"#s-separate-in-line\">s-separate-in-line</a>?")

        assertThat(production.toString()).isEqualTo("Production(counter=69, name=s-flow-line-prefix, args=[n], expression=->s-indent(n) + (->s-separate-in-line × ?))")
    }

    @Test fun shouldParseMulti() {
        val production = parse(75, "c-nb-comment-text",
            "<a href=\"#c-comment\">“<span class=\"quote\">#</span>”</a> <a href=\"#nb-char\">nb-char</a>*")

        assertThat(production.toString()).isEqualTo("Production(counter=75, name=c-nb-comment-text, args=[], expression=->c-comment + (->nb-char × *))")
    }

    @Test fun shouldParseParentheses() {
        val production = parse(0, "foo", "<a href=\"#bar\">bar</a> | ( <a href=\"#baz\">baz</a></a> )")

        assertThat(production.toString()).isEqualTo("Production(counter=0, name=foo, args=[], expression=[->bar ||\n" +
            "   ->baz])")
    }

    @Test fun shouldParseEmptyAlternativeBeforeEnd() {
        val production = parse(76, "b-comment",
            "<a href=\"#b-non-content\">b-non-content</a> | /* End of file */")

        assertThat(production.toString()).isEqualTo("Production(counter=76, name=b-comment, args=[], expression=->b-non-content)")
    }

    @Test fun shouldParseEmptyAlternativeBeforeClosingParentheses() {
        val production = parse(79, "s-l-comments",
            "( <a href=\"#s-b-comment\">s-b-comment</a> | /* Start of line */ )<br> <a href=\"#l-comment\">l-comment</a>*")

        assertThat(production.toString()).isEqualTo("Production(counter=79, name=s-l-comments, args=[], expression=->s-b-comment + (->l-comment × *))")
    }

    @Test fun shouldParseMultiLineComment() {
        val production = parse(64, "s-indent(<n)",
            "<a href=\"#s-space\">s-space</a> × <code class=\"varname\">m</code> /* Where <code class=\"varname\">m</code> &lt; <code class=\"varname\">n</code> */")

        assertThat(production.toString()).isEqualTo("Production(counter=64, name=s-indent, args=[<n], expression=(->s-space × m /* Where m < n */))")
    }

    @Test fun shouldParseEmpty() {
        val production = parse(105, "e-scalar", "/* Empty */")

        assertThat(production.toString()).isEqualTo("Production(counter=105, name=e-scalar, args=[], expression=<empty>)")
    }

    @Test fun shouldParseSwitch() {
        val production = parse(67, "s-line-prefix(n,c)",
            "<code class=\"varname\">c</code> = block-out ⇒ <a href=\"#s-block-line-prefix(n)\">s-block-line-prefix(n)</a><br> <code class=\"varname\">c</code> = block-in&nbsp; ⇒ <a href=\"#s-block-line-prefix(n)\">s-block-line-prefix(n)</a><br> <code class=\"varname\">c</code> = flow-out&nbsp; ⇒ <a href=\"#s-flow-line-prefix(n)\">s-flow-line-prefix(n)</a><br> <code class=\"varname\">c</code> = flow-in&nbsp;&nbsp; ⇒ <a href=\"#s-flow-line-prefix(n)\">s-flow-line-prefix(n)</a>")

        assertThat(production.toString()).isEqualTo("Production(counter=67, name=s-line-prefix, args=[n, c], expression=<c = block-out> ⇒ <->s-block-line-prefix(n)>\n" +
            "  <c = block-in> ⇒ <->s-block-line-prefix(n)>\n" +
            "  <c = flow-out> ⇒ <->s-flow-line-prefix(n)>\n" +
            "  <c = flow-in> ⇒ <->s-flow-line-prefix(n)>)")
    }
}
