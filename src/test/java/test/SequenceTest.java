package test;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.ScalarNode.Line;
import com.github.t1.yaml.model.SequenceNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;

class SequenceTest extends AbstractYamlTest {
    @Nested class givenSequence extends SingleDocument {
        @BeforeEach void setup() {
            input = "- one\n" +
                    "- two";
            expected = new Document().node(new SequenceNode()
                    .item(new ScalarNode().line("one"))
                    .item(new ScalarNode().line("two"))
            );
        }
    }

    @Nested class givenSequenceWithIndentedScalars extends SingleDocument {
        @BeforeEach void setup() {
            input = "- 1\n" +
                    "  2\n" +
                    "- 3\n" +
                    "  4\n" +
                    "  5";
            expected = new Document().node(new SequenceNode()
                    .item(new ScalarNode()
                            .line(new Line().text("1"))
                            .line(new Line().indent(2).text("2")))
                    .item(new ScalarNode()
                            .line(new Line().text("3"))
                            .line(new Line().indent(2).text("4"))
                            .line(new Line().indent(2).text("5")))
            );
        }
    }

    @Disabled @Nested class givenSequenceWithIndentedScalarsInNewLines extends SingleDocument {
        @BeforeEach void setup() {
            input = "-\n" +
                    "  1\n" +
                    "  2\n" +
                    "-\n" +
                    "  3\n" +
                    "  4\n" +
                    "  5";
            expected = new Document().node(new SequenceNode()
                    .item(new ScalarNode()
                            .line(new Line().text("1"))
                            .line(new Line().indent(2).text("2")))
                    .item(new ScalarNode()
                            .line(new Line().text("3"))
                            .line(new Line().indent(2).text("4"))
                            .line(new Line().indent(2).text("5")))
            );
        }
    }

    @Disabled @Nested class givenSequenceOfSequence extends SingleDocument {
        @BeforeEach void setup() {
            input = "" +
                    "-\n" +
                    "  - 1\n" +
                    "  - 2\n" +
                    "-\n" +
                    "  - 3\n" +
                    "  - 4\n" +
                    "  - 5";
            expected = new Document().node(new SequenceNode()
                    .item(
                            new SequenceNode()
                                    .item(new ScalarNode().line("1.1"))
                                    .item(new ScalarNode().line("1.2"))
                    )
                    .item(
                            new SequenceNode()
                                    .item(new ScalarNode().line("2.1"))
                                    .item(new ScalarNode().line("2.2"))
                                    .item(new ScalarNode().line("2.3"))
                    )
            );
        }
    }
}
