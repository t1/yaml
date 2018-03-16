package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MappingNode extends Node {
    private List<KeyValuePair> content;
}
