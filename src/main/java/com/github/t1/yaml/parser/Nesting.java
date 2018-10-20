package com.github.t1.yaml.parser;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static com.github.t1.yaml.tools.ToolsKt.spaces;

@RequiredArgsConstructor class Nesting {
    private final YamlScanner next;
    private int level;
    @Setter private boolean skipNext;

    @Override public String toString() { return "Nesting:" + level + (skipNext ? " skip next" : ""); }

    void up() { level++; }

    void down() { level--; }

    void expect() {
        next.expect(indent());
        skipNext = false;
    }

    boolean accept() {
        boolean accepted = next.accept(indent());
        if (accepted)
            skipNext = false;
        return accepted;
    }

    private String indent() { return skipNext ? "" : spaces(level * 2); }
}
