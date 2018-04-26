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

    @Test void shouldGenerateEmptySource() {
        String written = generate();

        assertThat(written).isEqualTo("" +
                "package com.github.t1.yaml.parser;\n" +
                "\n" +
                "import javax.annotation.Generated;\n" +
                "\n" +
                "@Generated(\"\")\n" +
                "@lombok.Generated\n" +
                "public class FooParser {\n" +
                "}\n");
    }

    @Test void shouldGenerateSimpleProduction() {
        String written = generate(new Production(0, "foo", "",
                new LiteralExpression("bar")));

        assertThat(written).isEqualTo("" +
                "package com.github.t1.yaml.parser;\n" +
                "\n" +
                "import javax.annotation.Generated;\n" +
                "\n" +
                "@Generated(\"\")\n" +
                "@lombok.Generated\n" +
                "public class FooParser {\n" +
                "    void foo() {\n" +
                "    }\n" +
                "}\n");
    }
}
