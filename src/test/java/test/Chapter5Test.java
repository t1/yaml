package test;

import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static test.Helpers.parse;
import static test.Helpers.parseAndCheck;

@Tag("spec") class Chapter5Test {
    @Test void spec_5_1_Byte_Order_Mark() {
        parseAndCheck("" +
                        "⇔# Comment only."
                , "");
    }

    @Test void spec_5_2_Invalid_Byte_Order_Mark() {
        assertThatThrownBy(() -> parse("" +
                "Invalid use of BOM\n" + // TODO !!seq
                "⇔\n" +
                "Inside a document."))
                .isInstanceOf(YamlParseException.class)
                .hasMessage("A BOM must not appear inside a document");
    }

    @Disabled @Test void spec_5_3_Block_Structure_Indicators() {
        parseAndCheck("" +
                        "sequence:\n" +
                        "- one\n" +
                        "- two\n" +
                        "mapping:\n" +
                        "  ? sky\n" +
                        "  : blue\n" +
                        "  sea : green"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!map {\n" +
                        "  ? !!str \"sequence\"\n" +
                        "  : !!seq [ !!str \"one\", !!str \"two\" ],\n" +
                        "  ? !!str \"mapping\"\n" +
                        "  : !!map {\n" +
                        "    ? !!str \"sky\" : !!str \"blue\",\n" +
                        "    ? !!str \"sea\" : !!str \"green\",\n" +
                        "  },\n" +
                        "}");
    }

    @Disabled @Test void spec_5_4_Flow_Collection_Indicators() {
        parseAndCheck("" +
                        "sequence: [ one, two, ]\n" +
                        "mapping: { sky: blue, sea: green }"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!map {\n" +
                        "  ? !!str \"sequence\"\n" +
                        "  : !!seq [ !!str \"one\", !!str \"two\" ],\n" +
                        "  ? !!str \"mapping\"\n" +
                        "  : !!map {\n" +
                        "    ? !!str \"sky\" : !!str \"blue\",\n" +
                        "    ? !!str \"sea\" : !!str \"green\",\n" +
                        "  },\n" +
                        "}");
    }

    @Test void spec_5_5_Comment_Indicator() {
        parseAndCheck("" +
                        "# Comment only."
                , ""); // This stream contains no documents, only comments.
    }

    @Disabled("anchor/alias")
    @Test void spec_5_6_Node_Property_Indicators() {
        parseAndCheck("" +
                        "anchored: !local &anchor value\n" +
                        "alias: *anchor"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!map {\n" +
                        "  ? !!str \"anchored\"\n" +
                        "  : !local &A1 \"value\",\n" +
                        "  ? !!str \"alias\"\n" +
                        "  : *A1,\n" +
                        "}");
    }

    @Disabled("Flow and literal scalars")
    @Test void spec_5_7_Block_Scalar_Indicators() {
        parseAndCheck("" +
                        "literal: |\n" +
                        "  some\n" +
                        "  text\n" +
                        "folded: >\n" +
                        "  some\n" +
                        "  text"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!map {\n" +
                        "  ? !!str \"literal\"\n" +
                        "  : !!str \"some\\ntext\\n\",\n" +
                        "  ? !!str \"folded\"\n" +
                        "  : !!str \"some text\\n\",\n" +
                        "}");
    }

    @Disabled @Test void spec_5_8_Quoted_Scalar_Indicators() {
        parseAndCheck("" +
                        "single: 'text'\n" +
                        "double: \"text\""
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!map {\n" +
                        "  ? !!str \"single\"\n" +
                        "  : !!str \"text\",\n" +
                        "  ? !!str \"double\"\n" +
                        "  : !!str \"text\",\n" +
                        "}");
    }

    @Disabled @Test void spec_5_9_Directive_Indicator() {
        parseAndCheck("" +
                        "%YAML 1.2\n" +
                        "--- text"
                , "");
    }

    @Disabled("ERROR: Reserved indicators can't start a plain scalar.")
    @Test void spec_5_10_Invalid_use_of_Reserved_Indicators() {
        parseAndCheck("" +
                        "commercial-at: @text\n" +
                        "grave-accent: `text"
                , "");
    }

    @Disabled @Test void sped_5_11_Line_Break_Characters() {
        parseAndCheck("" +
                        "|\n" +
                        "  Line break (no glyph)\n" +
                        "  Line break (glyphed)↓"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!str \"line break (no glyph)\\n\\\n" +
                        "      line break (glyphed)\\n\"");
    }

    @Disabled @Test void spec_5_12_Tabs_and_Spaces() {
        parseAndCheck("" +
                        "# Tabs and spaces\n" +
                        "quoted:·\"Quoted →\"\n" +
                        "block:→|\n" +
                        "··void main() {\n" +
                        "··→printf(\"Hello, world!\\n\");\n" +
                        "··}"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "!!map {\n" +
                        "  ? !!str \"quoted\"\n" +
                        "  : \"Quoted \\t\",\n" +
                        "  ? !!str \"block\"\n" +
                        "  : \"void main() {\\n\\\n" +
                        "    \\tprintf(\\\"Hello, world!\\\\n\\\");\\n\\\n" +
                        "    }\\n\",\n" +
                        "}");
    }

    @Disabled @Test void spec_5_13_Escaped_Characters() {
        parseAndCheck("" +
                        "\"Fun with \\\\\n" +
                        "\\\" \\a \\b \\e \\f \\↓\n" +
                        "\\n \\r \\t \\v \\0 \\↓\n" +
                        "\\  \\_ \\N \\L \\P \\↓\n" +
                        "\\x41 \\u0041 \\U00000041\""
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "\"Fun with \\x5C\n" +
                        "\\x22 \\x07 \\x08 \\x1B \\x0C\n" +
                        "\\x0A \\x0D \\x09 \\x0B \\x00\n" +
                        "\\x20 \\xA0 \\x85 \\u2028 \\u2029\n" +
                        "A A A\"");
    }

    @Disabled("ERROR:\n" +
            "- c is an invalid escaped character.\n" +
            "- q and - are invalid hex digits.")
    @Test void spec_5_14_Invalid_Escaped_Characters() {
        parseAndCheck("" +
                        "Bad escapes:\n" +
                        "  \"\\c\n" +
                        "  \\xq-\""
                , "");
    }
}
