package test;

import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.MappingNode;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.SequenceNode;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static test.Helpers.toStringWithoutTrailingNl;

class YamlTest {
    ///////////////////////////////////// inputs
    private static String input;
    private static Document expected;

    private static Stream expectedStream() { return new Stream().document(expected); }

    private static Stream expectedCanonicalStream() { return expectedStream().canonicalize(); }

    private static Document expectedCanonicalDocument() { return expected.canonicalize(); }

    ///////////////////////////////////// outputs
    private static Stream stream;
    private static Document document;
    private static YamlParseException thrown;

    @AfterEach void cleanup() {
        input = null;
        expected = null;

        stream = null;
        document = null;
        thrown = null;
    }


    ///////////////////////////////////////////////////////////////////////// WHEN

    private <T> T when(Function<String, T> function) {
        AtomicReference<T> result = new AtomicReference<>();
        thrown = catchThrowableOfType(() -> result.set(function.apply(input)), YamlParseException.class);
        return result.get();
    }

    private class ParseAll {
        @BeforeEach void setup() { stream = when(Yaml::parseAll); }
    }

    private class ParseFirst {
        @BeforeEach void setup() { document = when(Yaml::parseFirst); }
    }

    private class ParseSingle {
        @BeforeEach void setup() { document = when(Yaml::parseSingle); }
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    interface ThenIsEmptyStream {
        @Test default void thenStreamIsEmpty() {
            assertThat(thrown).isNull();
            assertThat(stream.documents()).isEmpty();
        }
    }

    interface ThenIsExpectedStream {
        @Test default void thenStreamIsExpected() {
            assertThat(thrown).isNull();
            assertThat(stream).isEqualTo(expectedStream());
        }
    }

    interface ThenIsExpectedCanonicalStream {
        @Test default void thenCanonicalStreamIsExpected() {
            assertThat(thrown).isNull();
            assertThat(stream.canonicalize()).isEqualTo(expectedCanonicalStream());
        }
    }

    interface ThenIsExpectedDocument {
        @Test default void thenDocumentIsExpected() {
            assertThat(thrown).isNull();
            assertThat(document).isEqualTo(expected);
        }
    }

    interface ThenIsExpectedCanonicalDocument {
        @Test default void thenCanonicalDocumentIsExpected() {
            assertThat(thrown).isNull();
            assertThat(document.canonicalize()).isEqualTo(expectedCanonicalDocument());
        }
    }

    interface ThenDocumentToStringIsSameAsInput {
        @Test default void thenDocumentToStringIsSameAsInput() {
            assertThat(thrown).isNull();
            assertThat(toStringWithoutTrailingNl(document)).isEqualTo(input);
        }

    }

    interface ThenStreamToStringIsSameAsInput {
        @Test default void thenStreamToStringIsSameAsInput() {
            assertThat(thrown).isNull();
            assertThat(toStringWithoutTrailingNl(stream)).isEqualTo(input);
        }
    }


    interface ThenThrowsInvalid {
        @Test default void thenThrowsInvalid() { assertThat(thrown).hasMessageStartingWith("unexpected [{][LEFT CURLY BRACKET][0x7b]"); }
    }

    interface ThenThrowsExpectedAtLeastOneDocumentButFoundNone {
        @Test default void thenThrowsExpectedAtLeastOne() { assertThat(thrown).hasMessage("expected at least one document, but found none"); }
    }

    interface ThenThrowsExpectedExactlyOneDocumentButFoundNone {
        @Test default void thenThrowsExpectedExactlyOne() { assertThat(thrown).hasMessage("expected exactly one document, but found none"); }
    }

    interface ThenThrowsExpectedExactlyOneDocumentButFoundMore {
        @Test default void thenThrowsExpectedExactlyOneDocumentButFoundMore() { assertThat(thrown).hasMessageStartingWith("expected exactly one document, but found more: "); }
    }


    ///////////////////////////////////////////////////////////////////////// GIVEN

    ////////////////////////////////////////////////////////////////////////////////////////
    class EmptyStream {
        @Nested class whenParseAll extends ParseAll implements ThenIsEmptyStream, ThenStreamToStringIsSameAsInput {}

        @Nested class whenParseFirst extends ParseFirst implements ThenThrowsExpectedAtLeastOneDocumentButFoundNone {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOneDocumentButFoundNone {}
    }

    @Nested class givenEmptyDocument extends EmptyStream {
        @BeforeEach void setup() { input = ""; }
    }

    @Nested class givenInvalidSecondDocument {
        @BeforeEach void setup() {
            input = "%YAML 1.2\n---\nvalid document\n...{";
            expected = new Document().directive(Directive.YAML_VERSION).node(new ScalarNode().line("valid document")).hasDocumentEndMarker(true);
        }

        @Nested class whenParseAll extends ParseAll implements ThenThrowsInvalid {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOneDocumentButFoundMore {}
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    class SingleDocument {
        @Nested class whenParseAll extends ParseAll implements ThenIsExpectedStream, ThenStreamToStringIsSameAsInput, ThenIsExpectedCanonicalStream {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument, ThenDocumentToStringIsSameAsInput, ThenIsExpectedCanonicalDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenIsExpectedDocument, ThenDocumentToStringIsSameAsInput, ThenIsExpectedCanonicalDocument {}
    }

    @Nested class givenOneLineCommentOnlyStream extends SingleDocument {
        @BeforeEach void setup() {
            input = "# test comment";
            expected = new Document().prefixComment(new Comment().text("test comment"));
        }
    }

    @Nested class givenTwoLineCommentOnlyStream extends SingleDocument {
        @BeforeEach void setup() {
            input = "# test comment\n# line two";
            expected = new Document().prefixComment(new Comment().text("test comment")).prefixComment(new Comment().text("line two"));
        }
    }

    @Nested class givenSpaceOnlyDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = " ";
            expected = new Document().node(new ScalarNode().line(" "));
        }
    }

    @Nested class givenScalarDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string";
            expected = new Document().node(new ScalarNode().line("dummy-string"));
        }
    }

    @Nested class givenSequenceDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = "- one\n- two";
            expected = new Document().node(new SequenceNode()
                    .entry(new ScalarNode().line("one"))
                    .entry(new ScalarNode().line("two"))
            );
        }
    }

    @Nested class givenMappingDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = "sky: blue\nsea: green";
            expected = new Document().node(new MappingNode()
                    .entry("sky", "blue")
                    .entry("sea", "green")
            );
        }
    }
}
