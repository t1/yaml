package test

import com.github.t1.yaml.Yaml
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.ScalarNode
import com.github.t1.yaml.model.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path

@Suppress("ClassName")
class FileTest {
    private lateinit var yamlFile: Path

    @BeforeEach fun setup() {
        yamlFile = Files.createTempFile("test-", ".yaml")!!
        Files.write(yamlFile, "dummy-string".toByteArray(UTF_8))

        expectedDocument = Document().node(ScalarNode().line("dummy-string"))
        expectedStream = Stream().document(expectedDocument)
    }


    interface ThenShouldMatchDocument {
        @Test fun thenShouldMatchDocument() {
            assertThat(document).isEqualTo(expectedDocument)
        }
    }

    interface ThenShouldMatchStream {
        @Test fun thenShouldMatchStream() {
            assertThat(stream).isEqualTo(expectedStream)
        }
    }


    @Nested inner class givenReader {
        lateinit var reader: BufferedReader

        @BeforeEach fun setup() {
            reader = Files.newBufferedReader(yamlFile, UTF_8)!!
        }

        @Nested inner class whenParseFirst : ThenShouldMatchDocument {
            @BeforeEach fun setup() {
                document = Yaml.parseFirst(reader)
            }
        }

        @Nested inner class whenParseSingle : ThenShouldMatchDocument {
            @BeforeEach fun setup() {
                document = Yaml.parseSingle(reader)
            }
        }

        @Nested inner class whenParseAll : ThenShouldMatchStream {
            @BeforeEach fun setup() {
                stream = Yaml.parseAll(reader)
            }
        }
    }


    @Nested inner class givenInputStream {
        lateinit var stream: InputStream

        @BeforeEach fun setup() {
            stream = Files.newInputStream(yamlFile)!!
        }

        @Nested inner class whenParseFirst : ThenShouldMatchDocument {
            @BeforeEach fun setup() {
                document = Yaml.parseFirst(stream)
            }
        }

        @Nested inner class whenParseSingle : ThenShouldMatchDocument {
            @BeforeEach fun setup() {
                document = Yaml.parseSingle(stream)
            }
        }

        @Nested inner class whenParseAll : ThenShouldMatchStream {
            @BeforeEach fun setup() {
                FileTest.stream = Yaml.parseAll(stream)
            }
        }
    }

    companion object {

        private var expectedDocument: Document? = null
        private var expectedStream: Stream? = null

        private var document: Document? = null
        private var stream: Stream? = null
    }
}
