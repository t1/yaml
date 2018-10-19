package com.github.t1.yaml.parser;

import com.github.t1.yaml.parser.Expression.AlternativesExpression;
import com.github.t1.yaml.parser.Expression.CodePointExpression;
import com.github.t1.yaml.parser.Expression.LiteralExpression;
import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import com.github.t1.yaml.parser.Expression.Visitor;
import com.github.t1.yaml.parser.Spec.Production;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class ParserGenerator {
    private static final Path JAVA_SOURCE = Paths.get("src/main/java/com/github/t1/yaml/parser/GeneratedParser.java");
    private Spec spec;

    public static void main(String[] args) {
        Spec spec = new SpecLoader().load();
        new ParserGenerator(spec).generateJavaCode();
    }

    ParserGenerator(Spec spec) { this.spec = spec; }

    private void generateJavaCode() {
        String className = JAVA_SOURCE.getFileName().toString();
        assert className.endsWith(".java");
        className = className.substring(0, className.length() - 5);
        try (Writer writer = Files.newBufferedWriter(JAVA_SOURCE)) {
            generateJavaCode(className, writer);
        } catch (IOException e) {
            throw new RuntimeException("while generating " + JAVA_SOURCE, e);
        }
    }

    void generateJavaCode(String className, Writer out) {
        new JavaCodeGenerator(className, out).write(spec.productions);
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
