package test

import com.github.t1.yaml.Yaml
import com.github.t1.yaml.dump.Canonicalizer
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Stream
import com.github.t1.yaml.parser.YamlParseException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicReference

open class AbstractYamlTest {

    @AfterEach fun cleanup() {
        input = null
        expected = null

        stream = null
        document = null
        thrown = null
    }

    ///////////////////////////////////////////////////////////////////////// WHEN

    private fun <T> `when`(function: (String) -> T): T {
        val result = AtomicReference<T>()
        val e = catchThrowable { result.set(function(input!!)) }
        if (e != null && e !is YamlParseException)
            throw RuntimeException("expected YamlParseException but got a ${e::class.simpleName}", e)
        thrown = e as? YamlParseException
        return result.get()
    }

    open inner class ParseAll {
        @BeforeEach fun setup() {
            stream = `when` { Yaml.parseAll(it) }
        }
    }

    open inner class ParseFirst {
        @BeforeEach fun setup() {
            document = `when` { Yaml.parseFirst(it) }
        }
    }

    open inner class ParseSingle {
        @BeforeEach fun setup() {
            document = `when` { Yaml.parseSingle(it) }
        }
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    interface ThenIsEmptyStream {
        @Test fun thenStreamIsEmpty() {
            rethrow()
            assertThat(stream!!.documents).isEmpty()
        }
    }

    interface ThenIsExpectedStream {
        @Test fun thenStreamIsExpected() {
            rethrow()
            assertThat(stream).isEqualTo(expectedStream())
        }
    }

    interface ThenIsExpectedCanonicalStream {
        @Test fun thenCanonicalStreamIsExpected() {
            if (thrown != null)
                throw thrown!!
            Yaml.canonicalize(stream!!)
            assertThat(stream).isEqualTo(expectedCanonicalStream())
        }
    }

    interface ThenIsExpectedDocument {
        @Test fun thenDocumentIsExpected() {
            rethrow()
            assertThat(document!!.isEmpty).describedAs("isEmpty").isEqualTo(expected!!.isEmpty)
            assertThat(document!!.hasDirectives()).describedAs("hasDirectives").isEqualTo(expected!!.hasDirectives())
            assertThat(document).isEqualTo(expected)
        }
    }

    interface ThenIsExpectedCanonicalDocument {
        @Test fun thenCanonicalDocumentIsExpected() {
            rethrow()
            document!!.guide(Canonicalizer())
            assertThat(document).isEqualTo(expectedCanonicalDocument())
        }
    }

    interface ThenDocumentToStringIsSameAsInput {
        @Test fun thenDocumentToStringIsSameAsInput() {
            rethrow()
            assertThat(withoutTrailingNl(Stream().document(document!!))).isEqualTo(input)
        }
    }

    interface ThenStreamToStringIsSameAsInput {
        @Test fun thenStreamToStringIsSameAsInput() {
            rethrow()
            assertThat(withoutTrailingNl(stream)).isEqualTo(input)
        }
    }


    interface ThenThrowsInvalid {
        @Test fun thenThrowsInvalid() {
            assertThat(thrown).hasMessageStartingWith("unexpected [{][LEFT CURLY BRACKET][0x7b]")
        }
    }

    interface ThenThrowsExpectedAtLeastOneDocumentButFoundNone {
        @Test fun thenThrowsExpectedAtLeastOne() {
            assertThat(thrown).hasMessage("expected at least one document, but found none")
        }
    }

    interface ThenThrowsExpectedExactlyOneDocumentButFoundNone {
        @Test fun thenThrowsExpectedExactlyOne() {
            assertThat(thrown).hasMessage("expected exactly one document, but found none")
        }
    }

    interface ThenThrowsExpectedExactlyOneDocumentButFoundMore {
        @Test fun thenThrowsExpectedExactlyOneDocumentButFoundMore() {
            assertThat(thrown).hasMessageStartingWith("expected exactly one document, but found more: ")
        }
    }

    /////////////////////////////////////////////////////////////////////////

    @Suppress("ClassName")
    open inner class SingleDocument {
        @Nested inner class whenParseAll : ParseAll(), ThenIsExpectedStream, ThenStreamToStringIsSameAsInput, ThenIsExpectedCanonicalStream

        @Nested inner class whenParseFirst : ParseFirst(), ThenIsExpectedDocument, ThenDocumentToStringIsSameAsInput, ThenIsExpectedCanonicalDocument

        @Nested inner class whenParseSingle : ParseSingle(), ThenIsExpectedDocument, ThenDocumentToStringIsSameAsInput, ThenIsExpectedCanonicalDocument
    }

    companion object {
        ///////////////////////////////////// inputs
        var input: String? = null
        var expected: Document? = null

        private fun expectedStream(): Stream {
            return Stream().document(expected!!)
        }

        private fun expectedCanonicalStream(): Stream {
            val stream = expectedStream()
            Yaml.canonicalize(stream)
            return stream
        }

        private fun expectedCanonicalDocument(): Document {
            expected!!.guide(Canonicalizer())
            return expected!!
        }

        ///////////////////////////////////// outputs
        private var stream: Stream? = null
        private var document: Document? = null
        private var thrown: YamlParseException? = null

        private fun rethrow() {
            if (thrown != null)
                throw thrown!!
        }
    }
}
