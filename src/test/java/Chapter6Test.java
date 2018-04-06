import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("spec") class Chapter6Test extends AbstractTest {
    @Disabled @Test void spec_6_1_Indentation_Spaces() {
        parseAndCheck("" +
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
                , "" +
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
}
