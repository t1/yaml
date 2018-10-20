package test;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;

class StreamTest extends AbstractYamlTest {
    @Nested class givenEmptyStream {
        @BeforeEach void setup() { input = ""; }

        @Nested class whenParseAll extends ParseAll implements ThenIsEmptyStream, ThenStreamToStringIsSameAsInput {}

        @Nested class whenParseFirst extends ParseFirst implements ThenThrowsExpectedAtLeastOneDocumentButFoundNone {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOneDocumentButFoundNone {}
    }

    @Disabled @Nested class givenInvalidSecondDocument {
        @BeforeEach void setup() {
            input = "%YAML 1.2\n---\nvalid document\n...{";
            expected = new Document().directive(Directive.YAML_VERSION).node(new Scalar().line("valid document")).hasDocumentEndMarker(true);
        }

        @Nested class whenParseAll extends ParseAll implements ThenThrowsInvalid {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOneDocumentButFoundMore {}
    }

    @Nested class givenOneLineCommentOnlyStream extends SingleDocument {
        @BeforeEach void setup() {
            input = "# test comment";
            expected = new Document().prefixComment(new Comment().indent(0).text("test comment"));
        }
    }

    @Nested class givenOneLineCommentOnlyWithLeadingSpacesStream extends SingleDocument {
        @BeforeEach void setup() {
            input = "#     test comment";
            expected = new Document().prefixComment(new Comment().indent(0).text("    test comment"));
        }
    }

    @Nested class givenOneLineIndentedCommentOnlyStream extends SingleDocument {
        @BeforeEach void setup() {
            input = "    # test comment";
            expected = new Document().prefixComment(new Comment().indent(4).text("test comment"));
        }
    }

    @Nested class givenTwoLineCommentOnlyStream extends SingleDocument {
        @BeforeEach void setup() {
            input = "# test comment\n# line two";
            expected = new Document()
                    .prefixComment(new Comment().indent(0).text("test comment"))
                    .prefixComment(new Comment().indent(0).text("line two"));
        }
    }
}
