package com.github.t1.yaml.model;

import lombok.Data;

@Data
public abstract class Node {
    private String anchor;
    private String style;
    private String spacing;
    private String lineWrapping;
}
