package test

import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.MappingNode
import com.github.t1.yaml.model.ScalarNode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested

@Suppress("ClassName")
class MappingTest : AbstractYamlTest() {
    @Nested inner class givenBlockMapping : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "sky: blue\nsea: green"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry("sky", "blue")
                .entry("sea", "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithSpaceInKey : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "sky high: blue\nsea deep: green"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry("sky high", "blue")
                .entry("sea deep", "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithColonInKey : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "sky:high: blue\nsea:deep: green"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry("sky:high", "blue")
                .entry("sea:deep", "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithDoubleQuotedKey : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "\"sky\": blue\n\"sea\": green"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry(ScalarNode().doubleQuoted().line("sky"), "blue")
                .entry(ScalarNode().doubleQuoted().line("sea"), "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithDoubleQuotedValue : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "sky: \"blue\"\nsea: \"green\""
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry("sky", ScalarNode().doubleQuoted().line("blue"))
                .entry("sea", ScalarNode().doubleQuoted().line("green"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithDoubleQuotedKeyAndValue : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "\"sky\": \"blue\"\n\"sea\": \"green\""
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry(ScalarNode().doubleQuoted().line("sky"), ScalarNode().doubleQuoted().line("blue"))
                .entry(ScalarNode().doubleQuoted().line("sea"), ScalarNode().doubleQuoted().line("green"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithMarkedKeys : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "? sky: blue\n? sea: green"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry(MappingNode.Entry().hasMarkedKey(true).key(ScalarNode().line("sky")).value(ScalarNode().line("blue")))
                .entry(MappingNode.Entry().hasMarkedKey(true).key(ScalarNode().line("sea")).value(ScalarNode().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithFirstKeyMarked : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "? sky: blue\nsea: green"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry(MappingNode.Entry().hasMarkedKey(true).key(ScalarNode().line("sky")).value(ScalarNode().line("blue")))
                .entry(MappingNode.Entry().hasMarkedKey(false).key(ScalarNode().line("sea")).value(ScalarNode().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithLastKeyMarked : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "sky: blue\n? sea: green"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry(MappingNode.Entry().hasMarkedKey(false).key(ScalarNode().line("sky")).value(ScalarNode().line("blue")))
                .entry(MappingNode.Entry().hasMarkedKey(true).key(ScalarNode().line("sea")).value(ScalarNode().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithNewLineAfterKey : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "sky:\nblue\nsea:\ngreen"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry(MappingNode.Entry().hasNlAfterKey(true).key(ScalarNode().line("sky")).value(ScalarNode().line("blue")))
                .entry(MappingNode.Entry().hasNlAfterKey(true).key(ScalarNode().line("sea")).value(ScalarNode().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithNewLineAfterFirstKey : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "sky:\nblue\nsea: green"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry(MappingNode.Entry().hasNlAfterKey(true).key(ScalarNode().line("sky")).value(ScalarNode().line("blue")))
                .entry(MappingNode.Entry().hasNlAfterKey(false).key(ScalarNode().line("sea")).value(ScalarNode().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithNewLineAfterLastKey : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "sky: blue\nsea:\ngreen"
            AbstractYamlTest.expected = Document().node(MappingNode()
                .entry(MappingNode.Entry().hasNlAfterKey(false).key(ScalarNode().line("sky")).value(ScalarNode().line("blue")))
                .entry(MappingNode.Entry().hasNlAfterKey(true).key(ScalarNode().line("sea")).value(ScalarNode().line("green")))
            )
        }
    }
}
