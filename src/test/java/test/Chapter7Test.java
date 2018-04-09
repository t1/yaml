package test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static test.Helpers.parseAndCheck;

@Tag("spec") class Chapter7Test {
    @Disabled @Test void spec_7_1_Alias_Nodes() {
        parseAndCheck("" +
                        "First occurrence: &anchor Foo\n" +
                        "Second occurrence: *anchor\n" +
                        "Override anchor: &anchor Bar\n" +
                        "Reuse anchor: *anchor"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!map {\n" +
                        "  ? !!str \"First occurrence\"\n" +
                        "  : &A !!str \"Foo\",\n" +
                        "  ? !!str \"Override anchor\"\n" +
                        "  : &B !!str \"Bar\",\n" +
                        "  ? !!str \"Second occurrence\"\n" +
                        "  : *A,\n" +
                        "  ? !!str \"Reuse anchor\"\n" +
                        "  : *B,\n" +
                        "}");
    }
}
