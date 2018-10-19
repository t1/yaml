package test

import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.ScalarNode
import com.github.t1.yaml.model.ScalarNode.Line
import com.github.t1.yaml.model.SequenceNode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested

@Suppress("ClassName") class SequenceTest : AbstractYamlTest() {
    @Nested inner class givenSequence : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "- one\n" + "- two"
            AbstractYamlTest.expected = Document().node(SequenceNode()
                .entry(ScalarNode().line("one"))
                .entry(ScalarNode().line("two"))
            )
        }
    }

    @Nested inner class givenSequenceWithIndentedScalars : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "- 1\n" +
                "  2\n" +
                "- 3\n" +
                "  4\n" +
                "  5"
            AbstractYamlTest.expected = Document().node(SequenceNode()
                .entry(ScalarNode()
                    .line(Line().text("1"))
                    .line(Line().indent(2).text("2")))
                .entry(ScalarNode()
                    .line(Line().text("3"))
                    .line(Line().indent(2).text("4"))
                    .line(Line().indent(2).text("5")))
            )
        }
    }

    @Nested inner class givenSequenceWithIndentedScalarsInNewLines : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "-\n" +
                "  1\n" +
                "  2\n" +
                "-\n" +
                "  3\n" +
                "  4\n" +
                "  5"
            AbstractYamlTest.expected = Document().node(SequenceNode()
                .entry(ScalarNode()
                    .line(Line().text("1"))
                    .line(Line().indent(2).text("2")))
                .entry(ScalarNode()
                    .line(Line().text("3"))
                    .line(Line().indent(2).text("4"))
                    .line(Line().indent(2).text("5")))
            )
        }
    }

    @Nested inner class givenSequenceOfSequence : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "" +
                "-\n" +
                "  - 1\n" +
                "  - 2\n" +
                "-\n" +
                "  - 3\n" +
                "  - 4\n" +
                "  - 5"
            AbstractYamlTest.expected = Document().node(SequenceNode()
                .entry(
                    SequenceNode()
                        .entry(ScalarNode().line("1.1"))
                        .entry(ScalarNode().line("1.2"))
                )
                .entry(
                    SequenceNode()
                        .entry(ScalarNode().line("2.1"))
                        .entry(ScalarNode().line("2.2"))
                        .entry(ScalarNode().line("2.3"))
                )
            )
        }
    }
}
