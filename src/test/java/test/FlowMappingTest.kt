package test

import com.github.t1.yaml.model.Collection.Style.FLOW
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Mapping
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested

@Suppress("ClassName")
class FlowMappingTest : AbstractYamlTest() {
    @Nested inner class givenFlowMapping : SingleDocument() {
        @BeforeEach fun setup() {
            input = "{sky: blue,sea: green}"
            expected = Document(node = Mapping(style = FLOW)
                .entry("sky", "blue")
                .entry("sea", "green")
            )
        }
    }

    @Disabled @Nested inner class givenSingleLineFlowMapping : SingleDocument() {
        @BeforeEach fun setup() {
            input = "{ sky: blue, sea: green , }"
            expected = Document(node = Mapping(style = FLOW)
                .entry("sky", "blue")
                .entry("sea", "green")
            )
        }
    }
}
