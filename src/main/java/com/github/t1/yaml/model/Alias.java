package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Alias extends Node {
    @Override public void guide(Visitor visitor) { visitor.visit(this); }
}
