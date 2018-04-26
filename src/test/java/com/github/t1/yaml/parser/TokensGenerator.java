package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.var;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class TokensGenerator {
    private static final Path CACHE = Paths.get("target", "spec.html");
    private static final Path JAVA_SOURCE = Paths.get("src/main/java/com/github/t1/yaml/parser/GeneratedParser.java");

    public static void main(String[] args) {
        new TokensGenerator().loadSpec().generateJavaCode(JAVA_SOURCE);
    }

    final List<Production> productions = new ArrayList<>();
    private final Map<String, Production> index = new TreeMap<>();

    TokensGenerator loadSpec() {
        loadSpecFrom((Files.exists(CACHE)) ? load() : fetch());
        return this;
    }

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

    private void loadSpecFrom(Document document) {
        for (val set : document.select("table.productionset table.productionset tr"))
            productions.add(parse(set));
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
        int counter;
        @NonNull String name;
        String args;

        @NonNull Expression expression;

        private final Map<String, Production> references = new HashMap<>();

        String getKey() { return name + ((args == null) ? "" : "(" + args + ")"); }

        @Override public String toString() {
            return "[" + counter + "] : " + name + ((args == null) ? "" : " [" + args + "]") + ":\n"
                    + "  " + expression;
        }
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

    private void generateJavaCode(Path path) {
        String className = path.getFileName().toString();
        assert className.endsWith(".java");
        className = className.substring(0, className.length() - 5);
        try (Writer writer = Files.newBufferedWriter(JAVA_SOURCE)) {
            generateJavaCode(className, writer);
        } catch (IOException e) {
            throw new RuntimeException("while generating " + path, e);
        }
    }

    void generateJavaCode(String className, Writer out) throws IOException {
        out.append(""
                + "package com.github.t1.yaml.parser;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "@Generated(\"\")\n"
                + "@lombok.Generated\n"
                + "public class ").append(className).append(" {\n");

        boolean first = true;
        for (Production production : productions) {
            if (first)
                first = false;
            else
                out.append("\n");
            write(production, out);
        }

        out.append("}\n");
    }

    private void write(Production production, Writer out) throws IOException {
        out.append("    void ").append(methodName(production)).append("(").append(args(production)).append(") {\n");
        out.append("    }\n");
    }

    private String methodName(Production production) {
        return production.name.replace("-", "_").replace("+", "_")
                + (production.args.contains("<") ? "_less" : "")
                + (production.args.contains("≤") ? "_lessEq" : "");
    }

    private String args(Production production) {
        return (production.args == null || production.args.isEmpty()) ? "" :
                Stream.of(production.args.replace("≤", "").replace("<", "")
                        .split(",")).collect(joining(", int ", "int ", ""));
    }
}
