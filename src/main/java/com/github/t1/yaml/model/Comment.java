package com.github.t1.yaml.model;

import lombok.Data;

import static com.github.t1.yaml.dump.Tools.spaces;

@Data
public class Comment {
    private String text;
    private int indent = 0;

    @Override public String toString() { return spaces(indent) + "# " + text; }
}
