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
import spec.generator.Expression.VariableExpression
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

        val HEADER = "" +
            "@file:Generated(\"${YamlSymbolGenerator::class.qualifiedName}\")\n" +
            "@file:Suppress(\"unused\", \"ObjectPropertyName\", \"FunctionName\", \"NonAsciiCharacters\")\n" +
            "\n" +
            "package com.github.t1.yaml.parser\n" +
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
            "\n" +
            "import com.github.t1.yaml.parser.InOutMode.`block-in`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`block-key`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`block-out`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`flow-in`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`flow-key`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`flow-out`\n" +
            "import com.github.t1.yaml.tools.CodePoint\n" +
            "import com.github.t1.yaml.tools.CodePointRange\n" +
            "import com.github.t1.yaml.tools.CodePointReader\n" +
            "import com.github.t1.yaml.tools.Match\n" +
            "import com.github.t1.yaml.tools.Token\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once\n" +
            "import com.github.t1.yaml.tools.empty\n" +
            "import com.github.t1.yaml.tools.startOfLine\n" +
            "import com.github.t1.yaml.tools.symbol\n" +
            "import com.github.t1.yaml.tools.toCodePointRange\n" +
            "import com.github.t1.yaml.tools.token\n" +
            "import com.github.t1.yaml.tools.undefined\n" +
            "import javax.annotation.Generated\n" +
            "\n" +
            "private val EOF = symbol(CodePoint.EOF)\n" +
            "\n"

        const val FOOTER = "" +
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
            "private val followedByAnNsPlainSafe = undefined\n" +
            "private val anNsCharPreceding = undefined\n" +
            "private val atMost1024CharactersAltogether = undefined\n"
    }

    private fun generateCode() {
        Files.newBufferedWriter(SOURCE_FILE).use { writer -> generateCode(writer) }
    }

    fun generateCode(out: Writer) {
        SourceCodeGenerator(spec, out).write()
    }

    class SourceCodeGenerator(
        private val spec: Spec,
        private val out: Writer
    ) {
        private val productions = spec.productions

        private fun write(string: String) {
            out.append(string)
        }

        fun write() {
            write(HEADER)

            for (production in productions.filter { production -> production.args.isEmpty() }) {
                try {
                    ProductionWriter(production).writeToken()
                } catch (e: Exception) {
                    throw RuntimeException("can't write enum entry for ${production.counter}: ${production.key}", e)
                }
            }

            for (production in productions.filter { production -> !production.args.isEmpty() }) {
                try {
                    ProductionWriter(production).writeTokenFactory()
                } catch (e: Exception) {
                    throw RuntimeException("can't write factory method for ${production.counter}: ${production.key}", e)
                }
            }

            write(FOOTER)
        }

        private inner class ProductionWriter(val production: Production) {
            private val visitor = ProductionVisitor()

            fun writeToken() {
                write(comment())
                write("val `${production.name}` = token(\"${production.name}\", ")
                when (production.counter) {
                    in setOf(82, 83, 86, 88, 89, 93, 97, 101) -> write("undefined /* TODO forward reference */")
                    in setOf(193, 207) -> write("undefined /* TODO global variable */")
                    else -> production.expression.guide(visitor)
                }
                write(")\n")
            }

            fun writeTokenFactory() {
                write(comment())
                write("fun `${funName()}`(")
                writeArgs()
                write(")")
                when {
                    production.counter in setOf(74, 112, 154, 155, 168, 170, 171, 174, 176, 178, 180, 184, 190, 191, 194, 197) ->
                        write("= undefined /* TODO global variable */\n")
                    production.counter in setOf(115, 124, 138, 141, 148, 153, 158, 186) -> write("= undefined /* TODO recursion */\n")
                    production.counter in setOf(136, 162, 163, 164, 165, 166, 183, 187, 201) -> write("= undefined /* TODO other */\n")
                    onlyArgOrEmpty.startsWith("<") || onlyArgOrEmpty.startsWith("≤") -> writeLessFun(onlyArgOrEmpty[0])
                    production.expression is ReferenceExpression -> visitor.writeFun(production.expression)
                    production.expression is RepeatedExpression -> visitor.writeFun(production.expression)
                    production.expression is SwitchExpression -> visitor.writeFun(production.expression)
                    production.expression is SequenceExpression -> visitor.writeFun(production.expression)
                    production.expression is AlternativesExpression -> visitor.writeFun(production.expression)
                    else -> throw UnsupportedOperationException("factory function for ${production.expression::class.simpleName}")
                }
            }

            private fun comment(): String {
                return "\n" +
                    "/**\n" +
                    " * ${with(production) { "`$counter` : $key:\n$expression" }.replace("\n", "\n * ")}\n" +
                    " */\n"
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
                    "m" -> "m: Int"
                    "t" -> "t: String"
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
                repeatedExpression.expression.guide(visitor)
                val negatedComparison = when (comparison) {
                    '<' -> ">="
                    '≤' -> ">"
                    else -> throw UnsupportedOperationException("unsupported comparison: '$comparison'")
                }
                write(".match(reader).codePoints } }\n" +
                    "    if (match.size $negatedComparison n) return@token Match(matches = false)\n" +
                    "    reader.expect(match)\n" +
                    "    return@token Match(matches = true, codePoints = match)\n" +
                    "}\n")
            }

            private open inner class ProductionVisitor : OrFailVisitor() {
                private var depth = 0

                override fun beforeCollection(expression: ContainerExpression) {
                    depth++
                }

                override fun afterCollection(expression: ContainerExpression) {
                    depth--
                }


                // ------------------------------ reference
                fun writeFun(reference: ReferenceExpression) = write(" = ${refCall(reference)}\n")

                override fun visit(reference: ReferenceExpression) = write(when (reference.ref) {
                    "End of file" -> "EOF"
                    "Empty" -> "empty"
                    "Start of line" -> "startOfLine"
                    "Followed by an ns-plain-safe(c)" -> "followedByAnNsPlainSafe"
                    "Followed by an ns-plain-safe(c) )" -> "followedByAnNsPlainSafe" // BUG-IN-SPEC
                    "An ns-char preceding" -> "anNsCharPreceding"
                    "At most 1024 characters altogether" -> "atMost1024CharactersAltogether"
                    "flow-key" -> "`flow-key`"
                    "flow-out" -> "`flow-out`"
                    "flow-in" -> "`flow-in`"
                    "block-key" -> "`block-key`"
                    "block-out" -> "`block-out`"
                    "block-in" -> "`block-in`"
                    else -> refCall(reference)
                })

                private fun refCall(reference: ReferenceExpression): String =
                    with(spec[reference.ref]) { "`$name`${argsKey.replace(",", ", ")}" }


                // ------------------------------ repeated
                fun writeFun(repeat: RepeatedExpression) {
                    write(": Token {\n" +
                        "    val token = ")
                    repeat.guide(this)
                    write("\n")
                    with(production) {
                        /** the arg as Kotlin string template variable */
                        fun argVar(arg: String) = when {
                            arg.startsWith("<") -> "<\$${arg.substring(1)}"
                            else -> "\$$arg"
                        }

                        write("    return token(\"$name(${args.joinToString(", ", transform = ::argVar)})\") { token.match(it) }\n")
                    }
                    write("}\n")
                }

                override fun visit(repeated: RepeatedExpression) = this
                override fun leave(repeated: RepeatedExpression) = write(" * ${when (val repetitions = repeated.repetitions) {
                    "?" -> "$zero_or_once"
                    "*" -> "$zero_or_more"
                    "+" -> "$once_or_more"
                    else -> repetitions
                }}")


                // ------------------------------ sequence
                fun writeFun(sequence: SequenceExpression) {
                    write(" = ")
                    sequence.guide(this)
                    write("\n")
                }

                override fun visit(sequence: SequenceExpression): Visitor {
                    if (depth > 1) write("(")
                    return this
                }

                override fun betweenSequenceItems() = write(" + ")
                override fun leave(sequence: SequenceExpression) {
                    if (depth > 1) write(")")
                }


                // ------------------------------ alternatives
                fun writeFun(alternatives: AlternativesExpression) {
                    write(" = ")
                    alternatives.guide(this)
                    write("\n")
                }

                override fun visit(alternatives: AlternativesExpression) = this
                override fun betweenAlternativesItems() = write(" or ")


                // ------------------------------ other
                override fun visit(codePoint: CodePointExpression) {
                    val quote = if (codePoint.codePoint.isSupplementary) '\"' else '\''
                    write("$quote${codePoint.codePoint.escaped}$quote")
                }


                override fun visit(minus: MinusExpression) = this
                override fun beforeSubtrahend(expression: Expression) = write(" - ")


                override fun visit(range: RangeExpression) = this
                override fun betweenRange(rangeExpression: RangeExpression) = write("..")


                fun writeFun(switch: SwitchExpression) = switch.guide(SwitchVisitor(switch))
            }

            private inner class SwitchVisitor(val switch: SwitchExpression) : ProductionVisitor() {
                val variableName: String

                init {
                    require(switch.balanced) { "unexpected balanced switch '$switch'" }
                    variableName = ((switch.cases[0] as EqualsExpression).left as VariableExpression).name
                    for (i in 0 until switch.cases.size) {
                        val case = switch.cases[i] as EqualsExpression
                        val left = case.left as VariableExpression
                        require(left.name == variableName)
                    }
                    write(" = when ($variableName) {\n")
                }

                override fun visit(switch: SwitchExpression) = this

                override fun beforeSwitchItem() = write("    ")
                override fun betweenSwitchCaseAndValue() = write(" -> ")
                override fun afterSwitchItem() = write(" named \"${production.name}(\$$variableName)\"\n")

                override fun visit(equals: EqualsExpression) = this
                override fun visit(variable: VariableExpression) {}

                override fun leave(switch: SwitchExpression) = write("" +
                    "    else -> error(\"unexpected `$variableName` value `$$variableName`\")\n" +
                    "}\n")
            }
        }
    }
}
