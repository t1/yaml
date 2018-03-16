package com.github.t1.yaml.model;

import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
public class Document {
    private List<Directive> directives;
    private Comment comment;
    private Node content;
}
