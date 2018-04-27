package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MappingNode extends CollectionNode {
    private List<Entry> entries = new ArrayList<>();

    public MappingNode entry(String key, String value) { return entry(new ScalarNode().line(key), value); }

    public MappingNode entry(ScalarNode key, String value) { return entry(key, new ScalarNode().line(value)); }

    public MappingNode entry(String key, ScalarNode value) { return entry(new ScalarNode().line(key), value); }

    public MappingNode entry(Node key, Node value) { return entry(new Entry().key(key).value(value)); }

    public MappingNode entry(Entry entry) {
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

    public void canonicalize() {
        entries.forEach(Entry::canonicalize);
    }

    public Entry lastEntry() { return entries.get(entries.size() - 1); }

    @Data
    public static class Entry {
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

        void canonicalize() {
            hasMarkedKey(true);
            // hasNlAfterKey(true);
            key.canonicalize();
            value.canonicalize();
        }
    }
}
