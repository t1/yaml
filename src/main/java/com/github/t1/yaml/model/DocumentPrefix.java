package com.github.t1.yaml.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Data
public class DocumentPrefix {
    // TODO byte-order-mark
    private List<Comment> comments = new ArrayList<>();

    public DocumentPrefix comment(Comment comment) {
        this.comments.add(comment);
        return this;
    }

    public String toString() { return comments.stream().map(Comment::toString).collect(joining("\n")); }
}
