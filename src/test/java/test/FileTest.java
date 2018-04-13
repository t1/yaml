package test;

import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class FileTest {
    private Path yamlFile;

    private static Document expectedDocument;
    private static Stream expectedStream;

    private static Document document;
    private static Stream stream;

    @BeforeEach @SneakyThrows(IOException.class) void setup() {
        yamlFile = Files.createTempFile("test-", ".yaml");
        Files.write(yamlFile, "dummy-string".getBytes(UTF_8));

        expectedDocument = new Document().node(new ScalarNode().line("dummy-string"));
        expectedStream = new Stream().document(expectedDocument);
    }


    interface ThenShouldMatchDocument {
        @Test default void thenShouldMatchDocument() { assertThat(document).isEqualTo(expectedDocument); }
    }

    interface ThenShouldMatchStream {
        @Test default void thenShouldMatchStream() { assertThat(stream).isEqualTo(expectedStream); }
    }


    @Nested class givenReader {
        BufferedReader reader;

        @BeforeEach @SneakyThrows(IOException.class) void setUp() { reader = Files.newBufferedReader(yamlFile, UTF_8); }

        @Nested class whenParseFirst implements ThenShouldMatchDocument {
            @BeforeEach void setup() { document = Yaml.parseFirst(reader); }
        }

        @Nested class whenParseSingle implements ThenShouldMatchDocument {
            @BeforeEach void setup() { document = Yaml.parseSingle(reader); }
        }

        @Nested class whenParseAll implements ThenShouldMatchStream {
            @BeforeEach void setup() { stream = Yaml.parseAll(reader); }
        }
    }


    @Nested class givenInputStream {
        InputStream stream;

        @BeforeEach @SneakyThrows(IOException.class) void setUp() { stream = Files.newInputStream(yamlFile); }

        @Nested class whenParseFirst implements ThenShouldMatchDocument {
            @BeforeEach void setup() { document = Yaml.parseFirst(stream); }
        }

        @Nested class whenParseSingle implements ThenShouldMatchDocument {
            @BeforeEach void setup() { document = Yaml.parseSingle(stream); }
        }

        @Nested class whenParseAll implements ThenShouldMatchStream {
            @BeforeEach void setup() { FileTest.stream = Yaml.parseAll(stream); }
        }
    }
}
