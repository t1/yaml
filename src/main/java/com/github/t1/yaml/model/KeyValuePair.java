package com.github.t1.yaml.model;

import lombok.Data;

@Data
public class KeyValuePair {
    private Node key;
    private Node value;
}
