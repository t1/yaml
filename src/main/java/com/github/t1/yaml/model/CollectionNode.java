package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class CollectionNode extends Node {
    public enum Style {
        FLOW, BLOCK
    }

    private Style style;
}
