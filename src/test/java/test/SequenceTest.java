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

class SequenceTest extends AbstractYamlTest {
    @Nested class givenSequence extends SingleDocument {
        @BeforeEach void setup() {
            input = "- one\n- two";
            expected = new Document().node(new SequenceNode()
                    .entry(new ScalarNode().line("one"))
                    .entry(new ScalarNode().line("two"))
            );
        }
    }
}
