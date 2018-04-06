package com.github.t1.yaml.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Document {
    private List<Directive> directives = new ArrayList<>();
    private Node node;

    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        if (!directives.isEmpty()) {
            for (Directive directive : directives)
                out.append(directive.toString()).append('\n');
            out.append("---\n");
        }
        if (node != null)
            out.append(node);
        return out.toString();
    }

    void canonicalize() {
        directives.add(Directive.YAML_VERSION);
        node.canonicalize();
    }
}
