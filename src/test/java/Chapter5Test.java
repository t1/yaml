import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.parser.YamlParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Chapter5Test extends SpecTest {
    @Test
    void spec_5_1_Byte_Order_Mark() {
        assertThat(canonical("" +
                "⇔# Comment only."
        )).isEqualTo("");
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
