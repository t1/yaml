package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

@Data
@EqualsAndHashCode(callSuper = true)
public class SequenceNode extends CollectionNode {
    private final List<Node> content = new ArrayList<>();

    public SequenceNode entry(Node node) {
        content.add(node);
        return this;
    }

    public String toString() {
        return content.stream().map(Node::toString).collect(joining("\n- ", "- ", ""));
    }
}
