package test

import com.github.t1.yaml.model.Comment
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.ScalarNode
import com.github.t1.yaml.model.ScalarNode.Line
import com.github.t1.yaml.parser.YamlParseException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class ScalarTest : AbstractYamlTest() {
    @Nested inner class givenScalar : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "dummy-string"
            AbstractYamlTest.expected = Document().node(ScalarNode().line("dummy-string"))
        }
    }

    @Test fun expectScalarStartNotContinueWithBlockSequence() {
        val thrown = catchThrowable { parse("scalar document\n- illegal sequence") }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found block sequence at [-][HYPHEN-MINUS][0x2d] at line 2 char 1")
    }

    @Test fun expectScalarStartNotContinueWithBlockMapping() {
        val thrown = catchThrowable { parse("scalar document\nkey: value") }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found block mapping at [k][LATIN SMALL LETTER K][0x6b] at line 2 char 1")
    }

    @Test fun expectScalarStartNotContinueWithFlowMapping() {
        val thrown = catchThrowable { parse("scalar document\n{key: value}") }

        assertThat(thrown)
            .isInstanceOf(YamlParseException::class.java)
            .hasMessage("Expected a scalar node to continue with scalar values but found flow mapping at [{][LEFT CURLY BRACKET][0x7b] at line 2 char 1")
    }


    @Nested inner class givenOneSpaceOnlyDocument : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = " "
            AbstractYamlTest.expected = Document().node(ScalarNode().line(Line().text("").indent(1)))
        }
    }

    @Nested inner class givenTwoSpacesOnlyDocument : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "  "
            AbstractYamlTest.expected = Document().node(ScalarNode().line(Line().text("").indent(2)))
        }
    }

    @Nested inner class givenIndentedScalarDocument : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "    foo"
            AbstractYamlTest.expected = Document().node(ScalarNode().line(Line().text("foo").indent(4)))
        }
    }

    @Nested inner class givenIndentedScalarsDocument : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "    foo\n" + "  bar"
            AbstractYamlTest.expected = Document().node(ScalarNode()
                .line(Line().text("foo").indent(4))
                .line(Line().text("bar").indent(2)))
        }
    }

    @Nested inner class givenScalarWithSpacesBeforeAndAfter : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "    dummy-string  "
            AbstractYamlTest.expected = Document().node(ScalarNode().line(Line().indent(4).text("dummy-string  ")))
        }
    }


    @Nested inner class givenScalarWithComment : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "dummy-string # dummy-comment"
            AbstractYamlTest.expected = Document().node(ScalarNode().line("dummy-string")
                .comment(Comment().indent(1).text("dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithIndentedComment : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "dummy-string    # dummy-comment"
            AbstractYamlTest.expected = Document().node(ScalarNode().line("dummy-string")
                .comment(Comment().indent(4).text("dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithCommentWithLeadingSpaces : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "dummy-string #     dummy-comment"
            AbstractYamlTest.expected = Document().node(ScalarNode().line("dummy-string")
                .comment(Comment().indent(1).text("    dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithCommentBefore : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "# dummy-comment\n" + "dummy-string"
            AbstractYamlTest.expected = Document()
                .prefixComment(Comment().text("dummy-comment"))
                .node(ScalarNode().line("dummy-string"))
        }
    }

    @Nested inner class givenScalarWithCommentAfter : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "dummy-string\n" + "# dummy-comment"
            AbstractYamlTest.expected = Document()
                .node(ScalarNode().line("dummy-string")
                    .line("").comment(Comment().text("dummy-comment")))
        }
    }

    @Nested inner class givenScalarWithCommentBeforeAndAfter : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "# before\n" +
                "dummy-string\n" +
                "# after"
            AbstractYamlTest.expected = Document()
                .prefixComment(Comment().text("before"))
                .node(ScalarNode().line("dummy-string")
                    .line("").comment(Comment().text("after")))
        }
    }

    @Nested inner class givenScalarWithIndentedCommentBeforeAndAfter : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "    # before\n" +
                "dummy-string\n" +
                "        # after"
            AbstractYamlTest.expected = Document()
                .prefixComment(Comment().indent(4).text("before"))
                .node(ScalarNode()
                    .line("dummy-string")
                    .line(Line().indent(8).text(""))
                    .comment(Comment().text("after")))
        }
    }

    @Nested inner class givenIndentedScalarWithIndentedComment : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "    dummy-string  # dummy-comment"
            AbstractYamlTest.expected = Document().node(ScalarNode()
                .line(Line().indent(4).text("dummy-string"))
                .comment(Comment().indent(2).text("dummy-comment")))
        }
    }

    @Nested inner class givenTwoLineScalarWithCommentInside : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "before # inside\n" + "after"
            AbstractYamlTest.expected = Document().node(ScalarNode()
                .line("before").comment(Comment().indent(1).text("inside"))
                .line("after"))
        }
    }

    @Nested inner class givenTwoLineScalarWithCommentAfterSecond : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "one\n" + "two # comment"
            AbstractYamlTest.expected = Document().node(ScalarNode()
                .line("one")
                .line("two").comment(Comment().indent(1).text("comment")))
        }
    }

    @Nested inner class givenTwoLineScalarWithTwoComments : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "one # first\n" + "two # second"
            AbstractYamlTest.expected = Document().node(ScalarNode()
                .line("one").comment(Comment().indent(1).text("first"))
                .line("two").comment(Comment().indent(1).text("second")))
        }
    }

    @Nested inner class givenTwoLineScalarWithTwoIndentedComments : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "one      # first\n" + "long-two # second"
            AbstractYamlTest.expected = Document().node(ScalarNode()
                .line("one").comment(Comment().indent(6).text("first"))
                .line("long-two").comment(Comment().indent(1).text("second")))
        }
    }

    @Nested inner class givenTwoLineScalarWithCommentBetween : AbstractYamlTest.SingleDocument() {
        @BeforeEach fun setup() {
            AbstractYamlTest.input = "one\n" +
                "# comment\n" +
                "two"
            AbstractYamlTest.expected = Document().node(ScalarNode()
                .line("one")
                .line("").comment(Comment().text("comment"))
                .line("two"))
        }
    }
}
