package test

import com.github.t1.yaml.model.Collection.Style.FLOW
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Sequence
import com.github.t1.yaml.model.Sequence.Item
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested

@Suppress("ClassName")
class SequenceTest : AbstractYamlTest() {
    @Nested inner class givenFlowSequenceOfSimpleScalars : SingleDocument() {
        @BeforeEach fun setup() {
            input = "[one, two]"
            expected = Document(node = Sequence(style = FLOW)
                .item(Scalar().line("one"))
                .item(Scalar().line("two"))
            )
        }
    }

    @Nested inner class givenBlockSequenceOfSimpleScalars : SingleDocument() {
        @BeforeEach fun setup() {
            input = "- one\n" + "- two"
            expected = Document(node = Sequence()
                .item(Scalar().line("one"))
                .item(Scalar().line("two"))
            )
        }
    }

    @Nested inner class givenBlockSequenceOfIndentedScalars : SingleDocument() {
        @BeforeEach fun setup() {
            input = "- 1\n" +
                "  2\n" +
                "- 3\n" +
                "  4\n" +
                "    5\n" +
                "  6"
            expected = Document(node = Sequence()
                .item(Scalar()
                    .line(Line().text("1"))
                    .line(Line().text("2")))
                .item(Scalar()
                    .line(Line().text("3"))
                    .line(Line().text("4"))
                    .line(Line().text("5").indent(2))
                    .line(Line().text("6")))
            )
        }
    }

    @Nested inner class givenBlockSequenceWithIndentedScalarsInNewLines : SingleDocument() {
        @BeforeEach fun setup() {
            input = "-\n" +
                "  1\n" +
                "  2\n" +
                "-\n" +
                "  3\n" +
                "          4\n" +
                "  5"
            expected = Document(node = Sequence()
                .item(Item(nl = true, node = Scalar()
                    .line(Line().text("1"))
                    .line(Line().text("2"))))
                .item(Item(nl = true, node = Scalar()
                    .line(Line().text("3"))
                    .line(Line().text("4").indent(8))
                    .line(Line().text("5"))))
            )
        }
    }

    @Nested inner class givenBlockSequenceOfBlockSequences : SingleDocument() {
        @BeforeEach fun setup() {
            input = "-\n" +
                "  - 1\n" +
                "  - 2\n" +
                "-\n" +
                "  - 3\n" +
                "  -     4\n" +
                "  - 5"
            expected = Document(node = Sequence()
                .item(Item(nl = true, node = Sequence()
                    .item(Scalar().line("1"))
                    .item(Scalar().line("2"))))
                .item(Item(nl = true, node = Sequence()
                    .item(Scalar().line("3"))
                    .item(Scalar().line(Line().text("4").indent(4)))
                    .item(Scalar().line("5"))))
            )
        }
    }

    @Nested inner class givenBlockSequenceOfBlockSequencesOfBlockSequences : SingleDocument() {
        @BeforeEach fun setup() {
            input = "-\n" +
                "  -\n" +
                "    - 1\n" +
                "    - 2\n" +
                "-\n" +
                "  -\n" +
                "    - 3\n" +
                "    -     4\n" +
                "    - 5"
            expected = Document(node = Sequence()
                .item(Item(nl = true, node = Sequence()
                    .item(Item(nl = true, node = Sequence()
                        .item(Scalar().line("1"))
                        .item(Scalar().line("2"))
                    ))))
                .item(Item(nl = true, node = Sequence()
                    .item(Item(nl = true, node = Sequence()
                        .item(Scalar().line("3"))
                        .item(Scalar().line(Line().text("4").indent(4)))
                        .item(Scalar().line("5"))
                    ))))
            )
        }
    }

    @Nested inner class givenBlockSequenceOfFlowSequences : SingleDocument() {
        @BeforeEach fun setup() {
            input = "- [1, 2]\n" + "- [3, 4, 5]"
            expected = Document(node = Sequence()
                .item(Item(node = Sequence(style = FLOW)
                    .item(Scalar().line("1"))
                    .item(Scalar().line("2"))))
                .item(Item(node = Sequence(style = FLOW)
                    .item(Scalar().line("3"))
                    .item(Scalar().line("4"))
                    .item(Scalar().line("5"))))
            )
        }
    }
}
