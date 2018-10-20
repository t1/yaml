package test;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.model.Sequence;
import com.github.t1.yaml.model.Sequence.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;

import static com.github.t1.yaml.model.Collection.Style.FLOW;

class SequenceTest extends AbstractYamlTest {
    @Nested class givenFlowSequenceOfSimpleScalars extends SingleDocument {
        @BeforeEach void setup() {
            input = "[one, two]";
            expected = new Document().node(new Sequence()
                    .style(FLOW)
                    .item(new Scalar().line("one"))
                    .item(new Scalar().line("two"))
            );
        }
    }

    @Nested class givenBlockSequenceOfSimpleScalars extends SingleDocument {
        @BeforeEach void setup() {
            input = "- one\n" +
                    "- two";
            expected = new Document().node(new Sequence()
                    .item(new Scalar().line("one"))
                    .item(new Scalar().line("two"))
            );
        }
    }

    @Disabled @Nested class givenBlockSequenceOfIndentedScalars extends SingleDocument {
        @BeforeEach void setup() {
            input = "- 1\n" +
                    "  2\n" +
                    "- 3\n" +
                    "  4\n" +
                    "    5\n" +
                    "  6";
            expected = new Document().node(new Sequence()
                    .item(new Scalar()
                            .line(new Line().text("1"))
                            .line(new Line().text("2")))
                    .item(new Scalar()
                            .line(new Line().text("3"))
                            .line(new Line().text("4"))
                            .line(new Line().text("5").indent(2))
                            .line(new Line().text("6")))
            );
        }
    }

    @Disabled @Nested class givenBlockSequenceWithIndentedScalarsInNewLines extends SingleDocument {
        @BeforeEach void setup() {
            input = "-\n" +
                    "  1\n" +
                    "  2\n" +
                    "-\n" +
                    "  3\n" +
                    "          4\n" +
                    "  5";
            expected = new Document().node(new Sequence()
                    .item(new Item().nl(true).node(new Scalar()
                            .line(new Line().text("1"))
                            .line(new Line().text("2"))))
                    .item(new Item().nl(true).node(new Scalar()
                            .line(new Line().text("3"))
                            .line(new Line().text("4").indent(8))
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
                    "  -     4\n" +
                    "  - 5";
            expected = new Document().node(new Sequence()
                    .item(new Item().nl(true).node(new Sequence()
                            .item(new Scalar().line("1"))
                            .item(new Scalar().line("2"))))
                    .item(new Item().nl(true).node(new Sequence()
                            .item(new Scalar().line("3"))
                            .item(new Scalar().line(new Line().text("4").indent(4)))
                            .item(new Scalar().line("5"))))
            );
        }
    }

    @Nested class givenBlockSequenceOfBlockSequencesOfBlockSequences extends SingleDocument {
        @BeforeEach void setup() {
            input = "-\n" +
                    "  -\n" +
                    "    - 1\n" +
                    "    - 2\n" +
                    "-\n" +
                    "  -\n" +
                    "    - 3\n" +
                    "    -     4\n" +
                    "    - 5";
            expected = new Document().node(new Sequence()
                    .item(new Item().nl(true).node(new Sequence()
                            .item(new Item().nl(true).node(new Sequence()
                                    .item(new Scalar().line("1"))
                                    .item(new Scalar().line("2"))
                            ))))
                    .item(new Item().nl(true).node(new Sequence()
                            .item(new Item().nl(true).node(new Sequence()
                                    .item(new Scalar().line("3"))
                                    .item(new Scalar().line(new Line().text("4").indent(4)))
                                    .item(new Scalar().line("5"))
                            ))))
            );
        }
    }

    @Nested class givenBlockSequenceOfFlowSequences extends SingleDocument {
        @BeforeEach void setup() {
            input = "- [1, 2]\n" +
                    "- [3, 4, 5]";
            expected = new Document().node(new Sequence()
                    .item(new Item().node(new Sequence()
                            .style(FLOW)
                            .item(new Scalar().line("1"))
                            .item(new Scalar().line("2"))))
                    .item(new Item().node(new Sequence()
                            .style(FLOW)
                            .item(new Scalar().line("3"))
                            .item(new Scalar().line("4"))
                            .item(new Scalar().line("5"))))
            );
        }
    }
}
