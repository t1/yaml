package com.github.t1.yaml.model;

import lombok.Data;

@Data
public abstract class Node {
    @SuppressWarnings("unused")
    public interface Visitor {
        default void visit(Alias alias) {}


        default void visit(Sequence sequence) {}

        default void enterSequenceItem(Sequence sequence, Sequence.Item item) {}

        default void leaveSequenceItem(Sequence sequence, Sequence.Item item) {}

        default void leave(Sequence sequence) {}


        default void visit(Scalar scalar) {}

        default void enterScalarLine(Scalar node, Scalar.Line line) {}

        default void visit(Scalar.Line line) {}

        default void leaveScalarLine(Scalar node, Scalar.Line line) {}

        default void leave(Scalar scalar) {}


        default void visit(Mapping mapping) {}

        default void enterMappingEntry(Mapping mapping, Mapping.Entry entry) {}

        default void enterMappingKey(Mapping.Entry entry, Node key) {}

        default void leaveMappingKey(Mapping.Entry entry, Node key) {}

        default void enterMappingValue(Mapping.Entry entry, Node key) {}

        default void leaveMappingValue(Mapping.Entry entry, Node key) {}

        default void leaveMappingEntry(Mapping mapping, Mapping.Entry entry) {}

        default void leave(Mapping mapping) {}
    }


    private String anchor;
    private String spacing;
    private String lineWrapping;

    public void guide(Visitor visitor) {}

    public void canonicalize() {}
}
