package test

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Directive
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Scalar
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested

@Suppress("ClassName")
class StreamTest : AbstractYamlTest() {
    @Nested inner class givenEmptyStream {
        @BeforeEach fun setup() {
            input = ""
        }

        @Nested inner class whenParseAll : ParseAll(), ThenIsEmptyStream, ThenStreamToStringIsSameAsInput

        @Nested inner class whenParseFirst : ParseFirst(), ThenThrowsExpectedAtLeastOneDocumentButFoundNone

        @Nested inner class whenParseSingle : ParseSingle(), ThenThrowsExpectedExactlyOneDocumentButFoundNone
    }

    @Disabled @Nested inner class givenInvalidSecondDocument {
        @BeforeEach fun setup() {
            input = "%YAML 1.2\n---\nvalid document\n...{"
            expected = Document(hasDirectivesEndMarker = true).directive(Directive.YAML_VERSION).node(Scalar().line("valid document"))
        }

        @Nested inner class whenParseAll : ParseAll(), ThenThrowsInvalid

        @Nested inner class whenParseFirst : ParseFirst(), ThenIsExpectedDocument

        @Nested inner class whenParseSingle : ParseSingle(), ThenThrowsExpectedExactlyOneDocumentButFoundMore
    }

    @Nested inner class givenOneLineCommentOnlyStream : SingleDocument() {
        @BeforeEach fun setup() {
            input = "# test comment"
            expected = Document().prefixComment(Comment(indent = 0, text = "test comment"))
        }
    }

    @Nested inner class givenOneLineCommentOnlyWithLeadingSpacesStream : SingleDocument() {
        @BeforeEach fun setup() {
            input = "#     test comment"
            expected = Document().prefixComment(Comment(indent = 0, text = "    test comment"))
        }
    }

    @Nested inner class givenOneLineIndentedCommentOnlyStream : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    # test comment"
            expected = Document().prefixComment(Comment(indent = 4, text = "test comment"))
        }
    }

    @Nested inner class givenTwoLineCommentOnlyStream : SingleDocument() {
        @BeforeEach fun setup() {
            input = "# test comment\n# line two"
            expected = Document()
                .prefixComment(Comment(indent = 0, text = "test comment"))
                .prefixComment(Comment(indent = 0, text = "line two"))
        }
    }
}
