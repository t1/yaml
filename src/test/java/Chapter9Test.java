import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("spec") class Chapter9Test extends AbstractTest {
    @Test void spec_9_1_Document_Prefix() {
        parseAndCheck("" +
                        "⇔# Comment\n" +
                        "# lines\n" +
                        "Document"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \"Document\"");
    }

    @Test void spec_9_2_Document_Markers() {
        parseAndCheck("" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "Document\n" +
                        "... # Suffix"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \"Document\"");
    }
}
