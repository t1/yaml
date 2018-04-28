package test;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.ScalarNode.Line;
import com.github.t1.yaml.model.SequenceNode;
import com.github.t1.yaml.model.SequenceNode.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static com.github.t1.yaml.model.CollectionNode.Style.FLOW;

class SequenceTest extends AbstractYamlTest {
    @Nested class givenFlowSequenceOfSimpleScalars extends SingleDocument {
        @BeforeEach void setup() {
            input = "[one, two]";
            expected = new Document().node(new SequenceNode()
                    .style(FLOW)
                    .item(new ScalarNode().line("one"))
                    .item(new ScalarNode().line("two"))
            );
        }
    }

    @Nested class givenBlockSequenceOfSimpleScalars extends SingleDocument {
        @BeforeEach void setup() {
            input = "- one\n" +
                    "- two";
            expected = new Document().node(new SequenceNode()
                    .item(new ScalarNode().line("one"))
                    .item(new ScalarNode().line("two"))
            );
        }
    }

    @Nested class givenBlockSequenceOfIndentedScalars extends SingleDocument {
        @BeforeEach void setup() {
            input = "- 1\n" +
                    "  2\n" +
                    "- 3\n" +
                    "  4\n" +
                    "    5\n" +
                    "  6";
            expected = new Document().node(new SequenceNode()
                    .item(new ScalarNode()
                            .line(new Line().text("1"))
                            .line(new Line().text("2")))
                    .item(new ScalarNode()
                            .line(new Line().text("3"))
                            .line(new Line().text("4"))
                            .line(new Line().indent(2).text("5"))
                            .line(new Line().text("6")))
            );
        }
    }

    @Nested class givenBlockSequenceWithIndentedScalarsInNewLines extends SingleDocument {
        @BeforeEach void setup() {
            input = "-\n" +
                    "  1\n" +
                    "  2\n" +
                    "-\n" +
                    "  3\n" +
                    "  4\n" +
                    "  5";
            expected = new Document().node(new SequenceNode()
                    .item(new Item().nl(true).node(new ScalarNode()
                            .line(new Line().text("1"))
                            .line(new Line().text("2"))))
                    .item(new Item().nl(true).node(new ScalarNode()
                            .line(new Line().text("3"))
                            .line(new Line().text("4"))
                            .line(new Line().text("5"))))
            );
        }
    }

    @Nested class givenBlockSequenceOfBlockSequences extends SingleDocument {
        @BeforeEach void setup() {
            input = "-\n" +
                    "  - 1\n" +
                    "  - 2\n" +
                    "-\n" +
                    "  - 3\n" +
                    "  - 4\n" +
                    "  - 5";
            expected = new Document().node(new SequenceNode()
                    .item(new Item().nl(true).node(new SequenceNode()
                            .item(new ScalarNode().line("1"))
                            .item(new ScalarNode().line("2"))))
                    .item(new Item().nl(true).node(new SequenceNode()
                            .item(new ScalarNode().line("3"))
                            .item(new ScalarNode().line("4"))
                            .item(new ScalarNode().line("5"))))
            );
        }
    }
}
