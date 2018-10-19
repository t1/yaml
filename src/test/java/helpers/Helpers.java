package helpers;

import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Stream;
import lombok.experimental.UtilityClass;

import static org.assertj.core.api.Assertions.assertThat;

@UtilityClass public final class Helpers {
    private static final String BOM = "⇔";
    private static final String EMPTY = "°";
    private static final String BREAK = "↓\n"; // assert ↓ continues with \n
    private static final String TAB = "→";
    private static final String SPACE = "·";

    private static String resolveMagic(String yaml) {
        return yaml
                .replace(BOM, "\uFEFF")
                .replace(EMPTY, "")
                .replace(SPACE, " ")
                .replace(TAB, "\t")
                .replace(BREAK, "\n");
    }

    public static void parseAndCheck(String input, String expectedCanonical) {
        Stream stream = parse(input);
        assertStreamPresentation(input, stream);
        assertCanonicalStreamPresentation(expectedCanonical, stream);
    }

    public static void assertCanonicalStreamPresentation(String expectedCanonical, Stream stream) {
        Yaml.canonicalize(stream);
        assertThat(Yaml.present(stream)).describedAs("canonicalized stream presentation")
                .isEqualTo(expectedCanonical);
    }

    public static void assertStreamPresentation(String input, Stream stream) {
        assertThat(withoutTrailingNl(stream)).describedAs("stream presentation")
                .isEqualTo(resolveMagic(input.replaceAll(BOM, "")));
    }

    public static String withoutTrailingNl(Stream stream) {
        if (stream == null)
            return null;
        String string = Yaml.present(stream);
        if (!string.isEmpty() && string.charAt(string.length() - 1) == '\n')
            string = string.substring(0, string.length() - 1);
        return string;
    }

    public static Stream parse(String yaml) { return Yaml.parseAll(resolveMagic(yaml)); }
}
