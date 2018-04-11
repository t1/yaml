package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MappingNode extends CollectionNode {
    private List<Entry> content = new ArrayList<>();

    public MappingNode entry(String key, String value) { return entry(new ScalarNode().line(key), value); }

    public MappingNode entry(ScalarNode key, String value) { return entry(key, new ScalarNode().line(value)); }

    public MappingNode entry(String key, ScalarNode value) { return entry(new ScalarNode().line(key), value); }

    public MappingNode entry(Node key, Node value) { return entry(new Entry().key(key).value(value)); }

    public MappingNode entry(Entry entry) {
        content.add(entry);
        return this;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        boolean first = true;
        for (Entry pair : content) {
            if (first)
                first = false;
            else
                out.append('\n');
            out.append(pair);
        }
        return out.toString();
    }

    public void canonicalize() {
        content.forEach(Entry::canonicalize);
    }

    @Data
    public static class Entry {
        private boolean hasMarkedKey = false;
        private Node key;
        private Node value;

        public String toString() { return (hasMarkedKey ? "? " : "") + key + ": " + value; }

        void canonicalize() {
            hasMarkedKey(true);
            key.canonicalize();
            value.canonicalize();
        }
    }
}
