package test

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Directive
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.ScalarNode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested

@Suppress("ClassName") class StreamTest : AbstractYamlTest() {
    @Nested inner class givenEmptyStream {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = ""
        }

        @Nested inner class whenParseAll : AbstractYamlTest.ParseAll(), AbstractYamlTest.ThenIsEmptyStream, AbstractYamlTest.ThenStreamToStringIsSameAsInput

        @Nested inner class whenParseFirst : AbstractYamlTest.ParseFirst(), AbstractYamlTest.ThenThrowsExpectedAtLeastOneDocumentButFoundNone

        @Nested inner class whenParseSingle : AbstractYamlTest.ParseSingle(), AbstractYamlTest.ThenThrowsExpectedExactlyOneDocumentButFoundNone
    }

    @Nested inner class givenInvalidSecondDocument {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "%YAML 1.2\n---\nvalid document\n...{"
            AbstractYamlTest.expected = Document().directive(Directive.YAML_VERSION).node(ScalarNode().line("valid document")).hasDocumentEndMarker(true)
        }

        @Nested inner class whenParseAll : AbstractYamlTest.ParseAll(), AbstractYamlTest.ThenThrowsInvalid

        @Nested inner class whenParseFirst : AbstractYamlTest.ParseFirst(), AbstractYamlTest.ThenIsExpectedDocument

        @Nested inner class whenParseSingle : AbstractYamlTest.ParseSingle(), AbstractYamlTest.ThenThrowsExpectedExactlyOneDocumentButFoundMore
    }

    @Nested inner class givenOneLineCommentOnlyStream : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "# test comment"
            AbstractYamlTest.expected = Document().prefixComment(Comment().indent(0).text("test comment"))
        }
    }

    @Nested inner class givenOneLineCommentOnlyWithLeadingSpacesStream : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "#     test comment"
            AbstractYamlTest.expected = Document().prefixComment(Comment().indent(0).text("    test comment"))
        }
    }

    @Nested inner class givenOneLineIndentedCommentOnlyStream : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "    # test comment"
            AbstractYamlTest.expected = Document().prefixComment(Comment().indent(4).text("test comment"))
        }
    }

    @Nested inner class givenTwoLineCommentOnlyStream : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "# test comment\n# line two"
            AbstractYamlTest.expected = Document()
                .prefixComment(Comment().indent(0).text("test comment"))
                .prefixComment(Comment().indent(0).text("line two"))
        }
    }
}
