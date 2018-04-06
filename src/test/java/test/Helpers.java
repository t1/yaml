package test;

import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Stream;
import lombok.experimental.UtilityClass;

import static org.assertj.core.api.Assertions.assertThat;

@UtilityClass final class Helpers {
    private static final String BOM = "⇔";

    static String toStringWithoutTrailingNl(Object object) {
        String string = object.toString();
        if (!string.isEmpty() && string.charAt(string.length() - 1) == '\n')
            string = string.substring(0, string.length() - 1);
        return string;
    }

    static void parseAndCheck(String input, String expectedCanonical) {
        Stream stream = parse(input);
        assertThat(toStringWithoutTrailingNl(stream)).isEqualTo(input.replace(BOM, ""));
        assertThat(stream.canonicalize().toString()).isEqualTo(expectedCanonical);
    }

    static Stream parse(String yaml) {
        yaml = yaml.replace(BOM, "\uFEFF");
        return Yaml.parseAll(yaml);
    }
}