package spec.generator

import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression
import spec.generator.Expression.Visitor
import java.io.IOException
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths

class YamlSymbolGenerator(private val spec: Spec) {
    companion object {
        private val SOURCE_FILE = Paths.get("src/main/java/com/github/t1/yaml/parser/YamlTokens.kt")

        @JvmStatic fun main(args: Array<String>) {
            val spec = SpecLoader().load()
            YamlSymbolGenerator(spec).generateCode()
        }
    }

    private fun generateCode() {
        var className = SOURCE_FILE.fileName.toString()
        val suffix = ".kt"
        assert(className.endsWith(suffix))
        className = className.substring(0, className.length - suffix.length)
        try {
            Files.newBufferedWriter(SOURCE_FILE).use { writer -> generateCode(className, writer) }
        } catch (e: IOException) {
            throw RuntimeException("while generating $SOURCE_FILE", e)
        }
    }

    fun generateCode(className: String, out: Writer) {
        SourceCodeGenerator(className, out).write(spec.productions)
    }

    class SourceCodeGenerator(private val className: String, private val out: Writer) {
        private fun append(string: String) = out.append(string)

        fun write(productions: List<Production>) {
            append("" +
                "package com.github.t1.yaml.parser\n" +
                "\n" +
                "import com.github.t1.yaml.tools.CodePoint\n" +
                "import com.github.t1.yaml.tools.Symbol\n" +
                "import com.github.t1.yaml.tools.Token\n" +
                "import com.github.t1.yaml.tools.symbol\n" +
                "import javax.annotation.Generated\n" +
                "\n" +
                "/**\n" +
                " * The productions as specified in the YAML spec\n" +
                " *\n" +
                " * e-        A production matching no characters.\n" +
                " * c-        A production starting and ending with a special character.\n" +
                " * b-        A production matching a single line break.\n" +
                " * nb-       A production starting and ending with a non-break character.\n" +
                " * s-        A production starting and ending with a white space character.\n" +
                " * ns-       A production starting and ending with a non-space character.\n" +
                " * l-        A production matching complete line(s).\n" +
                " * X-Y-      A production starting with an X- character and ending with a Y- character, where X- and Y- are any of the above prefixes.\n" +
                " * X+, X-Y+  A production as above, with the additional property that the matched content indentation level is greater than the specified n parameter.\n" +
                " */\n" +
                "@Generated(\"${YamlSymbolGenerator::class.qualifiedName}\")\n" +
                "@Suppress(\"unused\", \"EnumEntryName\", \"NonAsciiCharacters\")\n" +
                "enum class $className(override val predicates: List<(CodePoint) -> Boolean>) : Token {\n")

            for (production in productions) {
                ProductionWriter(production).write()
            }

            append("" +
                "    ;\n" +
                "\n" +
                "    constructor(codePoint: Char) : this(symbol(codePoint))\n" +
                "    constructor(symbol: Symbol) : this(listOf(symbol.predicate))\n" +
                "    constructor(token: Token) : this(token.predicates)\n" +
                "    @Deprecated(\"missing production visitor\") constructor() : this(listOf())\n" +
                "}\n")
        }

        private inner class ProductionWriter(val production: Production) : Visitor() {
            fun write() {
                append("\n" +
                    "    /**\n" +
                    "     * ${production.toString().replace("\n", "\n     * ")}\n" +
                    "     */\n")
                if (production.counter in setOf(87, 98))
                    append("// TODO to be fixed\n")
                else {
                    append("    `$methodName`(")
                    production.expression.guide(this)
                    append("),\n")
                }
            }

            private val Expression.isCurrent get() = this === production.expression

            private fun SequenceExpression.isFirst(expression: Expression) = this.expressions.first() === expression

            private val methodName get() = production.name + if (production.args == null) "" else "(${production.args.replace("<", "«")})"

            override fun visit(codePoint: CodePointExpression) {
                if (codePoint.isCurrent)
                    append("'${codePoint.codePoint.escaped}'")
            }

            override fun visit(reference: ReferenceExpression) {
                append("`${reference.ref}`")
            }

            override fun visit(sequence: SequenceExpression) = object : Visitor() {
                override fun visit(codePoint: CodePointExpression) {
                    if (!sequence.isFirst(codePoint))
                        append(" + ")
                    append("symbol('${codePoint.codePoint.escaped}')")
                }
            }

            override fun visit(alternatives: AlternativesExpression) = object : Visitor() {}
            override fun visit(minus: MinusExpression) = object : Visitor() {}
            override fun visit(repeated: RepeatedExpression) = object : Visitor() {}
            override fun visit(range: RangeExpression) = object : Visitor() {}
            override fun visit(switch: SwitchExpression) = object : Visitor() {}
        }
    }
}
