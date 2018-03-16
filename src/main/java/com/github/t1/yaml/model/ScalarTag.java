package com.github.t1.yaml.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScalarTag extends Tag {
    private String format;
}
