import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Experimental Test Style:
 * GIVEN: Nested class: setup in anonymous constructors -> GIVEN-reuse within the nested class
 * WHEN: Nested init-lower-case class in a GIVEN extending a similarily named class -> WHEN-reuse
 * THEN: Interfaces accessing static variables -> THEN-reuse
 */
class YamlTest {
    static String yaml;
    static Document expected;

    static Stream stream;
    static Document document;
    static YamlParseException thrown;


    ///////////////////////////////////////////////////////////////////////// WHEN

    private <T> T when(Function<String, T> function) {
        AtomicReference<T> result = new AtomicReference<>();
        thrown = catchThrowableOfType(() -> result.set(function.apply(yaml)), YamlParseException.class);
        return result.get();
    }

    private class ParseAll {
        { stream = when(Yaml::parseAll); }
    }

    private class ParseFirst {
        { document = when(Yaml::parseFirst); }
    }

    private class ParseSingle {
        { document = when(Yaml::parseSingle); }
    }


    ///////////////////////////////////////////////////////////////////////// THEN

    interface ThenIsEmptyStream {
        @Test default void isExpected() { assertThat(stream.documents()).isEmpty(); }
    }

    interface ThenIsExpectedStream {
        @Test default void isExpected() { assertThat(stream.documents()).isEqualTo(singletonList(expected)); }
    }

    interface ThenIsExpectedDocument {
        @Test default void isExpected() { assertThat(document).isEqualTo(expected); }
    }


    interface ThenThrowsExpectedAtLeastOne {
        @Test default void throwsExpectedAtLeastOne() { assertThat(thrown).hasMessage("expected at least one document, but found none"); }
    }

    interface ThenThrowsExpectedExactlyOne {
        @Test default void throwsExpectedExactlyOne() { assertThat(thrown).hasMessage("expected exactly one document, but found 0"); }
    }


    ///////////////////////////////////////////////////////////////////////// GIVEN

    @Nested class givenEmptyDocument {
        { yaml = ""; }

        @Nested class whenParseAll extends ParseAll implements ThenIsEmptyStream {}

        @Nested class whenParseFirst extends ParseFirst implements ThenThrowsExpectedAtLeastOne {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOne {}
    }


    @Nested class givenSpaceOnlyDocument {
        {
            yaml = " ";
            expected = new Document();
        }

        @Nested class whenParseAll extends ParseAll implements ThenIsExpectedStream {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenIsExpectedDocument {}
    }


    @Nested class givenCommentOnlyDocument {
        {
            yaml = "# test comment";
            expected = new Document().comment(new Comment().text("test comment"));
        }

        @Nested class whenParseAll extends ParseAll implements ThenIsExpectedStream {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenIsExpectedDocument {}
    }
}
