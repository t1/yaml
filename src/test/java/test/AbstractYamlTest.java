package test;

import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static test.Helpers.withoutTrailingNl;

public class AbstractYamlTest {
    ///////////////////////////////////// inputs
    static String input;
    static Document expected;

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

    private static void rethrow() {
        if (thrown != null)
            throw thrown;
    }

    ///////////////////////////////////////////////////////////////////////// WHEN

    private <T> T when(Function<String, T> function) {
        AtomicReference<T> result = new AtomicReference<>();
        Throwable e = catchThrowable(() -> result.set(function.apply(input)));
        if (e != null && !(e instanceof YamlParseException))
            throw new RuntimeException("expected YamlParseException but got a " + e.getClass().getName(), e);
        thrown = (YamlParseException) e;
        return result.get();
    }

    class ParseAll {
        @BeforeEach void setup() { stream = when(Yaml::parseAll); }
    }

    class ParseFirst {
        @BeforeEach void setup() { document = when(Yaml::parseFirst); }
    }

    class ParseSingle {
        @BeforeEach void setup() { document = when(Yaml::parseSingle); }
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    interface ThenIsEmptyStream {
        @Test default void thenStreamIsEmpty() {
            rethrow();
            assertThat(stream.documents()).isEmpty();
        }
    }

    interface ThenIsExpectedStream {
        @Test default void thenStreamIsExpected() {
            rethrow();
            assertThat(stream).isEqualTo(expectedStream());
        }
    }

    interface ThenIsExpectedCanonicalStream {
        @Test default void thenCanonicalStreamIsExpected() {
            if (thrown != null)
                throw thrown;
            assertThat(stream.canonicalize()).isEqualTo(expectedCanonicalStream());
        }
    }

    interface ThenIsExpectedDocument {
        @Test default void thenDocumentIsExpected() {
            rethrow();
            assertThat(document.isEmpty()).isEqualTo(expected.isEmpty());
            assertThat(document.hasDirectives()).isEqualTo(expected.hasDirectives());
            assertThat(document).isEqualTo(expected);
        }
    }

    interface ThenIsExpectedCanonicalDocument {
        @Test default void thenCanonicalDocumentIsExpected() {
            rethrow();
            assertThat(document.canonicalize()).isEqualTo(expectedCanonicalDocument());
        }
    }

    interface ThenDocumentToStringIsSameAsInput {
        @Test default void thenDocumentToStringIsSameAsInput() {
            rethrow();
            assertThat(withoutTrailingNl(document, Yaml::present)).isEqualTo(input);
        }

    }

    interface ThenStreamToStringIsSameAsInput {
        @Test default void thenStreamToStringIsSameAsInput() {
            rethrow();
            assertThat(withoutTrailingNl(stream, Yaml::present)).isEqualTo(input);
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

    /////////////////////////////////////////////////////////////////////////

    class SingleDocument {
        @Nested class whenParseAll extends ParseAll implements ThenIsExpectedStream, ThenStreamToStringIsSameAsInput, ThenIsExpectedCanonicalStream {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument, ThenDocumentToStringIsSameAsInput, ThenIsExpectedCanonicalDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenIsExpectedDocument, ThenDocumentToStringIsSameAsInput, ThenIsExpectedCanonicalDocument {}
    }
}
