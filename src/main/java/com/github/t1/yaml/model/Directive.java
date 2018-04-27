package com.github.t1.yaml.model;

import lombok.Value;

@Value
public class Directive {
    public static final Directive YAML_VERSION = new Directive("YAML", "1.2");

    private String name;
    private String parameters;

    boolean matchName(Directive that) { return name.equals(that.name); }
}
