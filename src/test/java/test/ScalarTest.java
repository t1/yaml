package test;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static test.Helpers.parse;

class ScalarTest extends AbstractYamlTest {
    @Nested class givenScalar extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string";
            expected = new Document().node(new ScalarNode().line("dummy-string"));
        }
    }

    @Test void expectScalarStartNotContinueWithBlockSequence() {
        Throwable thrown = catchThrowable(() -> parse("scalar document\n- illegal sequence"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found block sequence at [-][HYPHEN-MINUS][0x2d] at line 2 char 1");
    }

    @Test void expectScalarStartNotContinueWithBlockMapping() {
        Throwable thrown = catchThrowable(() -> parse("scalar document\nkey: value"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found block mapping at [k][LATIN SMALL LETTER K][0x6b] at line 2 char 1");
    }

    @Test void expectScalarStartNotContinueWithFlowMapping() {
        Throwable thrown = catchThrowable(() -> parse("scalar document\n{key: value}"));

        assertThat(thrown)
                .isInstanceOf(YamlParseException.class)
                .hasMessage("Expected a scalar node to continue with scalar values but found flow mapping at [{][LEFT CURLY BRACKET][0x7b] at line 2 char 1");
    }

    @Nested class givenScalarWithComment extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string # dummy-comment";
            expected = new Document().node(new ScalarNode().line("dummy-string")
                    .comment(new Comment().indent(1).text("dummy-comment")));
        }
    }

    @Nested class givenScalarWithIndentedComment extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string    # dummy-comment";
            expected = new Document().node(new ScalarNode().line("dummy-string")
                    .comment(new Comment().indent(4).text("dummy-comment")));
        }
    }

    @Nested class givenScalarWithCommentWithLeadingSpaces extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string #     dummy-comment";
            expected = new Document().node(new ScalarNode().line("dummy-string")
                    .comment(new Comment().indent(1).text("    dummy-comment")));
        }
    }

    @Nested class givenScalarWithCommentBefore extends SingleDocument {
        @BeforeEach void setup() {
            input = "# dummy-comment\n" +
                    "dummy-string";
            expected = new Document()
                    .prefixComment(new Comment().text("dummy-comment"))
                    .node(new ScalarNode().line("dummy-string"));
        }
    }

    @Nested class givenScalarWithCommentAfter extends SingleDocument {
        @BeforeEach void setup() {
            input = "dummy-string\n" +
                    "# dummy-comment";
            expected = new Document()
                    .node(new ScalarNode().line("dummy-string")
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
                    .node(new ScalarNode().line("dummy-string")
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
                    .node(new ScalarNode().line("dummy-string")
                            .line("").comment(new Comment().indent(8).text("after")));
        }
    }

    @Nested class givenTwoLineScalarWithCommentInside extends SingleDocument {
        @BeforeEach void setup() {
            input = "before # inside\n" +
                    "after";
            expected = new Document().node(new ScalarNode()
                    .line("before").comment(new Comment().indent(1).text("inside"))
                    .line("after"));
        }
    }

    @Nested class givenTwoLineScalarWithCommentAfterSecond extends SingleDocument {
        @BeforeEach void setup() {
            input = "one\n" +
                    "two # comment";
            expected = new Document().node(new ScalarNode()
                    .line("one")
                    .line("two").comment(new Comment().indent(1).text("comment")));
        }
    }

    @Nested class givenTwoLineScalarWithTwoComments extends SingleDocument {
        @BeforeEach void setup() {
            input = "one # first\n" +
                    "two # second";
            expected = new Document().node(new ScalarNode()
                    .line("one").comment(new Comment().indent(1).text("first"))
                    .line("two").comment(new Comment().indent(1).text("second")));
        }
    }

    @Nested class givenTwoLineScalarWithTwoIndentedComments extends SingleDocument {
        @BeforeEach void setup() {
            input = "one      # first\n" +
                    "long-two # second";
            expected = new Document().node(new ScalarNode()
                    .line("one").comment(new Comment().indent(6).text("first"))
                    .line("long-two").comment(new Comment().indent(1).text("second")));
        }
    }

    @Nested class givenTwoLineScalarWithCommentBetween extends SingleDocument {
        @BeforeEach void setup() {
            input = "one\n" +
                    "# comment\n" +
                    "two";
            expected = new Document().node(new ScalarNode()
                    .line("one")
                    .line("").comment(new Comment().text("comment"))
                    .line("two"));
        }
    }
}