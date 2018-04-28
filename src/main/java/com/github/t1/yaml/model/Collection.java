package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.t1.yaml.model.Collection.Style.BLOCK;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Collection extends Node {
    public enum Style {
        FLOW, BLOCK
    }

    private Style style = BLOCK;
}
