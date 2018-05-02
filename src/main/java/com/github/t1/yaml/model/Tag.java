package com.github.t1.yaml.model;

import lombok.Data;

public abstract @Data class Tag {
    private String name;
    private String kind;
}
