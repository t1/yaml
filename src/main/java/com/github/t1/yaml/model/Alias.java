package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class Alias extends Node {
    @Override public void guide(Visitor visitor) { visitor.visit(this); }
}
