package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.github.t1.yaml.model.ScalarNode.Style.DOUBLE_QUOTED;
import static com.github.t1.yaml.model.ScalarNode.Style.PLAIN;
import static com.github.t1.yaml.model.ScalarNode.Style.SINGLE_QUOTED;
import static java.util.stream.Collectors.joining;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScalarNode extends Node {
    @RequiredArgsConstructor
    public enum Style {
        PLAIN(""), SINGLE_QUOTED("\'"), DOUBLE_QUOTED("\"");

        private final String quote;
    }

    private Style style = PLAIN;
    private String tag;

    private final List<String> lines = new ArrayList<>();

    public ScalarNode line(String line) {
        lines.add(line);
        return this;
    }

    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        if (tag != null)
            out.append(tag).append(' ');
        out.append(lines.stream().collect(joining("\n", style.quote, style.quote)));
        return out.toString();
    }

    @Override public void canonicalize() {
        doubleQuoted();
        if (!lines.isEmpty())
            replaceWith(String.join(" ", lines));
        this.tag((lines.isEmpty()) ? "!!null" : "!!str");
    }

    private void replaceWith(String singleLine) {
        lines.clear();
        lines.add(singleLine);
    }

    public ScalarNode plain() { return style(PLAIN); }

    public ScalarNode singleQuoted() { return style(SINGLE_QUOTED); }

    public ScalarNode doubleQuoted() { return style(DOUBLE_QUOTED); }
}
