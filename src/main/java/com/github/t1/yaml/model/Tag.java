package com.github.t1.yaml.model;

import lombok.Data;

@Data
public abstract class Tag {
    private String name;
    private String kind;
}
