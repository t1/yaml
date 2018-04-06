import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class YamlTest {
    ///////////////////////////////////// inputs
    private static String input;
    private static Document expected;
    private static String expectedCanonical;

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
        @Test default void thenStreamIsEmpty() { assertThat(stream.documents()).isEmpty(); }
    }

    interface ThenIsExpectedStream {
        @Test default void thenStreamIsExpected() { assertThat(stream.documents()).isEqualTo(singletonList(expected)); }
    }

    interface ThenIsExpectedDocument {
        @Test default void thenDocumentIsExpected() { assertThat(document).isEqualTo(expected); }
    }

    interface ThenDocumentToStringIsSameAsInput {
        @Test default void thenDocumentToStringIsSameAsInput() { assertThat(document.toString()).isEqualTo(input); }
    }

    interface ThenStreamToStringIsSameAsInput {
        @Test default void thenStreamToStringIsSameAsInput() { assertThat(stream.toString()).isEqualTo(input); }
    }


    interface ThenThrowsExpectedAtLeastOne {
        @Test default void thenThrowsExpectedAtLeastOne() { assertThat(thrown).hasMessage("expected at least one document, but found none"); }
    }

    interface ThenThrowsExpectedExactlyOne {
        @Test default void thenThrowsExpectedExactlyOne() { assertThat(thrown).hasMessage("expected exactly one document, but found 0"); }
    }


    ///////////////////////////////////////////////////////////////////////// GIVEN

    private class ThenDocument {
        @Nested class whenParseAll extends ParseAll implements ThenIsExpectedStream, ThenStreamToStringIsSameAsInput {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument, ThenDocumentToStringIsSameAsInput {}

        @Nested class whenParseSingle extends ParseSingle implements ThenIsExpectedDocument, ThenDocumentToStringIsSameAsInput {}
    }

    private class ThenEmptyStream {
        @Nested class whenParseAll extends ParseAll implements ThenIsEmptyStream, ThenStreamToStringIsSameAsInput {}

        @Nested class whenParseFirst extends ParseFirst implements ThenThrowsExpectedAtLeastOne {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOne {}
    }

    @Nested class givenEmptyDocument extends ThenEmptyStream {
        @BeforeEach void setup() { input = ""; }
    }

    @Nested class givenOneLineCommentOnlyStream extends ThenEmptyStream {
        @BeforeEach void setup() {
            input = "# test comment";
        }
    }

    @Nested class givenTwoLineCommentOnlyStream extends ThenEmptyStream {
        @BeforeEach void setup() {
            input = "# test comment\n# line two";
        }
    }

    @Nested class givenSpaceOnlyDocument extends ThenDocument {
        @BeforeEach void setup() {
            input = " ";
            expected = new Document().node(new ScalarNode().text(" "));
        }
    }

    @Nested class givenScalarDocument extends ThenDocument {
        @BeforeEach void setup() {
            input = "dummy-string";
            expected = new Document().node(new ScalarNode().text("dummy-string"));
        }
    }
}
