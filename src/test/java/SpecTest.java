import com.github.t1.yaml.Yaml;
import org.junit.jupiter.api.Tag;

@Tag("spec") abstract class SpecTest {
    protected static String canonical(String yaml) {
        yaml = yaml.replace('⇔', '\uFEFF');
        return Yaml.parseAll(yaml).canonicalize().toString();
    }
}
