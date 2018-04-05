package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScalarNode extends Node {
    private String text;

    @Override public String toString() { return text; }
}
