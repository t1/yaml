package com.github.t1.yaml.parser;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

@Accessors(fluent = true, chain = true)
@RequiredArgsConstructor
public class TokensGenerator {
    private static final Path CACHE = Paths.get("target", "spec.html");

    public static void main(String[] args) {
        new TokensGenerator(production -> {
            System.out.println(production);
            System.out.flush();
        }).run();
    }

    private final Consumer<Production> consumer;

    void run() { document((Files.exists(CACHE)) ? load() : fetch()); }

    @SneakyThrows(IOException.class)
    private static Document load() {
        System.out.println("load spec from " + CACHE);
        return Jsoup.parse(CACHE.toFile(), "UTF-8");
    }

    @SneakyThrows(IOException.class)
    private static Document fetch() {
        System.out.println("fetch spec from yaml.org");
        val document = Jsoup.connect("http://yaml.org/spec/1.2/spec.html").get();
        Files.write(CACHE, document.toString().getBytes());
        return document;
    }

    private void document(Document document) {
        for (val set : document.select("table.productionset table.productionset tr"))
            production(set);
    }

    private void production(Element set) {
        val production = new Production(set);
        consumer.accept(production);
    }

    @Data static class Production {
        final int counter;
        final String name;
        final String args;

        Expression expression;

        Production(Element set) {
            this.counter = parseCounter(set);

            val argsMatcher = lhsMatcher(set);
            this.name = argsMatcher.group("name");
            this.args = argsMatcher.group("args");

            if (asList(67, 80, 110, 121, 127, 131, 136, 163, 164, 165, 166, 201).contains(counter))
                return;
            Element rhs = set.selectFirst("td.productionrhs");
            try {
                val parser = new NodeExpressionParser(rhs.childNodes());
                this.expression = parser.expression();
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


        @Override public String toString() {
            return "[" + counter + "] : " + name + ((args == null) ? "" : " [" + args + "]") + ":\n"
                    + "  " + expression;
        }
    }
}
