package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class ScalarTag extends Tag {
    private String format;
}
