package com.github.t1.yaml.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

@Data
public class Document {
    private List<Directive> directives = new ArrayList<>();
    private boolean hasDirectivesEndMarker;

    private List<Comment> prefixComments = new ArrayList<>();

    private Node node;

    private boolean hasDocumentEndMarker;
    private Comment suffixComment;

    public Document directive(Directive directive) {
        directives.add(Objects.requireNonNull(directive));
        return this;
    }

    public Document prefixComment(Comment comment) {
        prefixComments.add(Objects.requireNonNull(comment));
        return this;
    }

    public Document node(Node node) {
        assert this.node == null;
        this.node = Objects.requireNonNull(node);
        return this;
    }

    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        if (!directives.isEmpty() || hasDirectivesEndMarker) {
            for (Directive directive : directives)
                out.append(directive).append('\n');
            out.append("---\n");
        }
        if (!prefixComments.isEmpty())
            out.append(prefixComments.stream().map(Comment::toString).collect(joining("\n", "", "\n")));
        if (node != null)
            out.append(node);
        if (hasDocumentEndMarker) {
            out.append("\n...");
            if (suffixComment != null)
                out.append(" ").append(suffixComment);
            out.append("\n");
        }
        return out.toString();
    }

    public Document canonicalize() {
        if (directives.stream().noneMatch(Directive.YAML_VERSION::matchName))
            directives.add(Directive.YAML_VERSION);
        prefixComments.clear();
        if (node != null)
            node.canonicalize();
        hasDocumentEndMarker(false);
        suffixComment = null;
        return this;
    }
}
