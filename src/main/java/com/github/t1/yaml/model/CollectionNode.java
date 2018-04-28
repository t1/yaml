package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.t1.yaml.model.CollectionNode.Style.BLOCK;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class CollectionNode extends Node {
    public enum Style {
        FLOW, BLOCK
    }

    private Style style = BLOCK;
}
