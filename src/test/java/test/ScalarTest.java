package test;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.SequenceNode;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static test.Helpers.parse;

class ScalarTest extends AbstractYamlTest {
    @Nested class givenScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string";
            expected = new Document().node(new ScalarNode().line("dummy-string"));
        }
    }

    @Test void expectScalarStartNotContinueWithBlockSequence() {
        Throwable thrown = catchThrowable(() -> parse("scalar document\n- illegal sequence"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found block sequence at [-][HYPHEN-MINUS][0x2d] at line 2 char 1");
    }

    @Test void expectScalarStartNotContinueWithBlockMapping() {
        Throwable thrown = catchThrowable(() -> parse("scalar document\nkey: value"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found block mapping at [k][LATIN SMALL LETTER K][0x6b] at line 2 char 1");
    }

    @Test void expectScalarStartNotContinueWithFlowMapping() {
        Throwable thrown = catchThrowable(() -> parse("scalar document\n{key: value}"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found flow mapping at [{][LEFT CURLY BRACKET][0x7b] at line 2 char 1");
    }
}
