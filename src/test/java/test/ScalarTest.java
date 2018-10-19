package test;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static helpers.Helpers.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ScalarTest extends AbstractYamlTest {
    @Nested class givenPlainScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string";
            expected = new Document().node(new Scalar().plain().line("dummy-string"));
        }
    }

    @Nested class givenSingleQuotedScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "'dummy-string'";
            expected = new Document().node(new Scalar().singleQuoted().line("dummy-string"));
        }
    }

    @Nested class givenDoubleQuotedScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "\"dummy-string\"";
            expected = new Document().node(new Scalar().doubleQuoted().line("dummy-string"));
        }
    }

    @Nested class givenTwoLinePlainScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy\n" +
                    "string";
            expected = new Document().node(new Scalar().plain().line("dummy").line("string"));
        }
    }

    @Nested class givenTwoLineSingleQuotedScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "'dummy\n" +
                    "string'";
            expected = new Document().node(new Scalar().singleQuoted().line("dummy").line("string"));
        }
    }

    @Nested class givenTwoLineDoubleQuotedScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "\"dummy\n" +
                    "string\"";
            expected = new Document().node(new Scalar().doubleQuoted().line("dummy\nstring"));
        }
    }

    @Nested class givenIndentedTwoLinePlainScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy\n" +
                    "  string";
            expected = new Document().node(new Scalar()
                    .plain()
                    .line("dummy")
                    .line(new Line().indent(2).text("string")));
        }
    }

    @Nested class givenIndentedTwoLineSingleQuotedScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "'dummy\n" +
                    "  string'";
            expected = new Document().node(new Scalar()
                    .singleQuoted()
                    .line("dummy")
                    .line(new Line().indent(2).text("string")));
        }
    }

    @Test void expectScalarStartNotContinueWithFlowSequence() {
        Throwable thrown = catchThrowable(() -> parse("" +
                "scalar document\n" +
                "[illegal sequence]"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found " +
                        "flow sequence [[][LEFT SQUARE BRACKET][0x5b] at line 2 char 1");
    }

    @Test void expectScalarStartNotContinueWithBlockSequence() {
        Throwable thrown = catchThrowable(() -> parse("" +
                "scalar document\n" +
                "- illegal sequence"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found " +
                        "block sequence [-][HYPHEN-MINUS][0x2d] at line 2 char 1");
    }

    @Test void expectScalarStartNotContinueWithBlockMapping() {
        Throwable thrown = catchThrowable(() -> parse("" +
                "scalar document\n" +
                "key: value"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found " +
                        "block mapping [k][LATIN SMALL LETTER K][0x6b] at line 2 char 1");
    }

    @Test void expectScalarStartNotContinueWithFlowMapping() {
        Throwable thrown = catchThrowable(() -> parse("" +
                "scalar document\n" +
                "{key: value}"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found " +
                        "flow mapping [{][LEFT CURLY BRACKET][0x7b] at line 2 char 1");
    }


    @Test void expectNestedScalarStartNotContinueWithFlowSequence() {
        Throwable thrown = catchThrowable(() -> parse("" +
                "- scalar document\n" +
                "  [illegal sequence]"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found " +
                        "flow sequence [[][LEFT SQUARE BRACKET][0x5b] at line 2 char 3");
    }

    @Test void expectNestedScalarStartNotContinueWithBlockSequence() {
        Throwable thrown = catchThrowable(() -> parse("" +
                "- scalar document\n" +
                "  - illegal sequence"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found " +
                        "block sequence [-][HYPHEN-MINUS][0x2d] at line 2 char 3");
    }

    @Test void expectNestedScalarStartNotContinueWithBlockMapping() {
        Throwable thrown = catchThrowable(() -> parse("" +
                "- scalar document\n" +
                "  key: value"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found " +
                        "block mapping [k][LATIN SMALL LETTER K][0x6b] at line 2 char 3");
    }

    @Test void expectNestedScalarStartNotContinueWithFlowMapping() {
        Throwable thrown = catchThrowable(() -> parse("" +
                "- scalar document\n" +
                "  {key: value}"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found " +
                        "flow mapping [{][LEFT CURLY BRACKET][0x7b] at line 2 char 3");
    }


    @Nested class givenOneSpaceOnlyDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = " ";
            expected = new Document().node(new Scalar().line(new Line().text("").indent(1)));
        }
    }

    @Nested class givenTwoSpacesOnlyDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = "  ";
            expected = new Document().node(new Scalar().line(new Line().text("").indent(2)));
        }
    }

    @Nested class givenIndentedScalarDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = "    foo";
            expected = new Document().node(new Scalar().line(new Line().text("foo").indent(4)));
        }
    }

    @Nested class givenIndentedScalarsDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = "    foo\n" +
                    "  bar";
            expected = new Document().node(new Scalar()
                    .line(new Line().text("foo").indent(4))
                    .line(new Line().text("bar").indent(2)));
        }
    }

    @Nested class givenScalarWithSpacesBeforeAndAfter extends SingleDocument {
        @BeforeEach void setup() {
            input = "    dummy-string  ";
            expected = new Document().node(new Scalar().line(new Line().indent(4).text("dummy-string  ")));
        }
    }


    @Nested class givenScalarWithComment extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string # dummy-comment";
            expected = new Document().node(new Scalar().line("dummy-string")
                    .comment(new Comment().indent(1).text("dummy-comment")));
        }
    }

    @Nested class givenScalarWithIndentedComment extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string    # dummy-comment";
            expected = new Document().node(new Scalar().line("dummy-string")
                    .comment(new Comment().indent(4).text("dummy-comment")));
        }
    }

    @Nested class givenScalarWithCommentWithLeadingSpaces extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string #     dummy-comment";
            expected = new Document().node(new Scalar().line("dummy-string")
                    .comment(new Comment().indent(1).text("    dummy-comment")));
        }
    }

    @Nested class givenScalarWithCommentBefore extends SingleDocument {
        @BeforeEach void setup() {
            input = "# dummy-comment\n" +
                    "dummy-string";
            expected = new Document()
                    .prefixComment(new Comment().text("dummy-comment"))
                    .node(new Scalar().line("dummy-string"));
        }
    }

    @Nested class givenScalarWithCommentAfter extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string\n" +
                    "# dummy-comment";
            expected = new Document()
                    .node(new Scalar().line("dummy-string")
                            .line("").comment(new Comment().text("dummy-comment")));
        }
    }

    @Nested class givenScalarWithCommentBeforeAndAfter extends SingleDocument {
        @BeforeEach void setup() {
            input = "# before\n" +
                    "dummy-string\n" +
                    "# after";
            expected = new Document()
                    .prefixComment(new Comment().text("before"))
                    .node(new Scalar().line("dummy-string")
                            .line("").comment(new Comment().text("after")));
        }
    }

    @Nested class givenScalarWithIndentedCommentBeforeAndAfter extends SingleDocument {
        @BeforeEach void setup() {
            input = "    # before\n" +
                    "dummy-string\n" +
                    "        # after";
            expected = new Document()
                    .prefixComment(new Comment().indent(4).text("before"))
                    .node(new Scalar()
                            .line("dummy-string")
                            .line(new Line().indent(8).text(""))
                            .comment(new Comment().text("after")));
        }
    }

    @Nested class givenIndentedScalarWithIndentedComment extends SingleDocument {
        @BeforeEach void setup() {
            input = "    dummy-string  # dummy-comment";
            expected = new Document().node(new Scalar()
                    .line(new Line().indent(4).text("dummy-string"))
                    .comment(new Comment().indent(2).text("dummy-comment")));
        }
    }

    @Nested class givenTwoLineScalarWithCommentInside extends SingleDocument {
        @BeforeEach void setup() {
            input = "before # inside\n" +
                    "after";
            expected = new Document().node(new Scalar()
                    .line("before").comment(new Comment().indent(1).text("inside"))
                    .line("after"));
        }
    }

    @Nested class givenTwoLineScalarWithCommentAfterSecond extends SingleDocument {
        @BeforeEach void setup() {
            input = "one\n" +
                    "two # comment";
            expected = new Document().node(new Scalar()
                    .line("one")
                    .line("two").comment(new Comment().indent(1).text("comment")));
        }
    }

    @Nested class givenTwoLineScalarWithTwoComments extends SingleDocument {
        @BeforeEach void setup() {
            input = "one # first\n" +
                    "two # second";
            expected = new Document().node(new Scalar()
                    .line("one").comment(new Comment().indent(1).text("first"))
                    .line("two").comment(new Comment().indent(1).text("second")));
        }
    }

    @Nested class givenTwoLineScalarWithTwoIndentedComments extends SingleDocument {
        @BeforeEach void setup() {
            input = "one      # first\n" +
                    "long-two # second";
            expected = new Document().node(new Scalar()
                    .line("one").comment(new Comment().indent(6).text("first"))
                    .line("long-two").comment(new Comment().indent(1).text("second")));
        }
    }

    @Nested class givenTwoLineScalarWithCommentBetween extends SingleDocument {
        @BeforeEach void setup() {
            input = "one\n" +
                    "# comment\n" +
                    "two";
            expected = new Document().node(new Scalar()
                    .line("one")
                    .line("").comment(new Comment().text("comment"))
                    .line("two"));
        }
    }
}
