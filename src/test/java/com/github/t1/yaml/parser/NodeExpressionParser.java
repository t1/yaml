package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import com.github.t1.yaml.parser.Expression.AlternativesExpression;
import com.github.t1.yaml.parser.Expression.CodePointExpression;
import com.github.t1.yaml.parser.Expression.MinusExpression;
import com.github.t1.yaml.parser.Expression.NullExpression;
import com.github.t1.yaml.parser.Expression.RangeExpression;
import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import com.github.t1.yaml.parser.Expression.RepeatedExpression;
import com.github.t1.yaml.parser.Expression.SequenceExpression;
import lombok.experimental.var;
import lombok.val;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.List;

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
        if (next.accept("“"))
            return quote();
        if (next.isElement("a"))
            return href(next.readElement());
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
            if (next.isElement("br")) {
                next.expectElement("br");
                count++;
            }
        }
        while (count > 0);
    }

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

    private Expression quote() {
        Element span = next.readElement();
        assert span.tagName().equals("span");
        assert span.className().equals("quote");
        CodePoint codePoint = CodePoint.of(span.text());
        next.expect("”");
        return new CodePointExpression(codePoint);
    }

    private Expression href(Element element) {
        var href = element.attr("href");
        assert href.startsWith("#");
        href = href.substring(1);
        return new ReferenceExpression(href);
    }

    private String repetitions() {
        skipWhitespaceAndComments();
        String repetitions;
        if (next.isText()) {
            repetitions = next.read().toString();
        } else {
            Element element = next.readElement();
            assert element.tagName().equals("code");
            assert element.className().equals("varname");
            repetitions = element.text();
        }
        skipWhitespaceAndComments();
        return repetitions;
    }
}
