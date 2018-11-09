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

    @Test fun shouldParseHref() {
        val production = parse(0, "foo", "<a href=\"#bar\">bar</a>")

        assertThat(production.toString()).isEqualTo("`0` : foo:\n->bar")
    }

    @Test fun shouldParseParentheses() {
        val production = parse(0, "foo", "<a href=\"#bar\">bar</a> | ( <a href=\"#baz\">baz</a></a> )")

        assertThat(production.toString()).isEqualTo("`0` : foo:\n[->bar |\n   ->baz]")
    }

    @Test fun shouldParseAlternatives() {
        val production = parse(1, "c-printable",
            "&nbsp;&nbsp;#x9 | #xA | #xD | [#x20-#x7E]&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/* 8 bit */<br>" +
                " | #x85 | [#xA0-#xD7FF] | [#xE000-#xFFFD] /* 16 bit */<br>" +
                " | [#x10000-#x10FFFF]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;/* 32 bit */")

        assertThat(production.toString()).isEqualTo("" +
            "`1` : c-printable:\n" +
            "[<[\\t][CHARACTER TABULATION][0x9]> |\n" +
            "   <[\\n][LINE FEED (LF)][0xa]> |\n" +
            "   <[\\r][CARRIAGE RETURN (CR)][0xd]> |\n" +
            "   [<[ ][SPACE][0x20]>-<[~][TILDE][0x7e]>] |\n" +
            "   <[\\u0085][NEXT LINE (NEL)][0x85]> |\n" +
            "   [<[ ][NO-BREAK SPACE][0xa0]>-<[\uD7FF][?][0xd7ff]>] |\n" +
            "   [<[\uE000][PRIVATE USE AREA E000][0xe000]>-<[�][REPLACEMENT CHARACTER][0xfffd]>] |\n" +
            "   [<[\\uD800\\uDC00][LINEAR B SYLLABLE B008 A][0x10000]>-<[\\uDBFF\\uDFFF][?][0x10ffff]>]]")
    }

    @Test fun shouldParseHex() {
        val production = parse(3, "c-byte-order-mark", "#xFEFF")

        assertThat(production.toString()).isEqualTo("`3` : c-byte-order-mark:\n<[\\uFEFF][ZERO WIDTH NO-BREAK SPACE][0xfeff]>")
    }

    @Test fun shouldParseQuote() {
        val production = parse(4, "c-sequence-entry", "“<span class=\"quote\">-</span>”")

        assertThat(production.toString()).isEqualTo("`4` : c-sequence-entry:\n<[-][HYPHEN-MINUS][0x2d]>")
    }

    @Test fun shouldParseMinus() {
        val production = parse(27, "nb-char",
            "<a href=\"#c-printable\">c-printable</a> - <a href=\"#b-char\">b-char</a> - <a href=\"#c-byte-order-mark\">c-byte-order-mark</a>")

        assertThat(production.toString()).isEqualTo("`27` : nb-char:\n(->c-printable - ->b-char - ->c-byte-order-mark)")
    }

    @Test fun shouldParseMultiplication() {
        val production = parse(59, "ns-esc-8-bit",
            "“<span class=\"quote\">x</span>”<br> ( <a href=\"#ns-hex-digit\">ns-hex-digit</a> × 2 )")

        assertThat(production.toString()).isEqualTo("`59` : ns-esc-8-bit:\n<[x][LATIN SMALL LETTER X][0x78]> + (->ns-hex-digit × 2)")
    }

    @Test fun shouldParseSequenceWithAlternatives() {
        val production = parse(62, "c-ns-esc-char",
            " <a href=\"#c-escape\">“<span class=\"quote\">\\</span>”</a><br>" +
                " (&nbsp;<a href=\"#ns-esc-null\">ns-esc-null</a> | <a href=\"#ns-esc-bell\">ns-esc-bell</a> | <a href=\"#ns-esc-backspace\">ns-esc-backspace</a><br>" +
                " | <a href=\"#ns-esc-horizontal-tab\">ns-esc-horizontal-tab</a> | <a href=\"#ns-esc-line-feed\">ns-esc-line-feed</a><br>" +
                " | <a href=\"#ns-esc-vertical-tab\">ns-esc-vertical-tab</a> | <a href=\"#ns-esc-form-feed\">ns-esc-form-feed</a><br>" +
                " | <a href=\"#ns-esc-carriage-return\">ns-esc-carriage-return</a> | <a href=\"#ns-esc-escape\">ns-esc-escape</a> | <a href=\"#ns-esc-space\">ns-esc-space</a><br>" +
                " | <a href=\"#ns-esc-double-quote\">ns-esc-double-quote</a> | <a href=\"#ns-esc-slash\">ns-esc-slash</a> | <a href=\"#ns-esc-backslash\">ns-esc-backslash</a><br>" +
                " | <a href=\"#ns-esc-next-line\">ns-esc-next-line</a> | <a href=\"#ns-esc-non-breaking-space\">ns-esc-non-breaking-space</a><br>" +
                " | <a href=\"#ns-esc-line-separator\">ns-esc-line-separator</a> | <a href=\"#ns-esc-paragraph-separator\">ns-esc-paragraph-separator</a><br>" +
                " | <a href=\"#ns-esc-8-bit\">ns-esc-8-bit</a> | <a href=\"#ns-esc-16-bit\">ns-esc-16-bit</a> | <a href=\"#ns-esc-32-bit\">ns-esc-32-bit</a> )<br> ")

        assertThat(production.toString()).isEqualTo("`62` : c-ns-esc-char:\n" +
            "->c-escape + [->ns-esc-null |\n" +
            "   ->ns-esc-bell |\n" +
            "   ->ns-esc-backspace |\n" +
            "   ->ns-esc-horizontal-tab |\n" +
            "   ->ns-esc-line-feed |\n" +
            "   ->ns-esc-vertical-tab |\n" +
            "   ->ns-esc-form-feed |\n" +
            "   ->ns-esc-carriage-return |\n" +
            "   ->ns-esc-escape |\n" +
            "   ->ns-esc-space |\n" +
            "   ->ns-esc-double-quote |\n" +
            "   ->ns-esc-slash |\n" +
            "   ->ns-esc-backslash |\n" +
            "   ->ns-esc-next-line |\n" +
            "   ->ns-esc-non-breaking-space |\n" +
            "   ->ns-esc-line-separator |\n" +
            "   ->ns-esc-paragraph-separator |\n" +
            "   ->ns-esc-8-bit |\n" +
            "   ->ns-esc-16-bit |\n" +
            "   ->ns-esc-32-bit]")
    }

    @Test fun shouldParseMultiLineComment() {
        val production = parse(64, "s-indent(<n)",
            "<a href=\"#s-space\">s-space</a> × <code class=\"varname\">m</code> /* Where <code class=\"varname\">m</code> &lt; <code class=\"varname\">n</code> */")

        assertThat(production.toString()).isEqualTo("`64` : s-indent(<n):\n(->s-space × m /* Where m < n */)")
    }

    @Test fun shouldParsePlus() {
        val production = parse(66, "s-separate-in-line",
            "<a href=\"#s-white\">s-white</a>+ | /* Start of line */")

        assertThat(production.toString()).isEqualTo("`66` : s-separate-in-line:\n[(->s-white × +) |\n   ->Start of line]")
    }

    @Test fun shouldParseSwitch() {
        val production = parse(67, "s-line-prefix(n,c)", "" +
            "<code class=\"varname\">c</code> = block-out ⇒ <a href=\"#s-block-line-prefix(n)\">s-block-line-prefix(n)</a><br> " +
            "<code class=\"varname\">c</code> = block-in&nbsp; ⇒ <a href=\"#s-block-line-prefix(n)\">s-block-line-prefix(n)</a><br> " +
            "<code class=\"varname\">c</code> = flow-out&nbsp; ⇒ <a href=\"#s-flow-line-prefix(n)\">s-flow-line-prefix(n)</a><br> " +
            "<code class=\"varname\">c</code> = flow-in&nbsp;&nbsp; ⇒ <a href=\"#s-flow-line-prefix(n)\">s-flow-line-prefix(n)</a>")

        assertThat(production.toString()).isEqualTo("`67` : s-line-prefix(n,c):\n" +
            "<c> = ->block-out ⇒ ->s-block-line-prefix(n)\n" +
            "<c> = ->block-in ⇒ ->s-block-line-prefix(n)\n" +
            "<c> = ->flow-out ⇒ ->s-flow-line-prefix(n)\n" +
            "<c> = ->flow-in ⇒ ->s-flow-line-prefix(n)")
    }

    @Test fun shouldParseOptional() {
        val production = parse(69, "s-flow-line-prefix(n)",
            "<a href=\"#s-indent(n)\">s-indent(n)</a> <a href=\"#s-separate-in-line\">s-separate-in-line</a>?")

        assertThat(production.toString()).isEqualTo("`69` : s-flow-line-prefix(n):\n->s-indent(n) + (->s-separate-in-line × ?)")
    }

    @Test fun shouldParseMulti() {
        val production = parse(75, "c-nb-comment-text",
            "<a href=\"#c-comment\">“<span class=\"quote\">#</span>”</a> <a href=\"#nb-char\">nb-char</a>*")

        assertThat(production.toString()).isEqualTo("`75` : c-nb-comment-text:\n->c-comment + (->nb-char × *)")
    }

    @Test fun shouldParseEmptyAlternativeBeforeEnd() {
        val production = parse(76, "b-comment",
            "<a href=\"#b-non-content\">b-non-content</a> | /* End of file */")

        assertThat(production.toString()).isEqualTo("`76` : b-comment:\n[->b-non-content |\n   ->End of file]")
    }

    @Test fun shouldParseCommentOnlyAlternativeBeforeClosingParentheses() {
        val production = parse(79, "s-l-comments",
            "( <a href=\"#s-b-comment\">s-b-comment</a> | /* Start of line */ )<br> <a href=\"#l-comment\">l-comment</a>*")

        assertThat(production.toString()).isEqualTo("`79` : s-l-comments:\n[->s-b-comment |\n   ->Start of line] + (->l-comment × *)")
    }

    @Test fun shouldParseSwitchWithPlainRefValue() {
        val production = parse(136, "in-flow(c)", "" +
            " <code class=\"varname\">c</code> = flow-out&nbsp; ⇒ flow-in<br>" +
            " <code class=\"varname\">c</code> = flow-in&nbsp;&nbsp; ⇒ flow-in<br>" +
            " <code class=\"varname\">c</code> = block-key ⇒ flow-key<br>" +
            " <code class=\"varname\">c</code> = flow-key&nbsp; ⇒ flow-key ")

        assertThat(production.toString()).isEqualTo("`136` : in-flow(c):\n" +
            "<c> = ->flow-out ⇒ ->flow-in\n" +
            "<c> = ->flow-in ⇒ ->flow-in\n" +
            "<c> = ->block-key ⇒ ->flow-key\n" +
            "<c> = ->flow-key ⇒ ->flow-key")
    }

    @Test fun shouldParseSwitchWithCommentOnlyCase() {
        val production = parse(164, "c-chomping-indicator(t)", "" +
            " “<span class=\"quote\">-</span>”&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ⇒ <code class=\"varname\">t</code> = strip<br>" +
            " “<span class=\"quote\">+</span>”&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ⇒ <code class=\"varname\">t</code> = keep<br>" +
            " /* Empty */ ⇒ <code class=\"varname\">t</code> = clip ")

        assertThat(production.toString()).isEqualTo("`164` : c-chomping-indicator(t):\n" +
            "<[-][HYPHEN-MINUS][0x2d]> ⇒ <t> = ->strip\n" +
            "<[+][PLUS SIGN][0x2b]> ⇒ <t> = ->keep\n" +
            "->Empty ⇒ <t> = ->clip")
    }

    @Test fun shouldParseEmpty() {
        val production = parse(105, "e-scalar", "/* Empty */")

        assertThat(production.toString()).isEqualTo("`105` : e-scalar:\n->Empty")
    }

    @Test fun shouldParseNestedBrackets() {
        val production = parse(126, "ns-plain-first(c)", "" +
            " &nbsp;&nbsp;( <a href=\"#ns-char\">ns-char</a> - <a href=\"#c-indicator\">c-indicator</a> )<br>" +
            " | ( ( <a href=\"#c-mapping-key\">“<span class=\"quote\">?</span>”</a> | <a href=\"#c-mapping-value\">“<span class=\"quote\">:</span>”</a> | <a href=\"#c-sequence-entry\">“<span class=\"quote\">-</span>”</a> )<br>" +
            " &nbsp;&nbsp;&nbsp;&nbsp;/* Followed by an <a href=\"#ns-plain-safe(c)\">ns-plain-safe(c)</a>) */ ) ")

        assertThat(production.toString()).isEqualTo("`126` : ns-plain-first(c):\n" +
            "[(->ns-char - ->c-indicator) |\n" +
            "   [->c-mapping-key |\n" +
            "   ->c-mapping-value |\n" +
            "   ->c-sequence-entry] + ->Followed by an ns-plain-safe(c) )]")
    }

    @Test fun shouldParseSwitchWithCommentOnlyValue() {
        val production = parse(165, "b-chomped-last(t)", "" +
            "<code class=\"varname\">t</code> = strip ⇒ <a href=\"#b-non-content\">b-non-content</a> | /* End of file */<br> " +
            "<code class=\"varname\">t</code> = clip&nbsp; ⇒ <a href=\"#b-as-line-feed\">b-as-line-feed</a> | /* End of file */<br> " +
            "<code class=\"varname\">t</code> = keep&nbsp; ⇒ <a href=\"#b-as-line-feed\">b-as-line-feed</a> | /* End of file */ ")

        assertThat(production.toString()).isEqualTo("`165` : b-chomped-last(t):\n" +
            "<t> = ->strip ⇒ [->b-non-content |\n   ->End of file]\n" +
            "<t> = ->clip ⇒ [->b-as-line-feed |\n   ->End of file]\n" +
            "<t> = ->keep ⇒ [->b-as-line-feed |\n   ->End of file]")
    }
}
