package com.github.t1.yaml.parser;

import com.github.t1.yaml.model.Node;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class NodeParser {
    private final Scanner next;

    public Optional<Node> node() {
        return null;
    }

    private int c() { return next.peek().value; }

    /** [1] */
    private boolean c_printable() {
        int c = c();
        return c == 0x9 || c == 0xA || c == 0xD || (c >= 0x20 && c <= 0x7E)                /* 8 bit */
                || c == 0x85 || (c >= 0xA0 && c <= 0xD7FF) || (c >= 0xE000 && c <= 0xFFFD) /* 16 bit */
                || (c >= 0x10000 && c <= 0x10FFFF);                                        /* 32 bit */
    }

    /** [2] */
    private boolean nb_json() {
        int c = c();
        return c == 0x9 || (c >= 0x20 && c <= 0x10FFFF);
    }

    /** [3] */
    private boolean c_byte_order_mark() { return c() == 0xFEFF; }

    /** [4] */
    private boolean c_sequence_entry() { return c() == '-'; }
}
