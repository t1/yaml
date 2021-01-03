package test;

import com.github.t1.yaml.Yaml;
import com.github.t1.yaml.model.Document;
import org.junit.jupiter.api.Test;

import static com.github.t1.yaml.model.Document.document;
import static com.github.t1.yaml.model.Scalar.scalar;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the usage of the Java APIs, so it mainly tests the compiler ;-)
 */
class JavaApiTest {
    @Test
    void oneScalar() {
        Document parsed = Yaml.parseSingle("dummy-string");
        Document built = document(scalar().plain().line("dummy-string"));

        assertThat(parsed).isEqualTo(built);
        assertThat(Yaml.present(parsed)).isEqualTo("dummy-string\n");
    }
}
