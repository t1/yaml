package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScalarNode extends Node {
    private final List<String> lines = new ArrayList<>();

    public ScalarNode line(String line) {
        lines.add(line);
        return this;
    }

    @Override public String toString() { return String.join("\n", lines); }

    @Override public void canonicalize() {
        String singleLine = (lines.isEmpty()) ? "!!null \"\"" : "!!str \"" + String.join(" ", lines) + "\"";
        lines.clear();
        lines.add(singleLine);
    }
}
