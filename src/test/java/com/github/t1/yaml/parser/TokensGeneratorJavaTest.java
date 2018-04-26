package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.Expression.LiteralExpression;
import com.github.t1.yaml.parser.TokensGenerator.Production;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class TokensGeneratorJavaTest {
    @SneakyThrows(IOException.class)
    private String generate(Production... productions) {
        StringWriter writer = new StringWriter();

        TokensGenerator generator = new TokensGenerator();
        generator.productions.addAll(asList(productions));
        generator.generateJavaCode("FooParser", writer);

        return writer.toString();
    }

    private String java(String body) {
        return "package com.github.t1.yaml.parser;\n" +
                "\n" +
                "import javax.annotation.Generated;\n" +
                "\n" +
                "@Generated(\"\")\n" +
                "@lombok.Generated\n" +
                "public class FooParser {\n" +
                body +
                "}\n";
    }

    @Test void shouldGenerateEmptySource() {
        String written = generate();

        assertThat(written).isEqualTo(java(""));
    }

    @Test void shouldGenerateSimpleLiteralProduction() {
        String written = generate(new Production(0, "foo", "",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("" +
                "    void foo() {\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithOneArg() {
        String written = generate(new Production(0, "foo", "n",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("" +
                "    void foo(int n) {\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithLessArg() {
        String written = generate(new Production(0, "foo", "<n",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("" +
                "    void foo_less(int n) {\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithLessEqArg() {
        String written = generate(new Production(0, "foo", "≤n",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("" +
                "    void foo_lessEq(int n) {\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithTwoArgs() {
        String written = generate(new Production(0, "foo", "c,n",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("" +
                "    void foo(int c, int n) {\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithMinus() {
        String written = generate(new Production(0, "c-foo", "",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("" +
                "    void c_foo() {\n" +
                "    }\n"));
    }

    @Test void shouldGenerateLiteralProductionWithPlus() {
        String written = generate(new Production(0, "c+foo", "",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo(java("" +
                "    void c_foo() {\n" +
                "    }\n"));
    }
}
