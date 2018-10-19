package test;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Mapping;
import com.github.t1.yaml.model.Mapping.Entry;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.model.Sequence;
import com.github.t1.yaml.model.Sequence.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;

class MappingTest extends AbstractYamlTest {
    @Nested class givenBlockMapping extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: blue\nsea: green";
            expected = new Document().node(new Mapping()
                .entry("sky", "blue")
                .entry("sea", "green")
            );
        }
    }

    @Nested class givenBlockMappingWithSpaceInKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky high: blue\nsea deep: green";
            expected = new Document().node(new Mapping()
                .entry("sky high", "blue")
                .entry("sea deep", "green")
            );
        }
    }

    @Nested class givenBlockMappingWithColonInKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:high: blue\nsea:deep: green";
            expected = new Document().node(new Mapping()
                .entry("sky:high", "blue")
                .entry("sea:deep", "green")
            );
        }
    }

    @Nested class givenBlockMappingWithTwoLineValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: high\n" +
                "  blue\n" +
                "sea: low\n" +
                "  green";
            expected = new Document().node(new Mapping()
                .entry("sky", new Scalar().line("high").line("blue"))
                .entry("sea", new Scalar().line("low").line("green"))
            );
        }
    }

    @Nested class givenBlockMappingWithDoubleQuotedKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "\"sky\": blue\n\"sea\": green";
            expected = new Document().node(new Mapping()
                .entry(new Scalar().doubleQuoted().line("sky"), "blue")
                .entry(new Scalar().doubleQuoted().line("sea"), "green")
            );
        }
    }

    @Nested class givenBlockMappingWithDoubleQuotedValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: \"blue\"\nsea: \"green\"";
            expected = new Document().node(new Mapping()
                .entry("sky", new Scalar().doubleQuoted().line("blue"))
                .entry("sea", new Scalar().doubleQuoted().line("green"))
            );
        }
    }

    @Nested class givenBlockMappingWithDoubleQuotedKeyAndValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "\"sky\": \"blue\"\n\"sea\": \"green\"";
            expected = new Document().node(new Mapping()
                .entry(new Scalar().doubleQuoted().line("sky"), new Scalar().doubleQuoted().line("blue"))
                .entry(new Scalar().doubleQuoted().line("sea"), new Scalar().doubleQuoted().line("green"))
            );
        }
    }

    @Disabled
    @Nested class givenBlockMappingWithTwoLineSingleQuotedValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: 'high\n" +
                "blue'\n" +
                "sea: 'low\n" +
                "green'";
            expected = new Document().node(new Mapping()
                .entry("sky", new Scalar().singleQuoted().line("high").line("blue"))
                .entry("sea", new Scalar().singleQuoted().line("low").line("green"))
            );
        }
    }

    @Nested class givenBlockMappingWithSingleQuotedKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "'sky': blue\n'sea': green";
            expected = new Document().node(new Mapping()
                .entry(new Scalar().singleQuoted().line("sky"), "blue")
                .entry(new Scalar().singleQuoted().line("sea"), "green")
            );
        }
    }

    @Nested class givenBlockMappingWithSingleQuotedValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: 'blue'\nsea: 'green'";
            expected = new Document().node(new Mapping()
                .entry("sky", new Scalar().singleQuoted().line("blue"))
                .entry("sea", new Scalar().singleQuoted().line("green"))
            );
        }
    }

    @Nested class givenBlockMappingWithTwoLineDoubleQuotedValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: \"high\n" +
                "blue\"\n" +
                "sea: \"low\n" +
                "green\"";
            expected = new Document().node(new Mapping()
                .entry("sky", new Scalar().doubleQuoted().line("high\nblue"))
                .entry("sea", new Scalar().doubleQuoted().line("low\ngreen"))
            );
        }
    }

    @Nested class givenBlockMappingWithSingleQuotedKeyAndValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "'sky': 'blue'\n'sea': 'green'";
            expected = new Document().node(new Mapping()
                .entry(new Scalar().singleQuoted().line("sky"), new Scalar().singleQuoted().line("blue"))
                .entry(new Scalar().singleQuoted().line("sea"), new Scalar().singleQuoted().line("green"))
            );
        }
    }

    @Nested class givenBlockMappingWithSpacesInSingleQuotedKeyAndValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "' sky high ': ' light blue '\n' sea deep ': ' dark green '";
            expected = new Document().node(new Mapping()
                .entry(new Scalar().singleQuoted().line(" sky high "), new Scalar().singleQuoted().line(" light blue "))
                .entry(new Scalar().singleQuoted().line(" sea deep "), new Scalar().singleQuoted().line(" dark green "))
            );
        }
    }

    @Nested class givenBlockMappingWithMarkedKeys extends SingleDocument {
        @BeforeEach void setup() {
            input = "? sky: blue\n? sea: green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry().hasMarkedKey(true).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                .entry(new Mapping.Entry().hasMarkedKey(true).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithFirstKeyMarked extends SingleDocument {
        @BeforeEach void setup() {
            input = "? sky: blue\nsea: green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry().hasMarkedKey(true).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                .entry(new Mapping.Entry().hasMarkedKey(false).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithLastKeyMarked extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: blue\n? sea: green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry().hasMarkedKey(false).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                .entry(new Mapping.Entry().hasMarkedKey(true).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithNewLineAfterKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:\n" +
                "  blue\n" +
                "sea:\n" +
                "  green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry().hasNlAfterKey(true).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                .entry(new Mapping.Entry().hasNlAfterKey(true).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithNewLineAfterFirstKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:\n" +
                "  blue\n" +
                "sea: green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry().hasNlAfterKey(true).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                .entry(new Mapping.Entry().hasNlAfterKey(false).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithNewLineAfterLastKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: blue\n" +
                "sea:\n" +
                "  green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry().hasNlAfterKey(false).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                .entry(new Mapping.Entry().hasNlAfterKey(true).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithIndentedKeys extends SingleDocument {
        @BeforeEach void setup() {
            input = " sky: blue\n" +
                " sea: green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry()
                    .key(new Scalar().line(new Line().indent(1).text("sky")))
                    .value(new Scalar().line("blue")))
                .entry(new Mapping.Entry()
                    .key(new Scalar().line(new Line().indent(1).text("sea")))
                    .value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithIndentedValues extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:   blue\nsea:     green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry()
                    .key(new Scalar().line("sky"))
                    .value(new Scalar().line(new Line().indent(2).text("blue"))))
                .entry(new Mapping.Entry()
                    .key(new Scalar().line("sea"))
                    .value(new Scalar().line(new Line().indent(4).text("green"))))
            );
        }
    }

    @Nested class givenBlockMappingWithIndentedKeysAndValues extends SingleDocument {
        @BeforeEach void setup() {
            input = " sky:   blue\n" +
                " sea:     green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry()
                    .key(new Scalar().line(new Line().indent(1).text("sky")))
                    .value(new Scalar().line(new Line().indent(2).text("blue"))))
                .entry(new Mapping.Entry()
                    .key(new Scalar().line(new Line().indent(1).text("sea")))
                    .value(new Scalar().line(new Line().indent(4).text("green"))))
            );
        }
    }

    @Nested class givenBlockMappingWithIndentedMarkedKeysAndValues extends SingleDocument {
        @BeforeEach void setup() {
            input = "?  sky:   blue\n?    sea:     green";
            expected = new Document().node(new Mapping()
                .entry(new Mapping.Entry()
                    .hasMarkedKey(true)
                    .key(new Scalar().line(new Line().indent(1).text("sky")))
                    .value(new Scalar().line(new Line().indent(2).text("blue"))))
                .entry(new Mapping.Entry()
                    .hasMarkedKey(true)
                    .key(new Scalar().line(new Line().indent(3).text("sea")))
                    .value(new Scalar().line(new Line().indent(4).text("green"))))
            );
        }
    }

    @Nested class givenBlockMappingToBlockMapping extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:\n" +
                "  color: blue\n" +
                "  depth: high\n" +
                "sea:\n" +
                "  color: green\n" +
                "  depth: deep";
            expected = new Document().node(new Mapping()
                .entry(new Entry()
                    .key(new Scalar().line("sky"))
                    .hasNlAfterKey(true)
                    .value(new Mapping()
                        .entry("color", "blue")
                        .entry("depth", "high")
                    ))
                .entry(new Entry()
                    .key(new Scalar().line("sea"))
                    .hasNlAfterKey(true)
                    .value(new Mapping()
                        .entry("color", "green")
                        .entry("depth", "deep")
                    ))
            );
        }
    }

    @Nested class givenBlockMappingToBlockMappingToBlockMapping extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:\n" +
                "  properties:\n" +
                "    color: blue\n" +
                "    depth: high\n" +
                "  size:\n" +
                "    width: 100%\n" +
                "sea:\n" +
                "  properties:\n" +
                "    color: green\n" +
                "    depth: deep";
            expected = new Document().node(new Mapping()
                .entry(new Entry()
                    .key(new Scalar().line("sky"))
                    .hasNlAfterKey(true)
                    .value(new Mapping()
                        .entry(new Entry()
                            .key(new Scalar().line("properties"))
                            .hasNlAfterKey(true)
                            .value(new Mapping()
                                .entry("color", "blue")
                                .entry("depth", "high")))
                        .entry(new Entry()
                            .key(new Scalar().line("size"))
                            .hasNlAfterKey(true)
                            .value(new Mapping()
                                .entry("width", "100%")
                            )
                        )
                    ))
                .entry(new Entry()
                    .key(new Scalar().line("sea"))
                    .hasNlAfterKey(true)
                    .value(new Mapping()
                        .entry(new Entry()
                            .key(new Scalar().line("properties"))
                            .hasNlAfterKey(true)
                            .value(new Mapping()
                                .entry("color", "green")
                                .entry("depth", "deep")))
                    ))
            );
        }
    }

    @Disabled
    @Nested class givenBlockMappingToSequenceOfBlockMapping extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:\n" +
                "  -\n" +
                "    color: blue\n" +
                "    depth: high\n" +
                "  -\n" +
                "    foo: bar\n" +
                "sea:\n" +
                "  color: green\n" +
                "  depth: deep";
            expected = new Document().node(new Mapping()
                .entry(new Entry()
                    .key(new Scalar().line("sky"))
                    .hasNlAfterKey(true)
                    .value(new Sequence()
                        .item(new Item()
                            .nl(true)
                            .node(new Mapping()
                                .entry("color", "blue")
                                .entry("depth", "high")))
                        .item(new Item()
                            .nl(true)
                            .node(new Mapping()
                                .entry("foo", "bar")))
                    ))
                .entry(new Entry()
                    .key(new Scalar().line("sea"))
                    .hasNlAfterKey(true)
                    .value(new Mapping()
                        .entry("color", "green")
                        .entry("depth", "deep")
                    ))
            );
        }
    }
}
