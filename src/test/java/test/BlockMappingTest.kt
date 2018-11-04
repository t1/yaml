package test

import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Mapping
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Sequence
import com.github.t1.yaml.model.Sequence.Item
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested

@Suppress("ClassName")
class BlockMappingTest : AbstractYamlTest() {
    @Nested inner class givenBlockMapping : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky: blue\ntree: green"
            expected = Document(node = Mapping()
                .entry("sky", "blue")
                .entry("tree", "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithSpaceInKey : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky high: blue\ntree deep: green"
            expected = Document(node = Mapping()
                .entry("sky high", "blue")
                .entry("tree deep", "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithColonInKey : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky:high: blue\ntree:deep: green"
            expected = Document(node = Mapping()
                .entry("sky:high", "blue")
                .entry("tree:deep", "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithTwoLineValue : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky: high\n" +
                "  blue\n" +
                "tree: low\n" +
                "  green"
            expected = Document(node = Mapping()
                .entry("sky", Scalar().line("high").line("blue"))
                .entry("tree", Scalar().line("low").line("green"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithDoubleQuotedKey : SingleDocument() {
        @BeforeEach fun setup() {
            input = "\"sky\": blue\n\"tree\": green"
            expected = Document(node = Mapping()
                .entry(Scalar().doubleQuoted().line("sky"), "blue")
                .entry(Scalar().doubleQuoted().line("tree"), "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithDoubleQuotedValue : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky: \"blue\"\ntree: \"green\""
            expected = Document(node = Mapping()
                .entry("sky", Scalar().doubleQuoted().line("blue"))
                .entry("tree", Scalar().doubleQuoted().line("green"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithDoubleQuotedKeyAndValue : SingleDocument() {
        @BeforeEach fun setup() {
            input = "\"sky\": \"blue\"\n\"tree\": \"green\""
            expected = Document(node = Mapping()
                .entry(Scalar().doubleQuoted().line("sky"), Scalar().doubleQuoted().line("blue"))
                .entry(Scalar().doubleQuoted().line("tree"), Scalar().doubleQuoted().line("green"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithTwoLineSingleQuotedValue : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky: 'high\n" +
                "blue'\n" +
                "tree: 'low\n" +
                "green'"
            expected = Document(node = Mapping()
                .entry("sky", Scalar().singleQuoted().line("high\nblue"))
                .entry("tree", Scalar().singleQuoted().line("low\ngreen"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithSingleQuotedKey : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'sky': blue\n'tree': green"
            expected = Document(node = Mapping()
                .entry(Scalar().singleQuoted().line("sky"), "blue")
                .entry(Scalar().singleQuoted().line("tree"), "green")
            )
        }
    }

    @Nested inner class givenBlockMappingWithSingleQuotedValue : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky: 'blue'\ntree: 'green'"
            expected = Document(node = Mapping()
                .entry("sky", Scalar().singleQuoted().line("blue"))
                .entry("tree", Scalar().singleQuoted().line("green"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithTwoLineDoubleQuotedValue : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky: \"high\n" +
                "blue\"\n" +
                "tree: \"low\n" +
                "green\""
            expected = Document(node = Mapping()
                .entry("sky", Scalar().doubleQuoted().line("high\nblue"))
                .entry("tree", Scalar().doubleQuoted().line("low\ngreen"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithSingleQuotedKeyAndValue : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'sky': 'blue'\n'tree': 'green'"
            expected = Document(node = Mapping()
                .entry(Scalar().singleQuoted().line("sky"), Scalar().singleQuoted().line("blue"))
                .entry(Scalar().singleQuoted().line("tree"), Scalar().singleQuoted().line("green"))
            )
        }
    }

    @Nested inner class givenBlockMappingWithSpacesInSingleQuotedKeyAndValue : SingleDocument() {
        @BeforeEach fun setup() {
            input = "' sky high ': ' light blue '\n' tree deep ': ' dark green '"
            expected = Document(node = Mapping()
                .entry(Scalar().singleQuoted().line(" sky high "), Scalar().singleQuoted().line(" light blue "))
                .entry(Scalar().singleQuoted().line(" tree deep "), Scalar().singleQuoted().line(" dark green "))
            )
        }
    }

    @Nested inner class givenBlockMappingWithMarkedKeys : SingleDocument() {
        @BeforeEach fun setup() {
            input = "? sky: blue\n? tree: green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(hasMarkedKey = true, key = Scalar().line("sky"), value = Scalar().line("blue")))
                .entry(Mapping.Entry(hasMarkedKey = true, key = Scalar().line("tree"), value = Scalar().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithFirstKeyMarked : SingleDocument() {
        @BeforeEach fun setup() {
            input = "? sky: blue\ntree: green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(hasMarkedKey = true, key = Scalar().line("sky"), value = Scalar().line("blue")))
                .entry(Mapping.Entry(hasMarkedKey = false, key = Scalar().line("tree"), value = Scalar().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithLastKeyMarked : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky: blue\n? tree: green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(hasMarkedKey = false, key = Scalar().line("sky"), value = Scalar().line("blue")))
                .entry(Mapping.Entry(hasMarkedKey = true, key = Scalar().line("tree"), value = Scalar().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithNewLineAfterKey : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky:\n" +
                "  blue\n" +
                "tree:\n" +
                "  green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(hasNlAfterKey = true, key = Scalar().line("sky"), value = Scalar().line("blue")))
                .entry(Mapping.Entry(hasNlAfterKey = true, key = Scalar().line("tree"), value = Scalar().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithNewLineAfterFirstKey : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky:\n" +
                "  blue\n" +
                "tree: green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(hasNlAfterKey = true, key = Scalar().line("sky"), value = Scalar().line("blue")))
                .entry(Mapping.Entry(hasNlAfterKey = false, key = Scalar().line("tree"), value = Scalar().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithNewLineAfterLastKey : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky: blue\n" +
                "tree:\n" +
                "  green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(hasNlAfterKey = false, key = Scalar().line("sky"), value = Scalar().line("blue")))
                .entry(Mapping.Entry(hasNlAfterKey = true, key = Scalar().line("tree"), value = Scalar().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithIndentedKeys : SingleDocument() {
        @BeforeEach fun setup() {
            input = " sky: blue\n" + " tree: green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(key = Scalar().line(Line().indent(1).text("sky")), value = Scalar().line("blue")))
                .entry(Mapping.Entry(key = Scalar().line(Line().indent(1).text("tree")), value = Scalar().line("green")))
            )
        }
    }

    @Nested inner class givenBlockMappingWithIndentedValues : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky:   blue\ntree:     green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(key = Scalar().line("sky"), value = Scalar().line(Line().indent(2).text("blue"))))
                .entry(Mapping.Entry(key = Scalar().line("tree"), value = Scalar().line(Line().indent(4).text("green"))))
            )
        }
    }

    @Nested inner class givenBlockMappingWithIndentedKeysAndValues : SingleDocument() {
        @BeforeEach fun setup() {
            input = " sky:   blue\n" + " tree:     green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(key = Scalar().line(Line().indent(1).text("sky")), value = Scalar().line(Line().indent(2).text("blue"))))
                .entry(Mapping.Entry(key = Scalar().line(Line().indent(1).text("tree")), value = Scalar().line(Line().indent(4).text("green"))))
            )
        }
    }

    @Nested inner class givenBlockMappingWithIndentedMarkedKeysAndValues : SingleDocument() {
        @BeforeEach fun setup() {
            input = "?  sky:   blue\n?    tree:     green"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(hasMarkedKey = true, key = Scalar().line(Line().indent(1).text("sky")), value = Scalar().line(Line().indent(2).text("blue"))))
                .entry(Mapping.Entry(hasMarkedKey = true, key = Scalar().line(Line().indent(3).text("tree")), value = Scalar().line(Line().indent(4).text("green"))))
            )
        }
    }

    @Nested inner class givenBlockMappingToBlockMapping : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky:\n" +
                "  color: blue\n" +
                "  depth: high\n" +
                "tree:\n" +
                "  color: green\n" +
                "  depth: deep"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(key = Scalar().line("sky"), hasNlAfterKey = true, value = Mapping()
                    .entry("color", "blue")
                    .entry("depth", "high")
                ))
                .entry(Mapping.Entry(key = Scalar().line("tree"), hasNlAfterKey = true, value = Mapping()
                    .entry("color", "green")
                    .entry("depth", "deep")
                ))
            )
        }
    }

    @Nested inner class givenBlockMappingToBlockMappingToBlockMapping : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky:\n" +
                "  properties:\n" +
                "    color: blue\n" +
                "    depth: high\n" +
                "  size:\n" +
                "    width: 100%\n" +
                "tree:\n" +
                "  properties:\n" +
                "    color: green\n" +
                "    depth: deep"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(key = Scalar().line("sky"), hasNlAfterKey = true, value = Mapping()
                    .entry(Mapping.Entry(key = Scalar().line("properties"), hasNlAfterKey = true, value = Mapping()
                        .entry("color", "blue")
                        .entry("depth", "high")))
                    .entry(Mapping.Entry(key = Scalar().line("size"), hasNlAfterKey = true, value = Mapping()
                        .entry("width", "100%")
                    )
                    )
                ))
                .entry(Mapping.Entry(key = Scalar().line("tree"), hasNlAfterKey = true, value = Mapping()
                    .entry(Mapping.Entry(key = Scalar().line("properties"), hasNlAfterKey = true, value = Mapping()
                        .entry("color", "green")
                        .entry("depth", "deep")))
                ))
            )
        }
    }

    @Nested inner class givenBlockMappingToSequenceOfBlockMapping : SingleDocument() {
        @BeforeEach fun setup() {
            input = "sky:\n" +
                "  -\n" +
                "    color: blue\n" +
                "    depth: high\n" +
                "  -\n" +
                "    foo: bar\n" +
                "tree:\n" +
                "  color: green\n" +
                "  depth: deep"
            expected = Document(node = Mapping()
                .entry(Mapping.Entry(key = Scalar().line("sky"), hasNlAfterKey = true, value = Sequence()
                    .item(Item(nl = true, node = Mapping()
                        .entry("color", "blue")
                        .entry("depth", "high")))
                    .item(Item(nl = true, node = Mapping()
                        .entry("foo", "bar")))
                ))
                .entry(Mapping.Entry(key = Scalar().line("tree"), hasNlAfterKey = true, value = Mapping()
                    .entry("color", "green")
                    .entry("depth", "deep")
                ))
            )
        }
    }
}
