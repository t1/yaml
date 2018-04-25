package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

class Expression {
    Expression merge(Expression that) { return new SequenceExpression().add(this).add(that); }

    Expression last() { return this; }

    Expression replaceLastWith(Expression expression) { return expression; }

    /** just a starting point to merge expressions into */
    static class NullExpression extends Expression {
        @Override Expression merge(Expression that) { return that; }

        @Override public String toString() { return "<empty>"; }
    }

    static class ContainerExpression extends Expression {
        final List<Expression> expressions = new ArrayList<>();

        @SneakyThrows(ReflectiveOperationException.class)
        static Expression of(Expression left, Expression right, Class<? extends ContainerExpression> type) {
            ContainerExpression result = type.isInstance(left)
                    ? (ContainerExpression) left
                    : type.newInstance().add(left);
            if (type.isInstance(right))
                result.expressions.addAll(((ContainerExpression) right).expressions);
            else
                result.add(right);
            return result;
        }

        @Override Expression last() { return lastOf(expressions).last(); }

        @Override Expression replaceLastWith(Expression expression) {
            if (lastOf(expressions) instanceof ContainerExpression)
                lastOf(expressions).replaceLastWith(expression);
            else
                setLastOf(expressions, expression);
            return this;
        }

        ContainerExpression add(Expression expression) {
            expressions.add(expression);
            return this;
        }

        Expression merge(Expression expression) {
            if (expression instanceof ContainerExpression)
                expressions.addAll(((ContainerExpression) expression).expressions);
            else
                expressions.add(expression);
            return this;
        }
    }

    static class AlternativesExpression extends ContainerExpression {
        static Expression of(Expression left, Expression right) {
            return ContainerExpression.of(left, right, AlternativesExpression.class);
        }

        @Override public String toString() {
            return expressions.stream().map(Expression::toString).collect(joining(" ||\n   ", "[", "]"));
        }
    }

    static class SequenceExpression extends ContainerExpression {
        static Expression of(Expression left, Expression right) {
            return ContainerExpression.of(left, right, SequenceExpression.class);
        }

        @Override public String toString() {
            return expressions.isEmpty() ? "<empty sequence>" :
                    expressions.stream().map(Expression::toString).collect(joining(" + "));
        }
    }

    @AllArgsConstructor
    static class CodePointExpression extends Expression {
        private CodePoint codePoint;

        @Override public String toString() { return "<" + codePoint.xinfo() + ">"; }
    }

    @AllArgsConstructor
    static class LiteralExpression extends Expression {
        private String literal;

        @Override public String toString() { return "<" + literal + ">"; }
    }

    @AllArgsConstructor
    static class RangeExpression extends Expression {
        private Expression left, right;

        @Override public String toString() { return "[" + left + "-" + right + "]"; }
    }

    @AllArgsConstructor
    static class ReferenceExpression extends Expression {
        private String ref;

        @Override public String toString() { return "->" + ref; }
    }

    @RequiredArgsConstructor
    static class MinusExpression extends Expression {
        private final Expression minuend;
        private List<Expression> subtrahends = new ArrayList<>();

        static Expression of(Expression minuend, Expression subtrahend) {
            MinusExpression result = (minuend instanceof MinusExpression) ? (MinusExpression) minuend : new MinusExpression(minuend);
            if (subtrahend instanceof MinusExpression) {
                MinusExpression minus = (MinusExpression) subtrahend;
                result.merge(minus.minuend);
                minus.subtrahends.forEach(result::merge);
            } else {
                result.merge(subtrahend);
            }
            return result;
        }

        @Override Expression merge(Expression that) {
            subtrahends.add(that);
            return this;
        }

        @Override public String toString() {
            return minuend + " - " +
                    subtrahends.stream().map(Expression::toString).collect(joining(" - "));
        }
    }

    @AllArgsConstructor
    static class RepeatedExpression extends Expression {
        Expression expression;
        String repetitions;

        @Override public String toString() { return "(" + expression + " × " + repetitions + ")"; }
    }

    static class SwitchExpression extends ContainerExpression {
        final List<Expression> cases = new ArrayList<>();

        boolean balanced() { return cases.size() == expressions.size(); }

        SwitchExpression mergeCase(Expression expression) {
            if (balanced())
                cases.add(expression);
            else
                setLastOf(cases, lastOf(cases).merge(expression));
            return this;
        }

        @Override Expression merge(Expression that) {
            if (balanced()) {
                replaceLastWith(last().merge(that));
            } else {
                assert cases.size() == expressions.size() + 1;
                expressions.add(that);
            }
            return this;
        }

        @Override public String toString() {
            val out = new StringBuilder();
            for (int i = 0; i < cases.size(); i++) {
                if (i > 0)
                    out.append("\n  ");
                out.append(cases.get(i)).append(" ⇒ ")
                        .append(i < expressions.size() ? expressions.get(i) : "?");
            }
            return out.toString();
        }
    }

    private static Expression lastOf(List<Expression> expressions) { return expressions.get(expressions.size() - 1); }

    private static void setLastOf(List<Expression> expressions, Expression expression) {
        expressions.set(expressions.size() - 1, expression);
    }
}
