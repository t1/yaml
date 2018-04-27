package com.github.t1.yaml.model;

import lombok.Data;

@Data
public abstract class Node {
    @SuppressWarnings("unused")
    public interface Visitor {
        default void visit(AliasNode alias) {}


        default void visit(SequenceNode sequence) {}

        default void enterSequenceItem(SequenceNode sequence, Node item) {}

        default void leaveSequenceItem(SequenceNode sequence, Node item) {}

        default void leave(SequenceNode sequence) {}


        default void visit(ScalarNode scalar) {}

        default void enterScalarLine(ScalarNode node, ScalarNode.Line line) {}

        default void visit(ScalarNode.Line line) {}

        default void leaveScalarLine(ScalarNode node, ScalarNode.Line line) {}

        default void leave(ScalarNode scalar) {}


        default void visit(MappingNode mapping) {}

        default void enterMappingEntry(MappingNode mapping, MappingNode.Entry entry) {}

        default void enterMappingKey(MappingNode.Entry entry, Node key) {}

        default void leaveMappingKey(MappingNode.Entry entry, Node key) {}

        default void enterMappingValue(MappingNode.Entry entry, Node key) {}

        default void leaveMappingValue(MappingNode.Entry entry, Node key) {}

        default void leaveMappingEntry(MappingNode mapping, MappingNode.Entry entry) {}

        default void leave(MappingNode mapping) {}
    }


    private String anchor;
    private String spacing;
    private String lineWrapping;

    public void guide(Visitor visitor) {}

    public void canonicalize() {}
}
