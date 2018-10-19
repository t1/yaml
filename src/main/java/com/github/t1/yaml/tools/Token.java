package com.github.t1.yaml.tools;

import java.util.List;
import java.util.function.Predicate;

public interface Token {
    List<Predicate<CodePoint>> predicates();
}
