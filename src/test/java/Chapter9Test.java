import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Chapter9Test extends SpecTest {
    @Test void spec_9_1_Document_Prefix() {
        assertThat(canonical("" +
                "⇔# Comment\n" +
                "# lines\n" +
                "Document"
        )).isEqualTo("" +
                "%YAML 1.2\n" +
                "---\n" +
                "!!str \"Document\"");
    }
}
