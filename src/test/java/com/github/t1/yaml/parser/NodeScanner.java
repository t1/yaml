package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;
import java.util.function.Supplier;

import static com.github.t1.yaml.dump.CodePoint.EOF;

/**
 * Scans an html snippet which includes seamless scanning of text nodes.
 */
@RequiredArgsConstructor class NodeScanner {
    private final List<Node> nodes;
    private int i = 0;
    private Scanner nextText;

    @Override public String toString() {
        return more() ? "in node " + i + ": [" + ((nextText == null) ? node() : nextText) + "]" : "<end>";
    }

    private Node node() { return nodes.get(i); }

    private void next() { nextText = null; i++; }

    boolean end() { return !more(); }

    boolean more() { return i < nodes.size() || nextText != null && nextText.more(); }

    boolean isText() { return more() && node() instanceof TextNode && withText(() -> nextText.more()); }

    int count(String text) { return isText() ? withText(() -> nextText.count(text)) : 0; }


    boolean accept(String text) {
        if (!more() || !isText())
            return false;
        return withText(() -> nextText.accept(text));
    }

    boolean is(String text) { return nextText != null && nextText.is(text); }

    CodePoint peek() { return (nextText == null) ? EOF : nextText.peek(); }

    CodePoint read() { return withText(() -> nextText.read()); }

    String readUntil(String end) { return withText(() -> nextText.readUntil(end)); }

    String readUntilAndSkip(String end) {
        StringBuilder out = new StringBuilder();
        while (more() && !accept(end)) {
            if (isText())
                out.append(readUntil(end));
            else
                out.append(readElement().html());
        }
        return out.toString();
    }

    NodeScanner expect(String text) {
        assert more();
        withText(() -> nextText.expect(text));
        return this;
    }

    private <T> T withText(Supplier<T> runnable) {
        assert more();
        assert node() instanceof TextNode;
        if (nextText == null)
            nextText = new Scanner(((TextNode) node()).text().trim());
        T result = runnable.get();
        if (nextText.end())
            next();
        return result;
    }

    Element peekElement() { return (Element) node(); }

    Element readElement() {
        Node node = node();
        next();
        return (Element) node;
    }

    boolean isElement(String tagName) {
        if (isText() || end())
            return false;
        Node node = node();
        return (node instanceof Element && ((Element) node).tagName().equals(tagName));
    }

    void expectElement(String tagName) {
        assert more();
        assert !isText();
        assert isElement(tagName);
        readElement();
    }
}
