package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public @Data class Sequence extends Collection {
    public static @Data class Item {
        boolean nl;
        Node node;
    }

    @Override public Sequence style(Style style) { super.style(style); return this; }

    private final List<Item> items = new ArrayList<>();

    public Sequence item(Node node) { return item(new Item().node(node)); }

    public Sequence item(Item item) {
        items.add(item);
        return this;
    }

    @Override public void guide(Visitor visitor) {
        visitor.visit(this);
        for (Item item : items) {
            visitor.enterSequenceItem(this, item);
            item.node.guide(visitor);
            visitor.leaveSequenceItem(this, item);
        }
        visitor.leave(this);
    }

    @Override public void canonicalize() {
        for (Item item : items)
            item.node.canonicalize();
    }

    public Item firstItem() { return items.get(0); }

    public Item lastItem() { return items.get(items.size() - 1); }
}
