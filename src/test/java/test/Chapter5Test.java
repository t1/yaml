package test;

import com.github.t1.yaml.parser.YamlParseException;
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
}
