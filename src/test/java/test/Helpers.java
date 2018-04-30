package test;

import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Stream;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@UtilityClass final class Helpers {
    private static final String BOM = "⇔";
    private static final String EMPTY = "°";

    private static String resolveMagic(String yaml) {
        return yaml
                .replace(BOM, "\uFEFF")
                .replace(EMPTY, "");
    }

    private static String removeMagic(String yaml) {
        return yaml
                .replace(BOM, "")
                .replace(EMPTY, "");
    }


    static void parseAndCheck(String input, String expectedCanonical) {
        Stream stream = parse(input);
        assertThat(withoutTrailingNl(stream, Yaml::present)).describedAs("stream presentation")
                .isEqualTo(removeMagic(input));
        assertThat(Yaml.present(stream.canonicalize())).describedAs("canonicalized stream presentation")
                .isEqualTo(expectedCanonical);
    }

    static <T> String withoutTrailingNl(T object, Function<T, String> presenter) {
        if (object == null)
            return null;
        String string = presenter.apply(object);
        if (!string.isEmpty() && string.charAt(string.length() - 1) == '\n')
            string = string.substring(0, string.length() - 1);
        return string;
    }

    static Stream parse(String yaml) { return Yaml.parseAll(resolveMagic(yaml)); }
}
