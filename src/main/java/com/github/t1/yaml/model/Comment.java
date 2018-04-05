package com.github.t1.yaml.model;

import lombok.Data;

@Data
public class Comment {
    private String text;

    @Override public String toString() { return "# " + text; }
}
