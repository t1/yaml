package spec.generator

import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.LiteralExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.Visitor
import java.io.IOException
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors.joining
import java.util.stream.Stream

class ParserGenerator(private val spec: Spec) {

    private fun generateJavaCode() {
        var className = ParserGenerator.JAVA_SOURCE.fileName.toString()
        assert(className.endsWith(".java"))
        className = className.substring(0, className.length - 5)
        try {
            Files.newBufferedWriter(ParserGenerator.JAVA_SOURCE).use { writer -> generateJavaCode(className, writer) }
        } catch (e: IOException) {
            throw RuntimeException("while generating ${ParserGenerator.JAVA_SOURCE}", e)
        }

    }

    @Throws(IOException::class)
    fun generateJavaCode(className: String, out: Writer) {
        ParserGenerator.JavaCodeGenerator(className, out).write(spec.productions)
    }

    class JavaCodeGenerator(private val className: String, private val out: Writer) {
        private var production: Production? = null

        @Throws(IOException::class)
        fun write(productions: List<Production>) {
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
                "@Generated(\"").append(ParserGenerator::class.java.name).append("\")\n" +
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
                "    public boolean more() { return next.more(); }\n")

            for (p in productions) {
                production = p
                writeProduction()
            }

            out.append("}\n")
        }

        @Throws(IOException::class)
        private fun writeProduction() {
            out.append("\n").append(methodComment())
            out.append("    private Object ").append(methodName()).append("(").append(args()).append(") {\n")
            writeBody()
            out.append("    }\n")
        }

        private fun methodComment(): String {
            return "" +
                "    /**\n" +
                "     * " + production!!.toString().replace("\n", "\n" + "     * ") + "\n" +
                "     */\n"
        }

        private fun methodName(): String {
            return methodName(production!!.name) + if (production!!.args == null)
                ""
            else
                "" +
                    (if (production!!.args!!.contains("<")) "_less" else "") +
                    if (production!!.args!!.contains("≤")) "_lessEq" else ""
        }

        private fun methodName(name: String): String {
            return name.replace("-", "_").replace("+", "_")
        }

        private fun args(): String {
            return if (production!!.args == null || production!!.args!!.isEmpty())
                ""
            else
                Stream.of(*production!!.args!!.replace("≤", "").replace("<", "")
                    .split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()).collect(joining(", int ", "int ", ""))
        }

        @Throws(IOException::class)
        private fun writeBody() {
            val productionBodyWriter = ProductionBodyWriter()
            production!!.expression.guide(productionBodyWriter)
            if (!productionBodyWriter.written)
                append("        return null;\n")
        }

        @Throws(IOException::class)
        private fun append(text: String) {
            out.append(text)
        }

        private inner class ProductionBodyWriter : Visitor() {
            var written = false

            @Throws(IOException::class)
            override fun visit(literalExpression: LiteralExpression) {
                if (!written) {
                    written = true
                    append("        return next.accept(\"" + literalExpression.literal + "\") ? \"" + literalExpression.literal + "\" : null;\n")
                }
            }

            @Throws(IOException::class)
            override fun visit(referenceExpression: ReferenceExpression) {
                written = true
                append("        return " + methodName(referenceExpression.ref) + "();\n")
            }

            @Throws(IOException::class)
            override fun visit(alternativesExpression: AlternativesExpression): Visitor {
                written = true
                append("        Object result = ")
                return object : Visitor() {
                    private var first = true

                    @Throws(IOException::class)
                    override fun visit(referenceExpression: ReferenceExpression) {
                        if (first) {
                            first = false
                            append(methodName(referenceExpression.ref) + "();\n")
                        } else {
                            append("" +
                                "        if (result == null)\n" +
                                "            result = " + methodName(referenceExpression.ref) + "();\n")
                        }
                    }

                    @Throws(IOException::class)
                    override fun visit(codePointExpression: CodePointExpression) {
                        val codePoint = "CodePoint.of(0x" + codePointExpression.codePoint.hex() + ")"
                        val text = "next.is($codePoint) ? $codePoint : null;\n"
                        if (first) {
                            first = false
                        } else {
                            append("" +
                                "        if (result == null)\n" +
                                "            result = ")
                        }
                        append(text)
                    }

                    @Throws(IOException::class)
                    override fun leave(alternativesExpression: AlternativesExpression) {
                        append("" +
                            "        if (result == null)\n" +
                            "            throw new YamlParseException(\"can't find " + production!!.name + "\" + next);\n" +
                            "        return result;\n")
                    }
                }
            }
        }
    }

    companion object {
        private val JAVA_SOURCE = Paths.get("src/main/java/com/github/t1/yaml/parser/GeneratedParser.java")

        @Throws(IOException::class)
        @JvmStatic fun main(args: Array<String>) {
            val spec = SpecLoader().load()
            ParserGenerator(spec).generateJavaCode()
        }
    }
}
