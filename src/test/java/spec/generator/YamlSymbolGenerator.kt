package spec.generator

import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.ContainerExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression
import spec.generator.Expression.Visitor
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths

class YamlSymbolGenerator(private val spec: Spec) {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val spec = SpecLoader().load()
            YamlSymbolGenerator(spec).generateCode()
        }

        private val SOURCE_FILE = Paths.get("src/main/java/com/github/t1/yaml/parser/YamlTokens.kt")

        val PREFIX = "" +
            "package com.github.t1.yaml.parser\n" +
            "\n" +
            "import com.github.t1.yaml.tools.CodePoint\n" +
            "import com.github.t1.yaml.tools.CodePointRange\n" +
            "import com.github.t1.yaml.tools.Match\n" +
            "import com.github.t1.yaml.tools.Scanner\n" +
            "import com.github.t1.yaml.tools.Token\n" +
            "import com.github.t1.yaml.tools.symbol\n" +
            "import com.github.t1.yaml.tools.toCodePointRange\n" +
            "import com.github.t1.yaml.tools.undefined\n" +
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
            "@Suppress(\"unused\", \"EnumEntryName\", \"NonAsciiCharacters\")\n"

        const val SUFFIX = "" +
            "    ;\n" +
            "\n" +
            "\n" +
            "    @Deprecated(\"not yet generated\") constructor() : this(undefined)\n" +
            "    constructor(codePoint: Char) : this(symbol(codePoint))\n" +
            "    constructor(range: CharRange) : this(range.toCodePointRange())\n" +
            "    constructor(range: CodePointRange) : this(symbol(range))\n" +
            "\n" +
            "    override fun match(scanner: Scanner): Match = this.token.match(scanner)\n" +
            "}\n" +
            "\n" +
            "private infix fun Char.or(that: Char) = symbol(this) or symbol(that)\n" +
            "private infix fun Char.or(that: Token) = symbol(this) or that\n" +
            "private infix fun Token.or(that: String): Token = or(symbol(that))\n" +
            "private infix fun Token.or(that: Char): Token = or(symbol(that))\n" +
            "private infix operator fun Char.rangeTo(that: Char) = symbol(CodePoint.of(this) .. CodePoint.of(that))\n" +
            "private infix operator fun Char.rangeTo(that: String) = symbol(CodePoint.of(this) .. CodePoint.of(that))\n" +
            "private infix operator fun String.rangeTo(that: String) = symbol(CodePoint.of(this) .. CodePoint.of(that))\n" +
            "private infix operator fun Char.plus(that: Char) = symbol(this) + symbol(that)\n" +
            "private infix operator fun Token.plus(that: Char) = this + symbol(that)\n" +
            "private infix fun Token.or(range: CharRange) = this.or(symbol(range.toCodePointRange()))\n"
    }

    private fun generateCode() {
        var className = SOURCE_FILE.fileName.toString()
        val suffix = ".kt"
        assert(className.endsWith(suffix))
        className = className.substring(0, className.length - suffix.length)
        Files.newBufferedWriter(SOURCE_FILE).use { writer -> generateCode(className, writer) }
    }

    fun generateCode(className: String, out: Writer) {
        SourceCodeGenerator(className, out).write(spec.productions)
    }

    class SourceCodeGenerator(private val className: String, private val out: Writer) {
        private fun append(string: String) = out.append(string)

        fun write(productions: List<Production>) {
            append(PREFIX +
                "enum class $className(private val token: Token) : Token {\n")

            for (production in productions) {
                ProductionWriter(production).write()
            }

            append(SUFFIX)
        }

        private inner class ProductionWriter(val production: Production) : Visitor() {
            fun write() {
                append("\n" +
                    "    /**\n" +
                    "     * ${production.toString().replace("\n", "\n     * ")}\n" +
                    "     */\n" +
                    "    `$methodName`(")
                if (production.counter in setOf(27, 37, 81, 87, 89, 93, 96, 97, 98, 126, 139, 142, 143, 144, 150, 151, 159, 161, 185, 188, 196, 198))
                    append("undefined /* TODO not generated */")
                else
                    production.expression.guide(this)
                append("),\n")
            }

            private val Expression.isCurrent get() = this === production.expression

            private fun ContainerExpression.isFirst(expression: Expression) = this.expressions.first() === expression

            private val methodName get() = production.name + if (production.args == null) "" else "(${production.args.replace("<", "«")})"

            override fun visit(codePoint: CodePointExpression) {
                if (codePoint.isCurrent)
                    append(string(codePoint))
            }

            override fun visit(reference: ReferenceExpression) {
                append("`${reference.ref}`")
            }

            override fun visit(sequence: SequenceExpression) = object : Visitor() {
                override fun visit(codePoint: CodePointExpression) {
                    if (!sequence.isFirst(codePoint))
                        append(" + ")
                    append("'${codePoint.codePoint.escaped}'")
                }
            }

            override fun visit(alternatives: AlternativesExpression) = object : Visitor() {
                override fun visit(reference: ReferenceExpression) {
                    if (!alternatives.isFirst(reference))
                        append(" or ")
                    this@ProductionWriter.visit(reference)
                }

                override fun visit(range: RangeExpression): Visitor {
                    if (!alternatives.isFirst(range))
                        append(" or ")
                    append("(")
                    this@ProductionWriter.visit(range)
                    append(")")
                    return object : Visitor() {}
                }

                override fun visit(codePoint: CodePointExpression) {
                    if (!alternatives.isFirst(codePoint))
                        append(" or ")
                    append(string(codePoint)) // TODO this@ProductionWriter.visit(codePoint)
                }

                override fun visit(sequence: SequenceExpression): Visitor {
                    append("(")
                    return object : Visitor() {
                        override fun visit(codePoint: CodePointExpression) {
                            if (!sequence.isFirst(codePoint))
                                append(" + ")
                            append("'${codePoint.codePoint.escaped}'")
                        }

                        override fun visit(reference: ReferenceExpression) {
                            if (!sequence.isFirst(reference))
                                append(" + ")
                            this@ProductionWriter.visit(reference)
                        }
                    }
                }

                override fun leave(sequence: SequenceExpression) {
                    append(")")
                }
            }

            override fun visit(minus: MinusExpression) = object : Visitor() {}
            override fun visit(repeated: RepeatedExpression) = object : Visitor() {}
            override fun visit(range: RangeExpression): Visitor {
                append(string(range.left as CodePointExpression)) // TODO range.left.guide(this)
                append(" .. ")
                append(string(range.right as CodePointExpression)) // TODO range.right.guide(this)
                return this
            }

            private fun string(codePoint: CodePointExpression): String {
                val quote = if (codePoint.codePoint.isBig) '\"' else '\''
                return "$quote${codePoint.codePoint.escaped}$quote"
            }

            override fun visit(switch: SwitchExpression) = object : Visitor() {}
        }
    }
}
