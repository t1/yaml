package com.github.t1.yaml.model;

import lombok.Data;

public @Data class Comment {
    private String text;
    private int indent = 0;
}
