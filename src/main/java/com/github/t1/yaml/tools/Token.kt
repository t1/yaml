package com.github.t1.yaml.tools

import java.util.function.Predicate

interface Token {
    val predicates: List<Predicate<CodePoint>>
}
