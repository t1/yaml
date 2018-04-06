import com.github.t1.yaml.Yaml;
import org.junit.jupiter.api.Tag;

abstract class AbstractTest {
    static String canonical(String yaml) {
        yaml = yaml.replace('⇔', '\uFEFF'); // BOM
        return Yaml.parseAll(yaml).canonicalize().toString();
    }
}
