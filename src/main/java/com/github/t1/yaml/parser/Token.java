package com.github.t1.yaml.parser;

import com.github.t1.yaml.tools.CodePoint;

import java.util.List;
import java.util.function.Predicate;

public interface Token {
    List<Predicate<CodePoint>> predicates();
}
