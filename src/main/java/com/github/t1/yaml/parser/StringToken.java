package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

class StringToken implements Token {
    @Getter @Accessors(fluent = true) List<Predicate<CodePoint>> predicates;

    StringToken(String string) {
        this.predicates = CodePoint
                .stream(string)
                .map(codePoint -> new Predicate<CodePoint>() {
                    @Override public boolean test(CodePoint c) { return c.equals(codePoint); }

                    @Override public String toString() { return codePoint.xinfo(); }
                })
                .collect(toList());
    }

    @Override public String toString() { return "StringToken" + predicates; }
}
