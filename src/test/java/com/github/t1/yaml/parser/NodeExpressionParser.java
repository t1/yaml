package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import com.github.t1.yaml.parser.Expression.AlternativesExpression;
import com.github.t1.yaml.parser.Expression.CodePointExpression;
import com.github.t1.yaml.parser.Expression.LiteralExpression;
import com.github.t1.yaml.parser.Expression.MinusExpression;
import com.github.t1.yaml.parser.Expression.NullExpression;
import com.github.t1.yaml.parser.Expression.RangeExpression;
import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import com.github.t1.yaml.parser.Expression.RepeatedExpression;
import com.github.t1.yaml.parser.Expression.SequenceExpression;
import com.github.t1.yaml.parser.Expression.SwitchExpression;
import lombok.experimental.var;
import lombok.val;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.List;
import java.util.function.Supplier;

/**
 * A recursive descend parser for the grammar html snippets in the yaml spec.
 * Error messages are generally failed assertions only, so you need the source to see what happened.
 * Some errors in the grammar description are not detected... the result may not be valid.
 */
class NodeExpressionParser {
    private final NodeScanner next;

    NodeExpressionParser(List<Node> nodes) { this.next = new NodeScanner(nodes); }

    Expression expression() {
        skipWhitespaceAndComments();

        Expression expression = body();

        skipWhitespaceAndComments();

        expression = postfix(expression);

        return expression;
    }

    private Expression body() {
        if (next.accept("("))
            return expression();
        if (next.accept("#x"))
            return hex();
        if (next.accept("[#x"))
            return range();
        if (isQuote())
            return quote();
        if (isHref())
            return href();
        if (isVar())
            return switchLabel(); // can only be in switch
        if (next.end())
            return new NullExpression();
        throw new AssertionError("unexpected start " + next);
    }

    private Expression postfix(Expression expression) {
        if (next.accept("-"))
            expression = MinusExpression.of(expression, expression());
        if (next.accept("×"))
            expression = new RepeatedExpression(expression, repetitions());
        if (next.accept("+"))
            expression = new RepeatedExpression(expression, "+");
        if (next.accept("?"))
            expression = new RepeatedExpression(expression, "?");
        if (next.accept("*"))
            expression = new RepeatedExpression(expression, "*");

        skipWhitespaceAndComments();

        if (next.accept("|")) {
            skipWhitespaceAndComments();
            if (next.more() && !next.accept(")")) // an empty trailing pipe is allowed :(
                expression = AlternativesExpression.of(expression, expression());
        } else if (next.accept("⇒")) {
            expression = switchExpression(expression);
        } else if (next.accept(")"))
            ; // simply return
        else if (next.more())
            expression = SequenceExpression.of(expression, expression());
        return expression;
    }

    private void skipWhitespaceAndComments() {
        int count;
        do
        {
            count = next.count(" ");
            if (next.accept("/*")) {
                next.readUntilAndSkip("*/");
                count++;
            }
            if (isBr()) {
                next.expectElement("br");
                count++;
            }
        }
        while (count > 0);
    }

    private boolean isBr() { return next.isElement("br"); }

    private Expression hex() {
        val hex = new StringBuilder();
        while (next.peek().isHex())
            hex.append(next.read());
        return new CodePointExpression(CodePoint.decode("0x" + hex));
    }

    private Expression range() {
        val from = hex();
        next.expect("-#x");
        val to = hex();
        next.expect("]");
        return new RangeExpression(from, to);
    }

    private boolean isQuote() { return next.accept("“"); }

    private Expression quote() {
        Element span = next.readElement();
        assert span.tagName().equals("span");
        assert span.className().equals("quote");
        CodePoint codePoint = CodePoint.of(span.text());
        next.expect("”");
        return new CodePointExpression(codePoint);
    }

    private boolean isHref() { return next.isElement("a") && next.peekElement().hasAttr("href"); }

    private Expression href() {
        var href = next.readElement().attr("href");
        assert href.startsWith("#");
        href = href.substring(1);
        return new ReferenceExpression(href);
    }

    private String repetitions() {
        skipWhitespaceAndComments();
        String repetitions = next.isText() ? next.read().toString() : readVar();
        skipWhitespaceAndComments();
        return repetitions;
    }

    private boolean isVar() { return next.isElement("code") && next.peekElement().className().equals("varname"); }

    private String readVar() {
        Element element = next.readElement();
        assert element.tagName().equals("code");
        assert element.className().equals("varname");
        return element.text();
    }

    private Expression switchExpression(Expression label) {
        skipWhitespaceAndComments();
        val switchExpression = new SwitchExpression();
        switchExpression.addCase(label).merge(switchValue());
        while (next.more()) {
            switchExpression.addCase(switchLabel());
            next.expect("⇒");
            switchExpression.merge(switchValue());
        }
        return switchExpression;
    }

    private LiteralExpression switchLabel() { return readLiteralUntil(() -> next.is("⇒")); }

    private LiteralExpression switchValue() { return readLiteralUntil(() -> next.isElement("br") || next.end()); }

    private LiteralExpression readLiteralUntil(Supplier<Boolean> end) {
        StringBuilder out = new StringBuilder();
        while (!end.get())
            if (isQuote())
                out.append(quote()).append(" ");
            else if (isVar())
                out.append(readVar()).append(" ");
            else if (isHref())
                out.append(href()).append(" ");
            else if (next.isText())
                out.appendCodePoint(next.read().value);
            else
                throw new AssertionError("unexpected switch literal " + next);
        skipWhitespaceAndComments();
        return new LiteralExpression(out.toString().trim());
    }
}
