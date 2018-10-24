package test

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED
import com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED
import helpers.catchParseException
import helpers.parse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class ScalarTest : AbstractYamlTest() {
    @Nested inner class givenPlainScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string"
            expected = Document(node = Scalar().plain().line("dummy-string"))
        }
    }

    @Nested inner class givenSingleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'dummy-string'"
            expected = Document(node = Scalar().singleQuoted().line("dummy-string"))
        }
    }

    @Nested inner class givenDoubleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "\"dummy-string\""
            expected = Document(node = Scalar().doubleQuoted().line("dummy-string"))
        }
    }

    @Nested inner class givenTwoLinePlainScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy\nstring"
            expected = Document(node = Scalar().plain().line("dummy").line("string"))
        }
    }

    @Nested inner class givenTwoLineSingleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'dummy\nstring'"
            expected = Document(node = Scalar().singleQuoted().line("dummy\nstring"))
        }
    }

    @Nested inner class givenTwoLineDoubleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "\"dummy\nstring\""
            expected = Document(node = Scalar().doubleQuoted().line("dummy\nstring"))
        }
    }

    @Nested inner class givenIndentedTwoLinePlainScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy\n  string"
            expected = Document(node = Scalar()
                .plain()
                .line("dummy")
                .line(Line().indent(2).text("string")))
        }
    }

    @Nested inner class givenIndentedTwoLineSingleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'dummy\n  string'"
            expected = Document(node = Scalar().singleQuoted().line("dummy\n  string"))
        }
    }

    @Nested inner class givenSingleQuotedScalarContainingTwoSingleQuotes : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'dummy''string'"
            expected = Document(node = Scalar().singleQuoted().line("dummy'string"))
        }
    }

    @Nested inner class givenDoubleQuotedScalarContainingSingleQuote : SingleDocument() {
        @BeforeEach fun setup() {
            input = "\"dummy'string\""
            expected = Document(node = Scalar().doubleQuoted().line("dummy'string"))
        }
    }

    // TODO double quoted scalar with escaped backslash
    // TODO double quoted scalar with escaped double quotes
    // TODO double quoted scalar with escaped other chars

    @Test fun expectSingleQuoteScalarToBeClosed() {
        val thrown = catchParseException {
            parse("'dummy")
        }

        assertThat(thrown).hasMessage("Expected a single quoted scalar to be closed at line 1 char 7")
    }

    @Test fun expectDoubleQuoteScalarToBeClosed() {
        val thrown = catchParseException {
            parse("\"dummy")
        }

        assertThat(thrown).hasMessage("Expected a double quoted scalar to be closed at line 1 char 7")
    }

    @Test fun expectPlainScalarStartNotContinueWithFlowSequence() {
        val thrown = catchParseException {
            parse("" +
                "scalar document\n" +
                "[illegal sequence]")
        }

        assertThat(thrown).hasMessage("Expected a scalar node to continue with scalar values but found flow sequence [[][LEFT SQUARE BRACKET][0x5b] at line 2 char 1")
    }

    @Test fun expectScalarStartNotContinueWithBlockSequence() {
        val thrown = catchParseException {
            parse("" +
                "scalar document\n" +
                "- illegal sequence")
        }

        assertThat(thrown).hasMessage("Expected a scalar node to continue with scalar values but found block sequence [-][HYPHEN-MINUS][0x2d] at line 2 char 1")
    }

    @Test fun expectScalarStartNotContinueWithBlockMapping() {
        val thrown = catchParseException {
            parse("" +
                "scalar document\n" +
                "key: value")
        }

        assertThat(thrown).hasMessage("Expected a scalar node to continue with scalar values but found block mapping [k][LATIN SMALL LETTER K][0x6b] at line 2 char 1")
    }

    @Test fun expectPlainScalarStartNotContinueWithFlowMapping() {
        val thrown = catchParseException {
            parse("" +
                "scalar document\n" +
                "{key: value}")
        }

        assertThat(thrown).hasMessage("Expected a scalar node to continue with scalar values but found flow mapping [{][LEFT CURLY BRACKET][0x7b] at line 2 char 1")
    }


    @Test fun expectNestedPlainScalarStartNotContinueWithFlowSequence() {
        val thrown = catchParseException {
            parse("" +
                "- scalar document\n" +
                "  [illegal sequence]")
        }

        assertThat(thrown).hasMessage("Expected a scalar node to continue with scalar values but found flow sequence [[][LEFT SQUARE BRACKET][0x5b] at line 2 char 3")
    }

    @Test fun expectNestedPlainScalarStartNotContinueWithBlockSequence() {
        val thrown = catchParseException {
            parse("" +
                "- scalar document\n" +
                "  - illegal sequence")
        }

        assertThat(thrown).hasMessage("Expected a scalar node to continue with scalar values but found block sequence [-][HYPHEN-MINUS][0x2d] at line 2 char 3")
    }

    @Test fun expectNestedPlainScalarStartNotContinueWithBlockMapping() {
        val thrown = catchParseException {
            parse("" +
                "- scalar document\n" +
                "  key: value")
        }

        assertThat(thrown).hasMessage("Expected a scalar node to continue with scalar values but found block mapping [k][LATIN SMALL LETTER K][0x6b] at line 2 char 3")
    }

    @Test fun expectNestedPlainScalarStartNotContinueWithFlowMapping() {
        val thrown = catchParseException {
            parse("" +
                "- scalar document\n" +
                "  {key: value}")
        }

        assertThat(thrown).hasMessage("Expected a scalar node to continue with scalar values but found flow mapping [{][LEFT CURLY BRACKET][0x7b] at line 2 char 3")
    }


    @Nested inner class givenOneSpaceOnlyDocument : SingleDocument() {
        @BeforeEach fun setup() {
            input = " "
            expected = Document(node = Scalar().line(Line().text("").indent(1)))
        }
    }

    @Nested inner class givenTwoSpacesOnlyDocument : SingleDocument() {
        @BeforeEach fun setup() {
            input = "  "
            expected = Document(node = Scalar().line(Line().text("").indent(2)))
        }
    }

    @Nested inner class givenIndentedScalarDocument : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    foo"
            expected = Document(node = Scalar().line(Line().text("foo").indent(4)))
        }
    }

    @Nested inner class givenIndentedScalarsDocument : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    foo\n  bar"
            expected = Document(node = Scalar()
                .line(Line().text("foo").indent(4))
                .line(Line().text("bar").indent(2)))
        }
    }

    @Nested inner class givenScalarWithSpacesBeforeAndAfter : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    dummy-string  "
            expected = Document(node = Scalar().line(Line().indent(4).text("dummy-string  ")))
        }
    }

    @Nested inner class givenPlainScalarWithComment : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string # dummy-comment"
            expected = Document(node = Scalar().line("dummy-string")
                .comment(Comment(indent = 1, text = "dummy-comment")))
        }
    }

    @Nested inner class givenSingleQuotedScalarWithComment : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'dummy-string' # dummy-comment"
            expected = Document(node = Scalar(style = SINGLE_QUOTED).line("dummy-string")
                .comment(Comment(indent = 1, text = "dummy-comment")))
        }
    }

    @Nested inner class givenDoubleQuotedScalarWithComment : SingleDocument() {
        @BeforeEach fun setup() {
            input = "\"dummy-string\" # dummy-comment"
            expected = Document(node = Scalar(style = DOUBLE_QUOTED).line("dummy-string")
                .comment(Comment(indent = 1, text = "dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithIndentedComment : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string    # dummy-comment"
            expected = Document(node = Scalar().line("dummy-string")
                .comment(Comment(indent = 4, text = "dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithCommentWithLeadingSpaces : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string #     dummy-comment"
            expected = Document(node = Scalar().line("dummy-string")
                .comment(Comment(indent = 1, text = "    dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithCommentBefore : SingleDocument() {
        @BeforeEach fun setup() {
            input = "# dummy-comment\ndummy-string"
            expected = Document()
                .prefixComment(Comment(text = "dummy-comment"))
                .node(Scalar().line("dummy-string"))
        }
    }

    @Nested inner class givenScalarWithCommentAfter : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string\n# dummy-comment"
            expected = Document()
                .node(Scalar().line("dummy-string")
                    .line("").comment(Comment(text = "dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithCommentBeforeAndAfter : SingleDocument() {
        @BeforeEach fun setup() {
            input = "# before\n" +
                "dummy-string\n" +
                "# after"
            expected = Document()
                .prefixComment(Comment(text = "before"))
                .node(Scalar().line("dummy-string")
                    .line("").comment(Comment(text = "after")))
        }
    }

    @Nested inner class givenScalarWithIndentedCommentBeforeAndAfter : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    # before\n" +
                "dummy-string\n" +
                "        # after"
            expected = Document()
                .prefixComment(Comment(indent = 4, text = "before"))
                .node(Scalar()
                    .line("dummy-string")
                    .line(Line().indent(8).text(""))
                    .comment(Comment(text = "after")))
        }
    }

    @Nested inner class givenIndentedScalarWithIndentedComment : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    dummy-string  # dummy-comment"
            expected = Document(node = Scalar()
                .line(Line().indent(4).text("dummy-string"))
                .comment(Comment(indent = 2, text = "dummy-comment")))
        }
    }

    @Nested inner class givenTwoLineScalarWithCommentInside : SingleDocument() {
        @BeforeEach fun setup() {
            input = "before # inside\nafter"
            expected = Document(node = Scalar()
                .line("before").comment(Comment(indent = 1, text = "inside"))
                .line("after"))
        }
    }

    @Nested inner class givenTwoLineScalarWithCommentAfterSecond : SingleDocument() {
        @BeforeEach fun setup() {
            input = "one\ntwo # comment"
            expected = Document(node = Scalar()
                .line("one")
                .line("two").comment(Comment(indent = 1, text = "comment")))
        }
    }

    @Nested inner class givenTwoLineScalarWithTwoComments : SingleDocument() {
        @BeforeEach fun setup() {
            input = "one # first\ntwo # second"
            expected = Document(node = Scalar()
                .line("one").comment(Comment(indent = 1, text = "first"))
                .line("two").comment(Comment(indent = 1, text = "second")))
        }
    }

    @Nested inner class givenTwoLineScalarWithTwoIndentedComments : SingleDocument() {
        @BeforeEach fun setup() {
            input = "one      # first\nlong-two # second"
            expected = Document(node = Scalar()
                .line("one").comment(Comment(indent = 6, text = "first"))
                .line("long-two").comment(Comment(indent = 1, text = "second")))
        }
    }

    @Nested inner class givenTwoLineScalarWithCommentBetween : SingleDocument() {
        @BeforeEach fun setup() {
            input = "one\n" +
                "# comment\n" +
                "two"
            expected = Document(node = Scalar()
                .line("one")
                .line("").comment(Comment(text = "comment"))
                .line("two"))
        }
    }
}
