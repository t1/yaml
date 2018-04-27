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
        hasDirectivesEndMarker = true;
        return this;
    }

    public Document prefixComment(Comment comment) {
        prefixComments.add(Objects.requireNonNull(comment));
        return this;
    }

    public Document node(Node node) {
        assert this.node == null : "Already has node";
        this.node = Objects.requireNonNull(node);
        return this;
    }

    @Override public String toString() { return new ToStringVisitor(this).toString(); }

    public Document canonicalize() {
        if (directives.stream().noneMatch(Directive.YAML_VERSION::matchName))
            directives.add(Directive.YAML_VERSION);
        prefixComments.clear();
        if (node == null)
            node = new ScalarNode();
        node.canonicalize();
        suffixComment = null;
        return this;
    }

    public boolean isEmpty() { return node == null && !hasDirectivesEndMarker; }

    public boolean hasDirectives() { return !directives.isEmpty(); }
}
