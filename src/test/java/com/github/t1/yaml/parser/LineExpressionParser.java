package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import com.github.t1.yaml.parser.Expression.AlternativesExpression;
import com.github.t1.yaml.parser.Expression.CodePointExpression;
import com.github.t1.yaml.parser.Expression.LiteralExpression;
import com.github.t1.yaml.parser.Expression.MinusExpression;
import com.github.t1.yaml.parser.Expression.NullExpression;
import com.github.t1.yaml.parser.Expression.ParenthesesExpression;
import com.github.t1.yaml.parser.Expression.RangeExpression;
import com.github.t1.yaml.parser.Expression.RepeatedExpression;
import com.github.t1.yaml.parser.Expression.SwitchExpression;
import lombok.val;

import java.util.Stack;

import static com.github.t1.yaml.parser.Symbol.SPACE;

class LineExpressionParser {
    private final Scanner next;
    private Expression expression;
    private final Stack<Expression> stack;

    LineExpressionParser(String text, Expression expression, Stack<Expression> stack) {
        this.next = new Scanner(text);
        this.expression = expression;
        this.stack = stack;
    }

    Expression expression() {
        try {
            while (next.more()) {
                int spaces = next.count(SPACE);
                if (spaces > 0 && next.end()) {
                    break;
                } else if (next.accept("|")) {
                    next.skip(SPACE);
                    if (!(expression instanceof AlternativesExpression))
                        expression = new AlternativesExpression().add(expression);
                } else if (next.accept("(")) {
                    this.stack.push(this.expression);
                    this.expression = new NullExpression();
                } else if (next.accept(")")) {
                    this.expression = this.stack.pop().merge(new ParenthesesExpression(this.expression));
                } else if (next.accept("-")) {
                    next.skip(SPACE);
                    if (!(expression instanceof MinusExpression))
                        expression = expression.replaceLastWith(new MinusExpression(expression.last()));
                } else if (next.accept("×")) {
                    repeat(next.skip(" ").readWord());
                } else if (next.accept("?")) {
                    repeat("?");
                } else if (next.accept("*")) {
                    repeat("*");
                } else if (next.accept("+")) {
                    repeat("+");
                } else if (next.accept("[#x")) {
                    val from = hex();
                    next.expect("-#x");
                    val to = hex();
                    next.expect("]");
                    expression = expression.merge(new RangeExpression(from, to));
                } else if (next.accept("#x")) {
                    expression = expression.merge(hex());
                } else if (expression instanceof SwitchExpression) {
                    next.skip(SPACE);
                    SwitchExpression switchExpression = (SwitchExpression) expression;
                    String text = next.readUntil("⇒");
                    if (next.accept("⇒")) {
                        switchExpression.mergeCase(new LiteralExpression(text));
                    } else {
                        switchExpression.merge(new LiteralExpression(text));
                    }
                } else if (next.accept("⇒")) {
                    next.skip(SPACE);
                    SwitchExpression switchExpression = new SwitchExpression();
                    expression = switchExpression.mergeCase(expression);
                    if (next.more())
                        switchExpression.merge(new LiteralExpression(next.readLine()));
                } else if (next.more()) {
                    throw new IllegalStateException("unexpected text" + next);
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("at " + next, e);
        }
        return expression;
    }

    private Expression hex() {
        val hex = new StringBuilder();
        while (isHex(next.peek()))
            hex.append(next.read());
        return new CodePointExpression(CodePoint.decode("0x" + hex));
    }

    private boolean isHex(CodePoint c) {
        return Character.isDigit(c.value) || c.value >= 'A' && c.value <= 'F';
    }

    private void repeat(String repetitions) {
        val expression = new RepeatedExpression(this.expression.last(), repetitions);
        this.expression = this.expression.replaceLastWith(expression);
    }
}
