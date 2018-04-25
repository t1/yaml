package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.TokensGenerator.Production;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

class TokensGeneratorTest {
    @Test void shouldParseFullSpec() {
        TokensGenerator generator = new TokensGenerator();

        generator.run();

        StringBuilder actual = new StringBuilder();
        for (Production production : generator.productions)
            actual.append(production).append("\n\n");
        assertThat(actual.toString()).isEqualTo(contentOf(TokensGeneratorTest.class.getResource("expected.txt")));
    }

    private Production parse(int counter, String name, String expression) {
        Element element = Jsoup.parse("<html><head></head><body>" +
                "<table>\n" +
                "  <tr>\n" +
                "    <td class=\"productioncounter\">[" + counter + "]</td> \n" +
                "    <td class=\"productionlhs\"><a id=\"" + name + "\"></a>" + name.replace("<", "&lt;") + "</td> \n" +
                "    <td class=\"productionrhs\"> " + expression + " </td> \n" +
                "  </tr>\n" +
                "</table>" +
                "</body></html>").selectFirst("tr");

        return new Production(element);
    }

    @Test
    void shouldParseAlternatives() {
        Production production = parse(1, "c-printable",
                "&nbsp;&nbsp;#x9 | #xA | #xD | [#x20-#x7E]&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/* 8 bit */<br>" +
                        " | #x85 | [#xA0-#xD7FF] | [#xE000-#xFFFD] /* 16 bit */<br>" +
                        " | [#x10000-#x10FFFF]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;/* 32 bit */");

        assertThat(production.toString()).isEqualTo("" +
                "[1] : c-printable:\n" +
                "  [<[\\t][CHARACTER TABULATION][0x9]> ||\n" +
                "   <[\\n][LINE FEED (LF)][0xa]> ||\n" +
                "   <[\\r][CARRIAGE RETURN (CR)][0xd]> ||\n" +
                "   [<[ ][SPACE][0x20]>-<[~][TILDE][0x7e]>] ||\n" +
                "   <[\u0085][NEXT LINE (NEL)][0x85]> ||\n" +
                "   [<[ ][NO-BREAK SPACE][0xa0]>-<[\uD7FF][null][0xd7ff]>] ||\n" +
                "   [<[\uE000][PRIVATE USE AREA E000][0xe000]>-<[�][REPLACEMENT CHARACTER][0xfffd]>] ||\n" +
                "   [<[\uD800\uDC00][LINEAR B SYLLABLE B008 A][0x10000]>-<[\uDBFF\uDFFF][null][0x10ffff]>]]");
    }

    @Test void shouldParseHex() {
        Production production = parse(3, "c-byte-order-mark", "#xFEFF");

        assertThat(production.toString()).isEqualTo("" +
                "[3] : c-byte-order-mark:\n" +
                "  <[\uFEFF][ZERO WIDTH NO-BREAK SPACE][0xfeff]>");
    }

    @Test void shouldParseQuote() {
        Production production = parse(4, "c-sequence-entry", "“<span class=\"quote\">-</span>”");

        assertThat(production.toString()).isEqualTo("" +
                "[4] : c-sequence-entry:\n" +
                "  <[-][HYPHEN-MINUS][0x2d]>");
    }

    @Test void shouldParseHref() {
        Production production = parse(0, "foo", "<a href=\"#bar\">bar</a>");

        assertThat(production.toString()).isEqualTo("" +
                "[0] : foo:\n" +
                "  ->bar");
    }

    @Test void shouldParseMinus() {
        Production production = parse(27, "nb-char",
                "<a href=\"#c-printable\">c-printable</a> - <a href=\"#b-char\">b-char</a> - <a href=\"#c-byte-order-mark\">c-byte-order-mark</a>");

        assertThat(production.toString()).isEqualTo("" +
                "[27] : nb-char:\n" +
                "  ->c-printable - ->b-char - ->c-byte-order-mark");
    }

    @Test void shouldParseMultiplication() {
        Production production = parse(59, "ns-esc-8-bit",
                "“<span class=\"quote\">x</span>”<br> ( <a href=\"#ns-hex-digit\">ns-hex-digit</a> × 2 )");

        assertThat(production.toString()).isEqualTo("" +
                "[59] : ns-esc-8-bit:\n" +
                "  <[x][LATIN SMALL LETTER X][0x78]> + (->ns-hex-digit × 2)");
    }

    @Test void shouldParsePlus() {
        Production production = parse(66, "s-separate-in-line",
                "<a href=\"#s-white\">s-white</a>+ | /* Start of line */");

        assertThat(production.toString()).isEqualTo("" +
                "[66] : s-separate-in-line:\n" +
                "  (->s-white × +)");
    }

    @Test void shouldParseOptional() {
        Production production = parse(69, "s-flow-line-prefix(n)",
                "<a href=\"#s-indent(n)\">s-indent(n)</a> <a href=\"#s-separate-in-line\">s-separate-in-line</a>?");

        assertThat(production.toString()).isEqualTo("" +
                "[69] : s-flow-line-prefix [n]:\n" +
                "  ->s-indent(n) + (->s-separate-in-line × ?)");
    }

    @Test void shouldParseMulti() {
        Production production = parse(75, "c-nb-comment-text",
                "<a href=\"#c-comment\">“<span class=\"quote\">#</span>”</a> <a href=\"#nb-char\">nb-char</a>*");

        assertThat(production.toString()).isEqualTo("" +
                "[75] : c-nb-comment-text:\n" +
                "  ->c-comment + (->nb-char × *)");
    }

    @Test void shouldParseParentheses() {
        Production production = parse(0, "foo", "<a href=\"#bar\">bar</a> | ( <a href=\"#baz\">baz</a></a> )");

        assertThat(production.toString()).isEqualTo("" +
                "[0] : foo:\n" +
                "  [->bar ||\n" +
                "   ->baz]");
    }

    @Test void shouldParseEmptyAlternativeBeforeEnd() {
        Production production = parse(76, "b-comment",
                "<a href=\"#b-non-content\">b-non-content</a> | /* End of file */");

        assertThat(production.toString()).isEqualTo("" +
                "[76] : b-comment:\n" +
                "  ->b-non-content");
    }

    @Test void shouldParseEmptyAlternativeBeforeClosingParentheses() {
        Production production = parse(79, "s-l-comments",
                "( <a href=\"#s-b-comment\">s-b-comment</a> | /* Start of line */ )<br> <a href=\"#l-comment\">l-comment</a>*");

        assertThat(production.toString()).isEqualTo("" +
                "[79] : s-l-comments:\n" +
                "  ->s-b-comment + (->l-comment × *)");
    }

    @Test void shouldParseMultiLineComment() {
        Production production = parse(64, "s-indent(<n)",
                "<a href=\"#s-space\">s-space</a> × <code class=\"varname\">m</code> /* Where <code class=\"varname\">m</code> &lt; <code class=\"varname\">n</code> */");

        assertThat(production.toString()).isEqualTo("" +
                "[64] : s-indent [<n]:\n" +
                "  (->s-space × m)");
    }

    @Test void shouldParseEmpty() {
        Production production = parse(105, "e-scalar", "/* Empty */");

        assertThat(production.toString()).isEqualTo("" +
                "[105] : e-scalar:\n" +
                "  <empty>");
    }

    @Test void shouldParseSwitch() {
        Production production = parse(67, "s-line-prefix(n,c)",
                "<code class=\"varname\">c</code> = block-out ⇒ <a href=\"#s-block-line-prefix(n)\">s-block-line-prefix(n)</a><br> <code class=\"varname\">c</code> = block-in&nbsp; ⇒ <a href=\"#s-block-line-prefix(n)\">s-block-line-prefix(n)</a><br> <code class=\"varname\">c</code> = flow-out&nbsp; ⇒ <a href=\"#s-flow-line-prefix(n)\">s-flow-line-prefix(n)</a><br> <code class=\"varname\">c</code> = flow-in&nbsp;&nbsp; ⇒ <a href=\"#s-flow-line-prefix(n)\">s-flow-line-prefix(n)</a>");

        assertThat(production.toString()).isEqualTo("" +
                "[67] : s-line-prefix [n,c]:\n" +
                "  <c = block-out> ⇒ <->s-block-line-prefix(n)>\n" +
                "  <c = block-in> ⇒ <->s-block-line-prefix(n)>\n" +
                "  <c = flow-out> ⇒ <->s-flow-line-prefix(n)>\n" +
                "  <c = flow-in> ⇒ <->s-flow-line-prefix(n)>");
    }
}
