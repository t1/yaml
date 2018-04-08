package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

@Data
@EqualsAndHashCode(callSuper = true)
public class MappingNode extends CollectionNode {
    private List<KeyValuePair> content = new ArrayList<>();

    public MappingNode entry(String key, String value) { return entry(new ScalarNode().line(key), value); }

    public MappingNode entry(ScalarNode key, String value) { return entry(key, new ScalarNode().line(value)); }

    public MappingNode entry(String key, ScalarNode value) { return entry(new ScalarNode().line(key), value); }

    public MappingNode entry(Node key, Node value) { return entry(new KeyValuePair().key(key).value(value)); }

    public MappingNode entry(KeyValuePair entry) {
        content.add(entry);
        return this;
    }

    public String toString() { return content.stream().map(KeyValuePair::toString).collect(joining("\n")); }
}
