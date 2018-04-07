package test;

import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static test.Helpers.parse;
import static test.Helpers.parseAndCheck;

@Tag("spec") class Chapter5Test {
    @Test
    void spec_5_1_Byte_Order_Mark() {
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

    @Disabled @Test void sped_5_3_Block_Structure_Indicators() {
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
}
