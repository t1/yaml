package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.Spec.Production;
import lombok.SneakyThrows;
import lombok.experimental.var;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SpecLoader {
    private static final Path CACHE = Paths.get("target", "spec.html");

    Spec load() {
        return loadSpecFrom((Files.exists(CACHE)) ? loadHtmlFromCache() : fetchHtmlFromYamlOrg());
    }

    @SneakyThrows(IOException.class)
    private static Document loadHtmlFromCache() {
        System.out.println("load spec from " + CACHE);
        return Jsoup.parse(CACHE.toFile(), "UTF-8");
    }

    @SneakyThrows(IOException.class)
    private static Document fetchHtmlFromYamlOrg() {
        System.out.println("fetch spec from yaml.org");
        val document = Jsoup.connect("http://yaml.org/spec/1.2/spec.html").get();
        Files.write(CACHE, document.toString().getBytes());
        return document;
    }

    private Spec loadSpecFrom(Document document) {
        List<Production> productions = new ArrayList<>();
        for (val set : document.select("table.productionset table.productionset tr"))
            productions.add(parse(set));
        return new Spec(productions);
    }

    Production parse(Element set) {
        val counter = parseCounter(set);

        val argsMatcher = lhsMatcher(set);
        val name = argsMatcher.group("name");
        val args = argsMatcher.group("args");

        val expression = expression(set, counter, name);

        return new Production(counter, name, args, expression);
    }

    private Expression expression(Element set, int counter, String name) {
        Element rhs = set.selectFirst("td.productionrhs");
        try {
            val parser = new NodeExpressionParser(rhs.childNodes());
            return parser.expression();
        } catch (RuntimeException | AssertionError e) {
            val info = new StringBuilder();
            info.append("can't parse [").append(counter).append("][").append(name).append("]:\n\n").append(rhs).append("\n\n");
            for (int i = 0; i < rhs.childNodeSize(); i++)
                info.append("  ").append(i).append(": ").append(rhs.childNode(i)).append("\n");
            throw new RuntimeException(info.toString(), e);
        }
    }

    private int parseCounter(Element set) {
        var text = set.selectFirst("td.productioncounter").text();
        assert text.startsWith("[");
        assert text.endsWith("]");
        text = text.substring(1, text.length() - 1);
        return Integer.parseInt(text);
    }

    @NotNull private Matcher lhsMatcher(Element set) {
        String lhs = set.selectFirst("td.productionlhs").text();
        Matcher argsMatcher = Pattern.compile("(?<name>.*?)(\\((?<args>.*)\\))?").matcher(lhs);
        if (!argsMatcher.matches())
            throw new RuntimeException("unexpected lhs");
        return argsMatcher;
    }
}
