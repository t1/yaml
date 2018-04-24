package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import com.github.t1.yaml.parser.Expression.CodePointExpression;
import com.github.t1.yaml.parser.Expression.LiteralExpression;
import com.github.t1.yaml.parser.Expression.NullExpression;
import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import com.github.t1.yaml.parser.Expression.RepeatedExpression;
import com.github.t1.yaml.parser.Expression.SwitchExpression;
import lombok.RequiredArgsConstructor;
import lombok.experimental.var;
import lombok.val;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;
import java.util.Stack;

@RequiredArgsConstructor class NodeExpressionParser {
    private final List<Node> nodes;
    private boolean commentSkipping = false;
    private Node node;
    private Expression expression = new NullExpression();
    private final Stack<Expression> stack = new Stack<>();
    private int i = 0;

    Expression expression() {
        while (more()) {
            next();
            try {
                if (isText(node)) {
                    text();
                } else if (commentSkipping) {
                    // skip
                } else if (isSpan(node) && classAttr(node).equals("quote")) {
                    quoteSpan();
                } else if (isHref(node)) {
                    href();
                } else if (isCode(node) && classAttr(node).equals("varname")) {
                    variable();
                } else if (isBR(node)) {
                    if (expression instanceof SwitchExpression)
                        if (((SwitchExpression) expression).balanced())
                            ((SwitchExpression) expression).mergeCase(new NullExpression()); // br closes case
                } else {
                    throw new IllegalStateException("unexpected node: " + node);
                }
            } catch (RuntimeException | AssertionError e) {
                throw new RuntimeException("at node " + nodes.indexOf(node), e);
            }
        }
        assert stack.isEmpty();
        return expression;
    }

    private boolean more() { return i < nodes.size(); }

    private void next() { node = nodes.get(i++); }

    private void text() {
        var text = text(node);
        if (commentSkipping) {
            if (text.contains("*/")) {
                commentSkipping = false;
                text = text.substring(text.indexOf("*/") + 2);
            } else
                return;
        }

        if (text.contains(" /*") && !text.contains("*/")) {
            commentSkipping = true;
            text = text.substring(0, text.indexOf("/*"));
        }

        while (text.indexOf(" /*") < text.indexOf("*/"))
            text = text.substring(0, text.indexOf(" /*")) + text.substring(text.indexOf("*/") + 2);

        if (text.endsWith("“"))
            text = text.substring(0, text.length() - 1);
        if (text.startsWith("”"))
            text = text.substring(1);

        expression = new LineExpressionParser(text, expression, stack).expression();
    }

    private void quoteSpan() {
        val textNode = node.childNode(0);
        expression = expression.merge(new CodePointExpression(CodePoint.at(0, text(textNode))));
    }

    private void href() {
        var href = element(node).attr("href");
        assert href.startsWith("#");
        href = href.substring(1);
        expression = expression.merge(new ReferenceExpression(href));
    }

    private void variable() {
        val name = element(node).text();
        if (expression instanceof NullExpression) {
            expression = new SwitchExpression();
            switchExpression(name);
        } else if (expression instanceof SwitchExpression) {
            switchExpression(name);
        } else {
            RepeatedExpression repeatedExpression = (RepeatedExpression) expression;
            assert repeatedExpression.repetitions.isEmpty();
            repeatedExpression.repetitions = name;
        }
    }

    private void switchExpression(String variable) {
        LiteralExpression literal = new LiteralExpression(variable);
        val switchExpression = (SwitchExpression) this.expression;
        if (switchExpression.balanced() || switchExpression.lastCase() instanceof NullExpression) {
            switchExpression.mergeCase(literal);
        } else {
            switchExpression.merge(literal);
        }
    }


    private static boolean isText(Node node) { return node instanceof TextNode; }

    private static String text(Node node) { return ((TextNode) node).text(); }

    private static boolean isHref(Node node) { return "a".equals(tagName(node)) && element(node).hasAttr("href"); }

    private static boolean isBR(Node node) { return "br".equals(tagName(node)); }

    private static boolean isSpan(Node node) { return "span".equals(tagName(node)); }

    private static boolean isCode(Node node) { return "code".equals(tagName(node)); }

    private static String classAttr(Node node) { return element(node).attr("class"); }

    private static String tagName(Node node) { return isElement(node) ? element(node).tagName() : null; }

    private static boolean isElement(Node node) { return node instanceof Element; }

    private static Element element(Node node) { return (Element) node; }
}
