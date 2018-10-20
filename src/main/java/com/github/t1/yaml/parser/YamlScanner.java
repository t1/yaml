package com.github.t1.yaml.parser;

import com.github.t1.yaml.tools.Scanner;

import java.io.Reader;

import static com.github.t1.yaml.parser.Marker.BLOCK_MAPPING_VALUE;
import static com.github.t1.yaml.parser.Marker.BLOCK_SEQUENCE_START;
import static com.github.t1.yaml.parser.Marker.DIRECTIVES_END_MARKER;
import static com.github.t1.yaml.parser.Marker.DOCUMENT_END_MARKER;
import static com.github.t1.yaml.parser.Symbol.FLOW_MAPPING_START;
import static com.github.t1.yaml.parser.Symbol.FLOW_SEQUENCE_START;
import static com.github.t1.yaml.parser.Symbol.QUESTION_MARK;

class YamlScanner extends Scanner {
    YamlScanner(int lookAheadLimit, Reader reader) { super(lookAheadLimit, reader); }

    @Override public boolean more() {
        return super.more()
            && !is(DOCUMENT_END_MARKER)
            && !is(DIRECTIVES_END_MARKER); // of next document
    }

    boolean isFlowSequence() { return is(FLOW_SEQUENCE_START); }

    boolean isBlockSequence() { return is(BLOCK_SEQUENCE_START); }

    boolean isFlowMapping() { return is(FLOW_MAPPING_START); }

    boolean isBlockMapping() {
        if (is(QUESTION_MARK))
            return true;
        String token = peekUntil(BLOCK_MAPPING_VALUE);
        return token != null && token.length() >= 1 && !token.contains("\n");
    }
}
