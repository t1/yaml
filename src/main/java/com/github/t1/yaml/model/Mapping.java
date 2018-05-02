package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public @Data class Mapping extends Collection {
    private List<Entry> entries = new ArrayList<>();

    public Mapping entry(String key, String value) { return entry(new Scalar().line(key), value); }

    public Mapping entry(Scalar key, String value) { return entry(key, new Scalar().line(value)); }

    public Mapping entry(String key, Scalar value) { return entry(new Scalar().line(key), value); }

    public Mapping entry(String key, Node value) { return entry(new Scalar().line(key), value); }

    public Mapping entry(Node key, Node value) { return entry(new Entry().key(key).value(value)); }

    public Mapping entry(Entry entry) {
        entries.add(entry);
        return this;
    }

    @Override public void guide(Visitor visitor) {
        visitor.visit(this);
        for (Entry entry : entries) {
            visitor.enterMappingEntry(this, entry);
            entry.guide(visitor);
            visitor.leaveMappingEntry(this, entry);
        }
        visitor.leave(this);
    }

    public Entry lastEntry() { return entries.get(entries.size() - 1); }

    public static @Data class Entry {
        private boolean hasMarkedKey = false;
        private boolean hasNlAfterKey = false;
        private Node key;
        private Node value;

        void guide(Visitor visitor) {
            visitor.enterMappingKey(this, key);
            key.guide(visitor);
            visitor.leaveMappingKey(this, key);
            visitor.enterMappingValue(this, value);
            value.guide(visitor);
            visitor.leaveMappingValue(this, value);
        }
    }
}
