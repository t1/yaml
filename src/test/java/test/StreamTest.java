package test;

import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.MappingNode;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.SequenceNode;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static test.Helpers.parse;

class StreamTest extends AbstractYamlTest {
    @Nested class givenEmptyDocument {
        @BeforeEach void setup() { input = ""; }

        @Nested class whenParseAll extends ParseAll implements ThenIsEmptyStream, ThenStreamToStringIsSameAsInput {}

        @Nested class whenParseFirst extends ParseFirst implements ThenThrowsExpectedAtLeastOneDocumentButFoundNone {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOneDocumentButFoundNone {}
    }

    @Nested class givenInvalidSecondDocument {
        @BeforeEach void setup() {
            input = "%YAML 1.2\n---\nvalid document\n...{";
            expected = new Document().directive(Directive.YAML_VERSION).node(new ScalarNode().line("valid document")).hasDocumentEndMarker(true);
        }

        @Nested class whenParseAll extends ParseAll implements ThenThrowsInvalid {}

        @Nested class whenParseFirst extends ParseFirst implements ThenIsExpectedDocument {}

        @Nested class whenParseSingle extends ParseSingle implements ThenThrowsExpectedExactlyOneDocumentButFoundMore {}
    }

    @Nested class givenOneLineCommentOnlyStream extends SingleDocument {
        @BeforeEach void setup() {
            input = "# test comment";
            expected = new Document().prefixComment(new Comment().text("test comment"));
        }
    }

    @Nested class givenTwoLineCommentOnlyStream extends SingleDocument {
        @BeforeEach void setup() {
            input = "# test comment\n# line two";
            expected = new Document().prefixComment(new Comment().text("test comment")).prefixComment(new Comment().text("line two"));
        }
    }

    @Nested class givenSpaceOnlyDocument extends SingleDocument {
        @BeforeEach void setup() {
            input = " ";
            expected = new Document().node(new ScalarNode().line(" "));
        }
    }
}
