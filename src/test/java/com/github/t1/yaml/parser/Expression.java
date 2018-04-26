package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

abstract class Expression {
    abstract void guide(Visitor visitor);

    @SuppressWarnings("unused") static class Visitor {
        void visit(NullExpression nullExpression) {}

        void visit(AlternativesExpression alternativesExpression) {}

        void visit(SequenceExpression sequenceExpression) {}

        void visit(CodePointExpression codePointExpression) {}

        void visit(LiteralExpression literalExpression) {}

        void visit(RangeExpression rangeExpression) {}

        void visit(ReferenceExpression referenceExpression) {}

        void visit(MinusExpression minusExpression) {}

        void visit(RepeatedExpression repeatedExpression) {}

        void visit(SwitchExpression switchExpression) {}
    }

    protected Expression last() { throw new UnsupportedOperationException(); }

    protected void replaceLastWith(Expression expression) { throw new UnsupportedOperationException(); }

    static class NullExpression extends Expression {
        @Override public String toString() { return "<empty>"; }

        @Override void guide(Visitor visitor) { visitor.visit(this); }
    }

    static abstract class ContainerExpression extends Expression {
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

        @Override protected Expression last() { return lastOf(expressions).last(); }

        @Override protected void replaceLastWith(Expression expression) {
            if (lastOf(expressions) instanceof ContainerExpression)
                lastOf(expressions).replaceLastWith(expression);
            else
                setLastOf(expressions, expression);
        }

        ContainerExpression add(Expression expression) {
            expressions.add(expression);
            return this;
        }

        void merge(Expression expression) {
            if (expression instanceof ContainerExpression)
                expressions.addAll(((ContainerExpression) expression).expressions);
            else
                expressions.add(expression);
        }
    }

    static class AlternativesExpression extends ContainerExpression {
        static Expression of(Expression left, Expression right) {
            return ContainerExpression.of(left, right, AlternativesExpression.class);
        }

        @Override public String toString() {
            return expressions.stream().map(Expression::toString).collect(joining(" ||\n   ", "[", "]"));
        }

        @Override void guide(Visitor visitor) {
            visitor.visit(this);
            expressions.forEach(expression -> expression.guide(visitor));
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

        @Override void guide(Visitor visitor) {
            visitor.visit(this);
            expressions.forEach(expression -> expression.guide(visitor));
        }
    }

    @AllArgsConstructor
    static class CodePointExpression extends Expression {
        private CodePoint codePoint;

        @Override public String toString() { return "<" + codePoint.xinfo() + ">"; }

        @Override void guide(Visitor visitor) { visitor.visit(this); }
    }

    @AllArgsConstructor
    static class LiteralExpression extends Expression {
        private String literal;

        @Override public String toString() { return "<" + literal + ">"; }

        @Override void guide(Visitor visitor) { visitor.visit(this); }
    }

    @AllArgsConstructor
    static class RangeExpression extends Expression {
        private Expression left, right;

        @Override public String toString() { return "[" + left + "-" + right + "]"; }

        @Override void guide(Visitor visitor) { visitor.visit(this); }
    }

    @AllArgsConstructor
    static class ReferenceExpression extends Expression {
        String ref;

        @Override public String toString() { return "->" + ref; }

        @Override void guide(Visitor visitor) { visitor.visit(this); }
    }

    @RequiredArgsConstructor
    static class MinusExpression extends Expression {
        private final Expression minuend;
        private List<Expression> subtrahends = new ArrayList<>();

        static Expression of(Expression minuend, Expression subtrahend) {
            MinusExpression result = new MinusExpression(minuend);
            if (subtrahend instanceof MinusExpression) {
                MinusExpression minus = (MinusExpression) subtrahend;
                result.subtrahends.add((minus.minuend));
                result.subtrahends.addAll(minus.subtrahends);
            } else {
                result.subtrahends.add(subtrahend);
            }
            return result;
        }

        @Override public String toString() {
            return minuend + " - " +
                    subtrahends.stream().map(Expression::toString).collect(joining(" - "));
        }

        @Override void guide(Visitor visitor) { visitor.visit(this); }
    }

    @AllArgsConstructor
    static class RepeatedExpression extends Expression {
        Expression expression;
        String repetitions;

        @Override public String toString() { return "(" + expression + " × " + repetitions + ")"; }

        @Override void guide(Visitor visitor) { visitor.visit(this); }
    }

    static class SwitchExpression extends ContainerExpression {
        final List<Expression> cases = new ArrayList<>();

        boolean balanced() { return cases.size() == expressions.size(); }

        SwitchExpression addCase(Expression expression) {
            if (balanced())
                cases.add(expression);
            else
                setLastOf(cases, expression);
            return this;
        }

        void merge(Expression that) {
            if (balanced()) {
                replaceLastWith(that);
            } else {
                assert cases.size() == expressions.size() + 1;
                expressions.add(that);
            }
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

        @Override void guide(Visitor visitor) {
            visitor.visit(this);
            for (int i = 0; i < cases.size(); i++) {
                cases.get(i).guide(visitor);
                if (expressions.size() > i)
                    expressions.get(i).guide(visitor);
            }
        }
    }

    private static Expression lastOf(List<Expression> expressions) { return expressions.get(expressions.size() - 1); }

    private static void setLastOf(List<Expression> expressions, Expression expression) {
        expressions.set(expressions.size() - 1, expression);
    }
}
