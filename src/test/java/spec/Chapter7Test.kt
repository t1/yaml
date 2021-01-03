package spec

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import test.parseAndCheck

@Tag("spec") class Chapter7Test {
    @Disabled @Test fun spec_7_1_Alias_Nodes() {
        parseAndCheck("" +
            "First occurrence: &anchor Foo\n" +
            "Second occurrence: *anchor\n" +
            "Override anchor: &anchor Bar\n" +
            "Reuse anchor: *anchor", "" +
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
            "}")
    }

    @Disabled @Test fun spec_7_2_Empty_Content() {
        parseAndCheck("" +
            "{\n" +
            "  foo : !!str°,\n" +
            "  !!str° : bar,\n" +
            "}", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!map {\n" +
            "  ? !!str \"foo\" : !!str \"\",\n" +
            "  ? !!str \"\"    : !!str \"bar\",\n" +
            "}")
    }

    @Disabled @Test fun spec_7_3_Completely_Empty_Flow_Nodes() {
        parseAndCheck("" +
            "{\n" +
            "  ? foo :°,\n" +
            "  °: bar,\n" +
            "}", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!map {\n" +
            "  ? !!str \"foo\" : !!null \"\",\n" +
            "  ? !!null \"\"   : !!str \"bar\",\n" +
            "}")
    }

    @Disabled @Test fun spec_7_4_Double_Quoted_Implicit_Keys() {
        parseAndCheck("" +
            "\"implicit block key\" : [\n" +
            "  \"implicit flow key\" : value,\n" +
            " ]", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!map {\n" +
            "  ? !!str \"implicit block key\"\n" +
            "  : !!seq [\n" +
            "    !!map {\n" +
            "      ? !!str \"implicit flow key\"\n" +
            "      : !!str \"value\",\n" +
            "    }\n" +
            "  ]\n" +
            "}")
    }

    @Disabled @Test fun spec_7_5_Double_Quoted_Line_Breaks() {
        parseAndCheck("" +
            "\"folded·↓\n" +
            "to a space,→↓\n" +
            "·↓\n" +
            "to a line feed, or·→\\↓\n" +
            "·\\·→non-content\"", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!str \"folded to a space,\\n\\\n" +
            "      to a line feed, \\\n" +
            "      or \\t \\tnon-content\"\n")
    }

    @Disabled @Test fun spec_7_6_Double_Quoted_Lines() {
        parseAndCheck("" +
            "\"·1st non-empty↓\n" +
            "↓\n" +
            "·2nd non-empty·\n" +
            "→3rd non-empty·\"", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!str \" 1st non-empty\\n\\\n" +
            "      2nd non-empty \\\n" +
            "      3rd non-empty \"\n")
    }

    @Disabled @Test fun spec_7_7_Single_Quoted_Characters() {
        parseAndCheck("" + " 'here''s to \"quotes\"'", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!str \"here's to \\\"quotes\\\"\"")
    }

    @Disabled @Test fun spec_7_8_Single_Quoted_Implicit_Keys() {
        parseAndCheck("" +
            "'implicit block key' : [\n" +
            "  'implicit flow key' : value,\n" +
            " ]", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!map {\n" +
            "  ? !!str \"implicit block key\"\n" +
            "  : !!seq [\n" +
            "    !!map {\n" +
            "      ? !!str \"implicit flow key\"\n" +
            "      : !!str \"value\",\n" +
            "    }\n" +
            "  ]\n" +
            "}")
    }

    @Disabled @Test fun spec_7_9_Single_Quoted_Lines() {
        parseAndCheck("" +
            "'·1st non-empty↓\n" +
            "↓\n" +
            "·2nd non-empty·\n" +
            "→3rd non-empty·'", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!str \" 1st non-empty\\n\\\n" +
            "      2nd non-empty \\\n" +
            "      3rd non-empty \"")
    }

    @Disabled @Test fun spec_7_10_Plain_Characters() {
        parseAndCheck("" +
            "# Outside flow collection:\n" +
            "- ::vector\n" +
            "- \": - ()\"\n" +
            "- Up, up, and away!\n" +
            "- -123\n" +
            "- http://example.com/foo#bar\n" +
            "# Inside flow collection:\n" +
            "- [ ::vector,\n" +
            "  \": - ()\",\n" +
            "  \"Up, up and away!\",\n" +
            "  -123,\n" +
            "  http://example.com/foo#bar ]", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!seq [\n" +
            "  !!str \"::vector\",\n" +
            "  !!str \": - ()\",\n" +
            "  !!str \"Up, up, and away!\",\n" +
            "  !!int \"-123\",\n" +
            "  !!str \"http://example.com/foo#bar\",\n" +
            "  !!seq [\n" +
            "    !!str \"::vector\",\n" +
            "    !!str \": - ()\",\n" +
            "    !!str \"Up, up, and away!\",\n" +
            "    !!int \"-123\",\n" +
            "    !!str \"http://example.com/foo#bar\",\n" +
            "  ],\n" +
            "]")
    }
}
