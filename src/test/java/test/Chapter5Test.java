package test;

import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static test.Helpers.parseAndCheck;

@Tag("spec") class Chapter5Test {
    @Test
    void spec_5_1_Byte_Order_Mark() {
        parseAndCheck("" +
                        "⇔# Comment only."
                , "");
    }

    @Disabled @Test void spec_5_2_Invalid_Byte_Order_Mark() {
        assertThatThrownBy(() -> Yaml.parseAll("" +
                "- Invalid use of BOM\n" +
                "⇔\n" +
                "- Inside a document."))
                .isInstanceOf(YamlParseException.class)
                .hasMessage("A BOM must not appear inside a document.");
    }
}
