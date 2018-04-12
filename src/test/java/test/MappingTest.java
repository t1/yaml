package test;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.MappingNode;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.SequenceNode;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static test.Helpers.parse;

class MappingTest extends AbstractYamlTest {
    @Nested class givenBlockMapping extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: blue\nsea: green";
            expected = new Document().node(new MappingNode()
                    .entry("sky", "blue")
                    .entry("sea", "green")
            );
        }
    }

    @Nested class givenBlockMappingWithSpaceInKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky high: blue\nsea deep: green";
            expected = new Document().node(new MappingNode()
                    .entry("sky high", "blue")
                    .entry("sea deep", "green")
            );
        }
    }

    @Nested class givenBlockMappingWithColonInKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:high: blue\nsea:deep: green";
            expected = new Document().node(new MappingNode()
                    .entry("sky:high", "blue")
                    .entry("sea:deep", "green")
            );
        }
    }

    @Nested class givenBlockMappingWithDoubleQuotedKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "\"sky\": blue\n\"sea\": green";
            expected = new Document().node(new MappingNode()
                    .entry(new ScalarNode().doubleQuoted().line("sky"), "blue")
                    .entry(new ScalarNode().doubleQuoted().line("sea"), "green")
            );
        }
    }

    @Nested class givenBlockMappingWithDoubleQuotedValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: \"blue\"\nsea: \"green\"";
            expected = new Document().node(new MappingNode()
                    .entry("sky", new ScalarNode().doubleQuoted().line("blue"))
                    .entry("sea", new ScalarNode().doubleQuoted().line("green"))
            );
        }
    }

    @Nested class givenBlockMappingWithDoubleQuotedKeyAndValue extends SingleDocument {
        @BeforeEach void setup() {
            input = "\"sky\": \"blue\"\n\"sea\": \"green\"";
            expected = new Document().node(new MappingNode()
                    .entry(new ScalarNode().doubleQuoted().line("sky"), new ScalarNode().doubleQuoted().line("blue"))
                    .entry(new ScalarNode().doubleQuoted().line("sea"), new ScalarNode().doubleQuoted().line("green"))
            );
        }
    }

    @Nested class givenBlockMappingWithMarkedKeys extends SingleDocument {
        @BeforeEach void setup() {
            input = "? sky: blue\n? sea: green";
            expected = new Document().node(new MappingNode()
                    .entry(new MappingNode.Entry().hasMarkedKey(true).key(new ScalarNode().line("sky")).value(new ScalarNode().line("blue")))
                    .entry(new MappingNode.Entry().hasMarkedKey(true).key(new ScalarNode().line("sea")).value(new ScalarNode().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithFirstKeyMarked extends SingleDocument {
        @BeforeEach void setup() {
            input = "? sky: blue\nsea: green";
            expected = new Document().node(new MappingNode()
                    .entry(new MappingNode.Entry().hasMarkedKey(true).key(new ScalarNode().line("sky")).value(new ScalarNode().line("blue")))
                    .entry(new MappingNode.Entry().hasMarkedKey(false).key(new ScalarNode().line("sea")).value(new ScalarNode().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithLastKeyMarked extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: blue\n? sea: green";
            expected = new Document().node(new MappingNode()
                    .entry(new MappingNode.Entry().hasMarkedKey(false).key(new ScalarNode().line("sky")).value(new ScalarNode().line("blue")))
                    .entry(new MappingNode.Entry().hasMarkedKey(true).key(new ScalarNode().line("sea")).value(new ScalarNode().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithNewLineAfterKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:\nblue\nsea:\ngreen";
            expected = new Document().node(new MappingNode()
                    .entry(new MappingNode.Entry().hasNlAfterKey(true).key(new ScalarNode().line("sky")).value(new ScalarNode().line("blue")))
                    .entry(new MappingNode.Entry().hasNlAfterKey(true).key(new ScalarNode().line("sea")).value(new ScalarNode().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithNewLineAfterFirstKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky:\nblue\nsea: green";
            expected = new Document().node(new MappingNode()
                    .entry(new MappingNode.Entry().hasNlAfterKey(true).key(new ScalarNode().line("sky")).value(new ScalarNode().line("blue")))
                    .entry(new MappingNode.Entry().hasNlAfterKey(false).key(new ScalarNode().line("sea")).value(new ScalarNode().line("green")))
            );
        }
    }

    @Nested class givenBlockMappingWithNewLineAfterLastKey extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: blue\nsea:\ngreen";
            expected = new Document().node(new MappingNode()
                    .entry(new MappingNode.Entry().hasNlAfterKey(false).key(new ScalarNode().line("sky")).value(new ScalarNode().line("blue")))
                    .entry(new MappingNode.Entry().hasNlAfterKey(true).key(new ScalarNode().line("sea")).value(new ScalarNode().line("green")))
            );
        }
    }
}
