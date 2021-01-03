package test

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.parser.YamlParseException
import helpers.parse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class ScalarTest : AbstractYamlTest() {
    @Nested inner class givenPlainScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string"
            expected = Document().node(Scalar().plain().line("dummy-string"))
        }
    }

    @Nested inner class givenSingleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'dummy-string'"
            expected = Document().node(Scalar().singleQuoted().line("dummy-string"))
        }
    }

    @Nested inner class givenDoubleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "\"dummy-string\""
            expected = Document().node(Scalar().doubleQuoted().line("dummy-string"))
        }
    }

    @Nested inner class givenTwoLinePlainScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy\n" + "string"
            expected = Document().node(Scalar().plain().line("dummy").line("string"))
        }
    }

    @Nested inner class givenTwoLineSingleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'dummy\n" + "string'"
            expected = Document().node(Scalar().singleQuoted().line("dummy").line("string"))
        }
    }

    @Nested inner class givenTwoLineDoubleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "\"dummy\n" + "string\""
            expected = Document().node(Scalar().doubleQuoted().line("dummy\nstring"))
        }
    }

    @Nested inner class givenIndentedTwoLinePlainScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy\n" + "  string"
            expected = Document().node(Scalar()
                .plain()
                .line("dummy")
                .line(Line().indent(2).text("string")))
        }
    }

    @Nested inner class givenIndentedTwoLineSingleQuotedScalar : SingleDocument() {
        @BeforeEach fun setup() {
            input = "'dummy\n" + "  string'"
            expected = Document().node(Scalar()
                .singleQuoted()
                .line("dummy")
                .line(Line().indent(2).text("string")))
        }
    }

    @Disabled @Test fun expectScalarStartNotContinueWithFlowSequence() {
        val thrown = catchThrowable {
            parse("" +
                "scalar document\n" +
                "[illegal sequence]")
        }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found " + "flow sequence [[][LEFT SQUARE BRACKET][0x5b] at line 2 char 1")
    }

    @Disabled @Test fun expectScalarStartNotContinueWithBlockSequence() {
        val thrown = catchThrowable {
            parse("" +
                "scalar document\n" +
                "- illegal sequence")
        }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found " + "block sequence [-][HYPHEN-MINUS][0x2d] at line 2 char 1")
    }

    @Disabled @Test fun expectScalarStartNotContinueWithBlockMapping() {
        val thrown = catchThrowable {
            parse("" +
                "scalar document\n" +
                "key: value")
        }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found " + "block mapping [k][LATIN SMALL LETTER K][0x6b] at line 2 char 1")
    }

    @Disabled @Test fun expectScalarStartNotContinueWithFlowMapping() {
        val thrown = catchThrowable {
            parse("" +
                "scalar document\n" +
                "{key: value}")
        }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found " + "flow mapping [{][LEFT CURLY BRACKET][0x7b] at line 2 char 1")
    }


    @Disabled @Test fun expectNestedScalarStartNotContinueWithFlowSequence() {
        val thrown = catchThrowable {
            parse("" +
                "- scalar document\n" +
                "  [illegal sequence]")
        }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found " + "flow sequence [[][LEFT SQUARE BRACKET][0x5b] at line 2 char 3")
    }

    @Disabled @Test fun expectNestedScalarStartNotContinueWithBlockSequence() {
        val thrown = catchThrowable {
            parse("" +
                "- scalar document\n" +
                "  - illegal sequence")
        }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found " + "block sequence [-][HYPHEN-MINUS][0x2d] at line 2 char 3")
    }

    @Disabled @Test fun expectNestedScalarStartNotContinueWithBlockMapping() {
        val thrown = catchThrowable {
            parse("" +
                "- scalar document\n" +
                "  key: value")
        }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found " + "block mapping [k][LATIN SMALL LETTER K][0x6b] at line 2 char 3")
    }

    @Disabled @Test fun expectNestedScalarStartNotContinueWithFlowMapping() {
        val thrown = catchThrowable {
            parse("" +
                "- scalar document\n" +
                "  {key: value}")
        }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found " + "flow mapping [{][LEFT CURLY BRACKET][0x7b] at line 2 char 3")
    }


    @Nested inner class givenOneSpaceOnlyDocument : SingleDocument() {
        @BeforeEach fun setup() {
            input = " "
            expected = Document().node(Scalar().line(Line().text("").indent(1)))
        }
    }

    @Nested inner class givenTwoSpacesOnlyDocument : SingleDocument() {
        @BeforeEach fun setup() {
            input = "  "
            expected = Document().node(Scalar().line(Line().text("").indent(2)))
        }
    }

    @Nested inner class givenIndentedScalarDocument : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    foo"
            expected = Document().node(Scalar().line(Line().text("foo").indent(4)))
        }
    }

    @Nested inner class givenIndentedScalarsDocument : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    foo\n" + "  bar"
            expected = Document().node(Scalar()
                .line(Line().text("foo").indent(4))
                .line(Line().text("bar").indent(2)))
        }
    }

    @Nested inner class givenScalarWithSpacesBeforeAndAfter : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    dummy-string  "
            expected = Document().node(Scalar().line(Line().indent(4).text("dummy-string  ")))
        }
    }


    @Disabled @Nested inner class givenScalarWithComment : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string # dummy-comment"
            expected = Document().node(Scalar().line("dummy-string")
                .comment(Comment(indent = 1, text = "dummy-comment")))
        }
    }

    @Disabled @Nested inner class givenScalarWithIndentedComment : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string    # dummy-comment"
            expected = Document().node(Scalar().line("dummy-string")
                .comment(Comment(indent=4,text="dummy-comment")))
        }
    }

    @Disabled @Nested inner class givenScalarWithCommentWithLeadingSpaces : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string #     dummy-comment"
            expected = Document().node(Scalar().line("dummy-string")
                .comment(Comment(indent=1,text="    dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithCommentBefore : SingleDocument() {
        @BeforeEach fun setup() {
            input = "# dummy-comment\n" + "dummy-string"
            expected = Document()
                .prefixComment(Comment(text="dummy-comment"))
                .node(Scalar().line("dummy-string"))
        }
    }

    @Disabled @Nested inner class givenScalarWithCommentAfter : SingleDocument() {
        @BeforeEach fun setup() {
            input = "dummy-string\n" + "# dummy-comment"
            expected = Document()
                .node(Scalar().line("dummy-string")
                    .line("").comment(Comment(text="dummy-comment")))
        }
    }

    @Disabled @Nested inner class givenScalarWithCommentBeforeAndAfter : SingleDocument() {
        @BeforeEach fun setup() {
            input = "# before\n" +
                "dummy-string\n" +
                "# after"
            expected = Document()
                .prefixComment(Comment(text="before"))
                .node(Scalar().line("dummy-string")
                    .line("").comment(Comment(text="after")))
        }
    }

    @Disabled @Nested inner class givenScalarWithIndentedCommentBeforeAndAfter : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    # before\n" +
                "dummy-string\n" +
                "        # after"
            expected = Document()
                .prefixComment(Comment(indent=4,text="before"))
                .node(Scalar()
                    .line("dummy-string")
                    .line(Line().indent(8).text(""))
                    .comment(Comment(text="after")))
        }
    }

    @Disabled @Nested inner class givenIndentedScalarWithIndentedComment : SingleDocument() {
        @BeforeEach fun setup() {
            input = "    dummy-string  # dummy-comment"
            expected = Document().node(Scalar()
                .line(Line().indent(4).text("dummy-string"))
                .comment(Comment(indent=2,text="dummy-comment")))
        }
    }

    @Disabled @Nested inner class givenTwoLineScalarWithCommentInside : SingleDocument() {
        @BeforeEach fun setup() {
            input = "before # inside\n" + "after"
            expected = Document().node(Scalar()
                .line("before").comment(Comment(indent=1,text="inside"))
                .line("after"))
        }
    }

    @Disabled @Nested inner class givenTwoLineScalarWithCommentAfterSecond : SingleDocument() {
        @BeforeEach fun setup() {
            input = "one\n" + "two # comment"
            expected = Document().node(Scalar()
                .line("one")
                .line("two").comment(Comment(indent=1,text="comment")))
        }
    }

    @Disabled @Nested inner class givenTwoLineScalarWithTwoComments : SingleDocument() {
        @BeforeEach fun setup() {
            input = "one # first\n" + "two # second"
            expected = Document().node(Scalar()
                .line("one").comment(Comment(indent=1,text="first"))
                .line("two").comment(Comment(indent=1,text="second")))
        }
    }

    @Disabled @Nested inner class givenTwoLineScalarWithTwoIndentedComments : SingleDocument() {
        @BeforeEach fun setup() {
            input = "one      # first\n" + "long-two # second"
            expected = Document().node(Scalar()
                .line("one").comment(Comment(indent=6,text="first"))
                .line("long-two").comment(Comment(indent=1,text="second")))
        }
    }

    @Disabled @Nested inner class givenTwoLineScalarWithCommentBetween : SingleDocument() {
        @BeforeEach fun setup() {
            input = "one\n" +
                "# comment\n" +
                "two"
            expected = Document().node(Scalar()
                .line("one")
                .line("").comment(Comment(text="comment"))
                .line("two"))
        }
    }
}
