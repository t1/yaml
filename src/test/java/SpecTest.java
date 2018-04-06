import com.github.t1.yaml.Yaml;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("spec") class SpecTest {
    private static String canonical(String yaml) {
        yaml = yaml.replace('⇔', '\uFEFF');
        return Yaml.parseAll(yaml).canonicalize().toString();
    }

    @Test
    void spec_5_1_Byte_Order_Mark() {
        assertThat(canonical("" +
                "⇔# Comment only."
        )).isEqualTo("");
    }

    @Disabled @Test void spec_6_1_Indentation_Spaces() {
        assertThat(canonical("" +
                "··# Leading comment line spaces are\n" +
                "···# neither content nor indentation.\n" +
                "····\n" +
                "Not indented:\n" +
                "·By one space: |\n" +
                "····By four\n" +
                "······spaces\n" +
                "·Flow style: [    # Leading spaces\n" +
                "···By two,        # in flow style\n" +
                "··Also by two,    # are neither\n" +
                "··→Still by two   # content nor\n" +
                "····]             # indentation."
        )).isEqualTo("" +
                "%YAML 1.2\n" +
                "---\n" +
                "!!map {\n" +
                "  ? !!str \"Not indented\"\n" +
                "  : !!map {\n" +
                "      ? !!str \"By one space\"\n" +
                "      : !!str \"By four\\n  spaces\\n\",\n" +
                "      ? !!str \"Flow style\"\n" +
                "      : !!seq [\n" +
                "          !!str \"By two\",\n" +
                "          !!str \"Also by two\",\n" +
                "          !!str \"Still by two\",\n" +
                "        ]\n" +
                "    }\n" +
                "}");
    }

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
