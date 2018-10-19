package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import lombok.NonNull;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class Spec {
    final List<Production> productions;
    private final Map<String, Production> index = new TreeMap<>();

    Spec(List<Production> productions) {
        this.productions = productions;
        index();
    }

    private void index() {
        for (Production production : productions) {
            Production previous = index.put(production.getKey(), production);
            assert previous == null : "overwrite " + previous.getKey();
        }
        for (Production production : productions) {
            production.expression.guide(new Expression.Visitor() {
                @Override void visit(ReferenceExpression referenceExpression) {
                    String ref = referenceExpression.ref;
                    production.references.put(ref, index.get(ref));
                }
            });
        }
    }

    Production get(String key) {
        Production result = index.get(key);
        if (result == null)
            throw new RuntimeException("no production '" + key + "' found");
        return result;
    }

    @Value static class Production {
        public int counter;
        public @NonNull String name;
        public String args;

        public @NonNull Expression expression;

        private final Map<String, Production> references = new HashMap<>();

        String getKey() { return name + ((args == null) ? "" : "(" + args + ")"); }

        @Override public String toString() {
            return "[" + counter + "] : " + name + ((args == null) ? "" : " [" + args + "]") + ":\n"
                + "  " + expression;
        }
    }
}
