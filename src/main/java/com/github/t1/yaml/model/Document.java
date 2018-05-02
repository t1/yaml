package com.github.t1.yaml.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public @Data class Document {
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

    public boolean isEmpty() { return node == null && !hasDirectivesEndMarker; }

    public boolean hasDirectives() { return !directives.isEmpty(); }


    public void guide(Visitor visitor) {
        visitor.visit(this);

        if (!directives.isEmpty() || hasDirectivesEndMarker)
            guideToDirectives(visitor);

        for (Comment prefixComment : prefixComments)
            visitor.visitPrefixComment(prefixComment);

        if (node != null)
            guideToBody(visitor);

        if (hasDocumentEndMarker)
            guideToDocumentEnd(visitor);

        visitor.leave(this);
    }

    private void guideToDirectives(Visitor visitor) {
        visitor.enterDirectives(this);
        for (Directive directive : directives)
            visitor.visit(directive);
        visitor.leaveDirectives(this);
    }

    private void guideToBody(Visitor visitor) {
        visitor.enterBody(node);
        node.guide(visitor);
        visitor.leaveBody(node);
    }

    private void guideToDocumentEnd(Visitor visitor) {
        visitor.enterDocumentEnd();
        if (suffixComment != null)
            visitor.visitSuffixCommend(suffixComment);
        visitor.leaveDocumentEnd();
    }
}
