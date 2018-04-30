package test;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Mapping;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import org.junit.jupiter.api.BeforeEach;
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
            input = "sky:\nblue\nsea:\ngreen";
            expected = new Document().node(new Mapping()
                    .entry(new Mapping.Entry().hasNlAfterKey(true).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                    .entry(new Mapping.Entry().hasNlAfterKey(true).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithNewLineAfterFirstKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:\nblue\nsea: green";
            expected = new Document().node(new Mapping()
                    .entry(new Mapping.Entry().hasNlAfterKey(true).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                    .entry(new Mapping.Entry().hasNlAfterKey(false).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithNewLineAfterLastKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: blue\nsea:\ngreen";
            expected = new Document().node(new Mapping()
                    .entry(new Mapping.Entry().hasNlAfterKey(false).key(new Scalar().line("sky")).value(new Scalar().line("blue")))
                    .entry(new Mapping.Entry().hasNlAfterKey(true).key(new Scalar().line("sea")).value(new Scalar().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithIndentedKeys extends SingleDocument {
        @BeforeEach void setup() {
            input = " sky: blue\n   sea: green";
            expected = new Document().node(new Mapping()
                    .entry(new Mapping.Entry()
                            .key(new Scalar().line(new Line().indent(1).text("sky")))
                            .value(new Scalar().line("blue")))
                    .entry(new Mapping.Entry()
                            .key(new Scalar().line(new Line().indent(3).text("sea")))
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
            input = " sky:   blue\n   sea:     green";
            expected = new Document().node(new Mapping()
                    .entry(new Mapping.Entry()
                            .key(new Scalar().line(new Line().indent(1).text("sky")))
                            .value(new Scalar().line(new Line().indent(2).text("blue"))))
                    .entry(new Mapping.Entry()
                            .key(new Scalar().line(new Line().indent(3).text("sea")))
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
}
