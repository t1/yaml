package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.TokensGenerator.Production;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

class TokensGeneratorTest {
    @Test
    void shouldRun() {
        List<Production> productions = new ArrayList<>();
        new TokensGenerator(productions::add).run();

        StringBuilder actual = new StringBuilder();
        for (Production production : productions)
            actual.append(production).append("\n\n");
        assertThat(actual.toString()).isEqualTo(contentOf(TokensGeneratorTest.class.getResource("expected.txt")));
    }
}
