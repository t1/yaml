package com.github.t1.yaml.model;

import lombok.Data;

import java.util.List;

@Data
public class Document {
    private List<Directive> directives;
    private Comment comment;
    private Node node;

    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        if (directives != null)
            for (Directive directive : directives)
                out.append(directive.toString());
        if (comment != null)
            out.append(comment.toString());
        if (node != null)
            out.append(node);
        return out.toString();
    }
}
