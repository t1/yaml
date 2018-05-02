package com.github.t1.yaml.model;

import lombok.Data;

public abstract @Data class Node {
    private String anchor;
    private String spacing;
    private String lineWrapping;

    public void guide(Visitor visitor) {}

    public void canonicalize() {}
}
