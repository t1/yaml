package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.Expression.AlternativesExpression;
import com.github.t1.yaml.parser.Expression.CodePointExpression;
import com.github.t1.yaml.parser.Expression.LiteralExpression;
import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import com.github.t1.yaml.parser.Expression.Visitor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

public class ParserGenerator {
    private static final Path CACHE = Paths.get("target", "spec.html");
    private static final Path JAVA_SOURCE = Paths.get("src/main/java/com/github/t1/yaml/parser/GeneratedParser.java");

    public static void main(String[] args) {
        new ParserGenerator().loadSpec().generateJavaCode(JAVA_SOURCE);
    }

    final List<Production> productions = new ArrayList<>();
    private final Map<String, Production> index = new TreeMap<>();

    ParserGenerator loadSpec() {
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

    void generateJavaCode(String className, Writer out) {
        new JavaCodeGenerator(className, out).write(productions);
    }

    @RequiredArgsConstructor
    private static class JavaCodeGenerator {
        private final String className;
        private final Writer out;
        private Production production;

        @SneakyThrows(IOException.class)
        void write(List<Production> productions) {
            out.append("" +
                    "package com.github.t1.yaml.parser;\n" +
                    "\n" +
                    "import com.github.t1.yaml.dump.CodePoint;\n" +
                    "import com.github.t1.yaml.model.Document;\n" +
                    "\n" +
                    "import javax.annotation.Generated;\n" +
                    "import java.io.BufferedReader;\n" +
                    "import java.io.InputStream;\n" +
                    "import java.io.InputStreamReader;\n" +
                    "import java.io.Reader;\n" +
                    "import java.io.StringReader;\n" +
                    "import java.util.Optional;\n" +
                    "\n" +
                    "import static java.nio.charset.StandardCharsets.UTF_8;\n" +
                    "\n" +
                    "@Generated(\"").append(ParserGenerator.class.getName()).append("\")\n" +
                    "@lombok.Generated\n" +
                    "public class ").append(className).append(" {\n" +
                    "    private final Scanner next;\n" +
                    "    private Document document;\n" +
                    "\n" +
                    "    public ").append(className).append("(String yaml) { this(new StringReader(yaml)); }\n" +
                    "\n" +
                    "    public ").append(className).append("(InputStream inputStream) { this(new BufferedReader(new InputStreamReader(inputStream, UTF_8))); }\n" +
                    "\n" +
                    "    public ").append(className).append("(Reader reader) { this.next = new Scanner(reader); }\n" +
                    "\n" +
                    "    public Optional<Document> document() {\n" +
                    "        l_bare_document();\n" +
                    "        return Optional.ofNullable(document);\n" +
                    "    }\n" +
                    "\n" +
                    "    public boolean more() { return next.more(); }\n");

            for (Production p : productions) {
                production = p;
                writeProduction();
            }

            out.append("}\n");
        }

        private void writeProduction() throws IOException {
            out.append("\n").append(methodComment());
            out.append("    private Object ").append(methodName()).append("(").append(args()).append(") {\n");
            writeBody();
            out.append("    }\n");
        }

        private String methodComment() {
            return "" +
                    "    /**\n" +
                    "     * " + production.toString().replace("\n", "\n" +
                    "     * ") + "\n" +
                    "     */\n";
        }

        private String methodName() {
            return methodName(production.name)
                    + ((production.args == null) ? "" : ""
                    + (production.args.contains("<") ? "_less" : "")
                    + (production.args.contains("≤") ? "_lessEq" : ""));
        }

        private String methodName(String name) {
            return name.replace("-", "_").replace("+", "_");
        }

        private String args() {
            return (production.args == null || production.args.isEmpty()) ? "" :
                    Stream.of(production.args.replace("≤", "").replace("<", "")
                            .split(",")).collect(joining(", int ", "int ", ""));
        }

        private void writeBody() {
            ProductionBodyWriter productionBodyWriter = new ProductionBodyWriter();
            production.expression.guide(productionBodyWriter);
            if (!productionBodyWriter.written)
                append("        return null;\n");
        }

        @SneakyThrows(IOException.class)
        private void append(String text) { out.append(text); }

        private class ProductionBodyWriter extends Visitor {
            private boolean written = false;

            @Override void visit(LiteralExpression literal) {
                if (!written) {
                    written = true;
                    append("        return next.accept(\"" + literal.literal + "\") ? \"" + literal.literal + "\" : null;\n");
                }
            }

            @Override void visit(ReferenceExpression reference) {
                written = true;
                append("        return " + methodName(reference.ref) + "();\n");
            }

            @Override Visitor visit(AlternativesExpression alternatives) {
                written = true;
                append("        Object result = ");
                return new Visitor() {
                    private boolean first = true;

                    @Override void visit(ReferenceExpression reference) {
                        if (first) {
                            first = false;
                            append(methodName(reference.ref) + "();\n");
                        } else {
                            append("" +
                                    "        if (result == null)\n" +
                                    "            result = " + methodName(reference.ref) + "();\n");
                        }
                    }

                    @Override void visit(CodePointExpression codePointExpression) {
                        String codePoint = "CodePoint.of(0x" + codePointExpression.codePoint.hex() + ")";
                        String text = "next.is(" + codePoint + ") ? " + codePoint + " : null;\n";
                        if (first) {
                            first = false;
                        } else {
                            append("" +
                                    "        if (result == null)\n" +
                                    "            result = ");
                        }
                        append(text);
                    }

                    @Override void leave(AlternativesExpression alternatives) {
                        append("" +
                                "        if (result == null)\n" +
                                "            throw new YamlParseException(\"can't find " + production.name + "\" + next);\n" +
                                "        return result;\n");
                    }
                };
            }
        }
    }
}
