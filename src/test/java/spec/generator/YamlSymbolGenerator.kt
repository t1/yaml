package spec.generator

import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.ContainerExpression
import spec.generator.Expression.EqualsExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression
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
            "@file:Suppress(\"unused\", \"FunctionName\", \"NonAsciiCharacters\")\n" +
            "\n" +
            "package com.github.t1.yaml.parser\n" +
            "\n" +
            "import com.github.t1.yaml.parser.InOutMode.`block-out`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`block-in`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`flow-out`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`flow-in`\n" +
            "import com.github.t1.yaml.parser.YamlTokens.`s-space`\n" +
            "import com.github.t1.yaml.tools.CodePoint\n" +
            "import com.github.t1.yaml.tools.CodePointRange\n" +
            "import com.github.t1.yaml.tools.CodePointReader\n" +
            "import com.github.t1.yaml.tools.Match\n" +
            "import com.github.t1.yaml.tools.Token\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once\n" +
            "import com.github.t1.yaml.tools.empty\n" +
            "import com.github.t1.yaml.tools.symbol\n" +
            "import com.github.t1.yaml.tools.toCodePointRange\n" +
            "import com.github.t1.yaml.tools.token\n" +
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
            "@Suppress(\"EnumEntryName\")\n"

        const val SUFFIX = "" +
            "    ;\n" +
            "\n" +
            "\n" +
            "    @Deprecated(\"not yet generated\") constructor() : this(undefined)\n" +
            "    constructor(codePoint: Char) : this(symbol(codePoint))\n" +
            "    constructor(range: CharRange) : this(range.toCodePointRange())\n" +
            "    constructor(range: CodePointRange) : this(symbol(range))\n" +
            "\n" +
            "    override fun match(reader: CodePointReader): Match = this.token.match(reader)\n" +
            "}\n" +
            "\n" +
            "private infix fun Char.or(that: Char) = symbol(this) or symbol(that)\n" +
            "private infix fun Char.or(that: Token) = symbol(this) or that\n" +
            "private infix fun CharRange.or(that: CharRange): Token = symbol(CodePoint.of(this.first)..CodePoint.of(this.last)) or symbol(CodePoint.of(that.first)..CodePoint.of(that.last))\n" +
            "private infix fun Token.or(that: String): Token = or(symbol(that))\n" +
            "private infix fun Token.or(that: Char): Token = or(symbol(that))\n" +
            "private infix operator fun Char.rangeTo(that: Char) = symbol(CodePoint.of(this)..CodePoint.of(that))\n" +
            "private infix operator fun Char.rangeTo(that: String) = symbol(CodePoint.of(this)..CodePoint.of(that))\n" +
            "private infix operator fun String.rangeTo(that: String) = symbol(CodePoint.of(this)..CodePoint.of(that))\n" +
            "private infix operator fun Char.plus(that: Char) = symbol(this) + symbol(that)\n" +
            "private infix operator fun Char.plus(token: Token) = symbol(this) + token\n" +
            "private infix operator fun Token.plus(that: Char) = this + symbol(that)\n" +
            "private infix fun Token.or(range: CharRange) = this.or(symbol(range.toCodePointRange()))\n" +
            "private val EOF = symbol(CodePoint.EOF)\n"
    }

    private fun generateCode() {
        var className = SOURCE_FILE.fileName.toString()
        val suffix = ".kt"
        assert(className.endsWith(suffix))
        className = className.substring(0, className.length - suffix.length)
        Files.newBufferedWriter(SOURCE_FILE).use { writer -> generateCode(className, writer) }
    }

    fun generateCode(className: String, out: Writer) {
        SourceCodeGenerator(className, spec, out).write()
    }

    class SourceCodeGenerator(
        private val className: String,
        private val spec: Spec,
        private val out: Writer
    ) {
        private val productions = spec.productions

        private fun write(string: String) {
            out.append(string)
        }

        fun write() {
            write(PREFIX +
                "enum class $className(private val token: Token) : Token {\n")

            for (production in productions.filter { production -> production.args.isEmpty() }) {
                try {
                    if (production.counter !in setOf(87, 89, 93, 97, 98, 103, 111, 114, 122, 123, 126, 139, 142, 193, 207, 210))
                        ProductionWriter(production).writeEnumEntry()
                } catch (e: Exception) {
                    throw RuntimeException("can't write enum entry for ${production.counter}: ${production.key}", e)
                }
            }

            write(SUFFIX)

            for (production in productions.filter { production -> !production.args.isEmpty() }) {
                try {
                    if (production.counter <= 69)
                        ProductionWriter(production).writeFactoryFun()
                } catch (e: Exception) {
                    throw RuntimeException("can't write factory method for ${production.counter}: ${production.key}", e)
                }
            }
        }

        private inner class ProductionWriter(val production: Production) : OrFailVisitor() {
            private var depth = 0

            fun writeEnumEntry() {
                writeComment("    ")
                write("    `${production.name}`(")
                production.expression.guide(this)
                write("),\n")
            }

            fun writeFactoryFun() {
                writeComment("")
                write("fun `${funName()}`(")
                writeArgs()
                write(")")
                when {
                    onlyArgOrEmpty.startsWith("<") || onlyArgOrEmpty.startsWith("≤") -> writeLessFun(onlyArgOrEmpty[0])
                    production.expression is ReferenceExpression -> writeFun(production.expression)
                    production.expression is RepeatedExpression -> writeFun(production.expression)
                    production.expression is SwitchExpression -> writeFun(production.expression)
                    production.expression is SequenceExpression -> writeFun(production.expression)
                    else -> throw UnsupportedOperationException("factory function for ${production.expression::class.simpleName}")
                }
            }

            private fun writeComment(indent: String) {
                val comment = with(production) { "`$counter` : $key:\n$expression" }
                write("\n" +
                    indent + "/**\n" +
                    indent + " * ${comment.replace("\n", "\n$indent * ")}\n" +
                    indent + " */\n")
            }

            private fun funName() = production.name +
                when { // consider only the first arg for extending the method name... good enough for YAML 1.2
                    onlyArgOrEmpty.startsWith("<") -> "≪" // '<' is an illegal character for a method name... much-less is not
                    onlyArgOrEmpty.startsWith("≤") -> "≤"
                    else -> ""
                }

            private val onlyArgOrEmpty: String get() = production.args.takeIf { it.size == 1 }?.get(0) ?: ""

            private fun writeArgs() = production.args.forEach {
                if (it != production.args[0])
                    write(", ")
                val pureVariableName = if (it.startsWith("<") || it.startsWith("≤")) it.substring(1) else it
                write(when (pureVariableName) {
                    "n" -> "n: Int"
                    "c" -> "c: InOutMode"
                    else -> "/* TODO arg $it */"
                })
            }

            private fun writeLessFun(comparison: Char) {
                val repeatedExpression = production.expression as RepeatedExpression
                require(repeatedExpression.repetitions == "m") { "unexpected repetitions '${repeatedExpression.repetitions}'" }
                require(repeatedExpression.comment == "Where m $comparison n") { "unexpected repeat comment '${repeatedExpression.comment}'" }
                write("" +
                    " = token(\"${production.name}($comparison\$n)\") { reader ->\n" +
                    "    val match = reader.mark { reader.readWhile { reader -> ")
                repeatedExpression.expression.guide(this)
                val negatedComparison = when (comparison) {
                    '<' -> ">="
                    '≤' -> ">"
                    else -> throw UnsupportedOperationException("unsupported comparison: '$comparison'")
                }
                write(".match(reader).codePoints } }\n" +
                    "    if (match.size $negatedComparison n) return@token Match(matches = false)\n" +
                    "    reader.read(match.size)\n" +
                    "    return@token Match(matches = true, codePoints = match)\n" +
                    "}\n")
            }

            private fun writeFun(referenceExpression: ReferenceExpression) = write(
                with(spec[referenceExpression.ref]) { " = `$name`$argsKey\n" })

            private fun writeFun(repeat: RepeatedExpression) {
                write(": Token {\n" +
                    "    val token = ")
                repeat.guide(this)
                write("\n" +
                    "    return token(\"${production.name}(${production.args.joinToString(", ") { argVar(it) }})\") { token.match(it) }\n" +
                    "}\n")
            }

            private fun writeFun(switchExpression: SwitchExpression) {
                require(switchExpression.balanced) { "unexpected balanced switch '$switchExpression'" }
                write(" = when (c) {\n")
                switchExpression.guide(this)
                write("}\n")
            }

            private fun writeFun(sequenceExpression: SequenceExpression) {
                write(" = ")
                sequenceExpression.guide(this)
            }

            /** the arg as Kotlin string template variable */
            private fun argVar(arg: String) = when {
                arg.startsWith("<") -> "<\$${arg.substring(1)}"
                else -> "\$$arg"
            }


            override fun visit(codePoint: CodePointExpression) {
                val quote = if (codePoint.codePoint.isBig) '\"' else '\''
                write("$quote${codePoint.codePoint.escaped}$quote")
            }

            override fun visit(reference: ReferenceExpression) = write(when (reference.ref) {
                "End of file" -> "EOF"
                "Empty" -> "empty"
                "Start of line" -> "startOfLine"
                else -> with(spec[reference.ref]) { "`$name`$argsKey" }
            })


            override fun beforeCollection(expression: ContainerExpression) {
                depth++
            }

            override fun afterCollection(expression: ContainerExpression) {
                depth--
            }

            override fun visit(sequence: SequenceExpression): Visitor {
                if (depth > 1) write("(")
                return this
            }

            override fun betweenSequenceItem(expression: Expression) = write(" + ")
            override fun leave(sequence: SequenceExpression) {
                if (depth > 1) write(")")
            }


            override fun visit(alternatives: AlternativesExpression) = this
            override fun betweenAlternativesItem(expression: Expression) = write(" or ")


            override fun visit(minus: MinusExpression) = this
            override fun beforeSubtrahend(expression: Expression) = write(" - ")


            override fun visit(repeated: RepeatedExpression) = this
            override fun leave(repeated: RepeatedExpression) = write(" * ${when (val repetitions = repeated.repetitions) {
                "?" -> "$zero_or_once"
                "*" -> "$zero_or_more"
                "+" -> "$once_or_more"
                else -> repetitions
            }}")


            override fun visit(range: RangeExpression) = this
            override fun betweenRange(rangeExpression: RangeExpression) = write("..")


            override fun visit(switch: SwitchExpression): Visitor {
                for (i in 0 until switch.cases.size) {
                    val case = switch.cases[i] as EqualsExpression
                    val left = case.left as Expression.VariableExpression
                    require(left.label == "c")
                    val right = case.right as ReferenceExpression
                    write("    `${right.ref}` -> ")
                    switch.expressions[i].guide(this)
                    write(" describedAs \"${production.name}(\$c)\"\n")
                }
                return IGNORE_VISITOR
            }
        }
    }
}
