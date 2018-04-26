package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import com.github.t1.yaml.parser.Expression.AlternativesExpression;
import com.github.t1.yaml.parser.Expression.CodePointExpression;
import com.github.t1.yaml.parser.Expression.LiteralExpression;
import com.github.t1.yaml.parser.Expression.MinusExpression;
import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import com.github.t1.yaml.parser.ParserGenerator.Production;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class ParserGeneratorJavaTest {
    private String generate(Production... productions) {
        StringWriter writer = new StringWriter();

        ParserGenerator generator = new ParserGenerator();
        generator.productions.addAll(asList(productions));
        generator.generateJavaCode("FooParser", writer);

        return writer.toString();
    }

    private String java(String body) {
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
                "@Generated(\"" + ParserGenerator.class.getName() + "\")\n" +
                "@lombok.Generated\n" +
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
                "}\n";
    }

    @Test void shouldGenerateEmptySource() {
        String written = generate();

        assertThat(written).isEqualTo(java(""));
    }

    @Test void shouldGenerateSimpleLiteralProduction() {
        String written = generate(new Production(0, "foo", null,
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [0] : foo:\n" +
                "     *   <bar>\n" +
                "     */\n" +
                "    private Object foo() {\n" +
                "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithOneArg() {
        String written = generate(new Production(0, "foo", "n",
                new LiteralExpression("baz")));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [0] : foo [n]:\n" +
                "     *   <baz>\n" +
                "     */\n" +
                "    private Object foo(int n) {\n" +
                "        return next.accept(\"baz\") ? \"baz\" : null;\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithLessArg() {
        String written = generate(new Production(0, "foo", "<n",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [0] : foo [<n]:\n" +
                "     *   <bar>\n" +
                "     */\n" +
                "    private Object foo_less(int n) {\n" +
                "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithLessEqArg() {
        String written = generate(new Production(0, "foo", "≤n",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [0] : foo [≤n]:\n" +
                "     *   <bar>\n" +
                "     */\n" +
                "    private Object foo_lessEq(int n) {\n" +
                "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithTwoArgs() {
        String written = generate(new Production(0, "foo", "c,n",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [0] : foo [c,n]:\n" +
                "     *   <bar>\n" +
                "     */\n" +
                "    private Object foo(int c, int n) {\n" +
                "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithMinus() {
        String written = generate(new Production(0, "c-foo", null,
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [0] : c-foo:\n" +
                "     *   <bar>\n" +
                "     */\n" +
                "    private Object c_foo() {\n" +
                "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithPlus() {
        String written = generate(new Production(0, "c+foo", null,
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [0] : c+foo:\n" +
                "     *   <bar>\n" +
                "     */\n" +
                "    private Object c_foo() {\n" +
                "        return next.accept(\"bar\") ? \"bar\" : null;\n" +
                "    }\n"));
    }


    @Test void shouldGenerateRefProduction() {
        String written = generate(new Production(0, "foo", null,
                new ReferenceExpression("bar")));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [0] : foo:\n" +
                "     *   ->bar\n" +
                "     */\n" +
                "    private Object foo() {\n" +
                "        return bar();\n" +
                "    }\n"));
    }


    @Test void shouldGenerateAlternativeReferencesProduction() {
        String written = generate(new Production(0, "ref-alternatives", null,
                AlternativesExpression.of(new ReferenceExpression("ref1"), new ReferenceExpression("ref2"))));

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
                "    }\n"));
    }

    @Test void shouldGenerateAlternativeCodePointsProduction() {
        String written = generate(new Production(0, "c-alternatives", null,
                AlternativesExpression.of(
                        new CodePointExpression(CodePoint.of("a")),
                        new CodePointExpression(CodePoint.of("b")))));

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
                "    }\n"));
    }

    @Test void shouldGenerateMixedAlternativesProduction() {
        String written = generate(new Production(0, "mixed-alternatives", null,
                AlternativesExpression.of(
                        new ReferenceExpression("ref1"),
                        new CodePointExpression(CodePoint.of("b")))));

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
                "    }\n"));
    }

    @Test void shouldGenerateMinusRefProduction() {
        String written = generate(new Production(27, "nb-char", null,
                new MinusExpression(new ReferenceExpression("c-printable"))
                        .minus(new ReferenceExpression("b-char"))
                        .minus(new ReferenceExpression("c-byte-order-mark"))
        ));

        assertThat(written).isEqualTo(java("\n" +
                "    /**\n" +
                "     * [27] : nb-char:\n" +
                "     *   ->c-printable - ->b-char - ->c-byte-order-mark\n" +
                "     */\n" +
                "    private Object nb_char() {\n" +
                "" +
                "    }\n"));
    }
}
