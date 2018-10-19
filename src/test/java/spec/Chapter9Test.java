package spec;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static helpers.Helpers.parseAndCheck;

@Tag("spec") class Chapter9Test {
    @Test void spec_9_1_Document_Prefix() {
        parseAndCheck("" +
                        "⇔# Comment\n" +
                        "# lines\n" +
                        "Document"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \"Document\"\n");
    }

    @Disabled
    @Test void spec_9_2_Document_Markers() {
        parseAndCheck("" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "Document\n" +
                        "... # Suffix"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \"Document\"\n");
    }

    @Test void spec_9_3_Bare_Documents() {
        parseAndCheck("" +
                        "Bare\n" +
                        "document\n" +
                        "...\n" +
                        "# No document\n" +
                        "...\n" +
                        "\n" + // TODO '|'
                        "%!PS-Adobe-2.0" // TODO comment: # Not the first line
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \"Bare document\"\n" +
                        "...\n" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \" %!PS-Adobe-2.0\"\n" // TODO space from pipe; where does the newline in the spec come from?
        );
    }

    @Disabled
    @Test void spec_9_4_Explicit_Documents() {
        parseAndCheck("" +
                        "---\n" +
                        // "{ matches\n" +
                        // "% : 20 }\n" + // TODO map!!
                        "dummy-document\n" +
                        "...\n" +
                        "---\n" +
                        "# Empty\n" +
                        "..."
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        // "!!map {\n" +
                        // "  !!str \"matches %\": !!int \"20\"\n" +
                        // "}\n" +
                        "!!str \"dummy-document\"\n" +
                        "...\n" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!null \"\"\n");
    }

    @Disabled
    @Test void spec_9_5_Directives_Documents() {
        parseAndCheck("" +
                        "%YAML 1.2\n" +
                        "---\n" + // TODO '|'
                        "%!PS-Adobe-2.0\n" +
                        "...\n" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "# Empty\n" +
                        "..."
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \"%!PS-Adobe-2.0\"\n" + // TODO where does the newline in the spec come from?
                        "...\n" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!null \"\"\n");
    }

    @Disabled
    @Test void spec_9_6_Stream() {
        parseAndCheck("" +
                        "Document\n" +
                        "---\n" +
                        "# Empty\n" +
                        "...\n" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "matches" // TODO ' %: 20'
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \"Document\"\n" +
                        "...\n" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!null \"\"\n" +
                        "...\n" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        // "!!map {\n" + // TODO !!map
                        "!!str \"matches" /*%: !!int 20*/ + "\"\n" +
                        // "}\n" +
                        ""
        );
    }
}
