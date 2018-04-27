package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SequenceNode extends CollectionNode {
    private final List<Node> items = new ArrayList<>();

    public SequenceNode item(Node node) {
        items.add(node);
        return this;
    }

    @Override public void guide(Visitor visitor) {
        visitor.visit(this);
        for (Node item : items) {
            visitor.enterSequenceItem(this, item);
            item.guide(visitor);
            visitor.leaveSequenceItem(this, item);
        }
        visitor.leave(this);
    }

    @Override public void canonicalize() {
        for (Node node : items)
            node.canonicalize();
    }

    public Node lastItem() { return items.get(items.size() - 1); }
}
