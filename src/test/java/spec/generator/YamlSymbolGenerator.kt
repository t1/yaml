package spec.generator

import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more
import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once
import org.assertj.core.api.Assertions.assertThat
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
import spec.generator.specparser.SpecLoader
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
            "@file:Suppress(\"unused\", \"ObjectPropertyName\", \"FunctionName\", \"NonAsciiCharacters\", \"REDUNDANT_ELSE_IN_WHEN\")\n" +
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
            "import com.github.t1.yaml.tools.Match\n" +
            "import com.github.t1.yaml.tools.Token\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.once_or_more\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_more\n" +
            "import com.github.t1.yaml.tools.Token.RepeatMode.zero_or_once\n" +
            "import com.github.t1.yaml.tools.empty\n" +
            "import com.github.t1.yaml.tools.endOfFile\n" +
            "import com.github.t1.yaml.tools.startOfLine\n" +
            "import com.github.t1.yaml.tools.symbol\n" +
            "import com.github.t1.yaml.tools.toCodePointRange\n" +
            "import com.github.t1.yaml.tools.token\n" +
            "import com.github.t1.yaml.tools.tokenGenerator\n" +
            "import com.github.t1.yaml.tools.undefined\n" +
            "import javax.annotation.Generated\n" +
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
            "private val anNsCharPreceding = undefined\n" +
            "private val atMost1024CharactersAltogether = undefined\n" +
            "private val excludingCForbiddenContent = undefined\n" +
            "private val followedByAnNsPlainSafe = undefined\n" +
            "\n"
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
        private val externalRefMap = mapOf(
            "End of file" to "endOfFile",
            "Empty" to "empty",
            "Start of line" to "startOfLine",
            "Followed by an ns-plain-safe(c)" to "followedByAnNsPlainSafe",
            "An ns-char preceding" to "anNsCharPreceding",
            "At most 1024 characters altogether" to "atMost1024CharactersAltogether",
            "flow-key" to "`flow-key`",
            "flow-out" to "`flow-out`",
            "flow-in" to "`flow-in`",
            "block-key" to "`block-key`",
            "block-out" to "`block-out`",
            "block-in" to "`block-in`",
            "m" to "m",
            "n" to "n",
            "n/a" to "-1",
            "n-1" to "n - 1",
            "n+1" to "n + 1",
            "auto-detect()" to "autoDetect",
            "strip" to "strip",
            "keep" to "keep",
            "clip" to "clip",
            "For some fixed auto-detected m > 0" to "forSomeFixedAutoDetectedMgt0",
            "Excluding c-forbidden content" to "excludingCForbiddenContent"
        )

        private fun write(string: String) {
            out.append(string)
        }

        fun write() {
            assertThat(spec.externalRefs - externalRefMap.keys).describedAs("unknown external references").isEmpty()

            write(HEADER)

            productionsSortedWithoutForwardReferences { production ->
                try {
                    ProductionWriter(production).write()
                } catch (e: Exception) {
                    throw RuntimeException("can't write production for ${production.counter}: ${production.key}", e)
                }
            }
        }

        private fun productionsSortedWithoutForwardReferences(body: (Production) -> Unit) {
            val remaining = productions.toMutableList()
            val written = mutableSetOf<Int>()
            val warned = mutableSetOf<Int>()
            fun firstWithoutForwardReferences(): Int {
                for ((index, production) in remaining.withIndex()) {
                    if (production.args.isNotEmpty()) return index // `fun` can reference forward, only `val` can not
                    val forwardRefs = production.references.values
                        .map { it.counter }
                        .filter { !written.contains(it) && production.counter != it }
                    if (forwardRefs.isEmpty()) return index
                    if (warned.add(production.counter)) write("\n// ${production.counter}: ${production.key} -> ${forwardRefs.sorted()}\n")
                }
                write("\n// remaining: ${remaining.map { it.counter }}\n")
                error("no production without forward references found")
            }

            while (!remaining.isEmpty()) {
                val index = firstWithoutForwardReferences()
                val production = remaining.removeAt(index)
                body(production)
                written.add(production.counter)
            }
        }

        private inner class ProductionWriter(private val production: Production) {
            private val visitor = ProductionVisitor()

            fun write() = if (production.hasArgs) writeTokenFactory() else writeToken()

            private fun writeToken() {
                write(comment())
                write("val `${production.name}` = token(\"${production.name}\", ")
                production.expression.guide(visitor)
                write(")\n")
            }

            private fun writeTokenFactory() {
                write(comment())
                val name = production.name.replace('<', '≪') // '<' is not allowed in method names
                write("fun `$name`(")
                writeArgs()
                write(")")
                if (isRecursive) write(" : Token")
                if (hasInternalFunRefs) write(" = tokenGenerator(\"$name\") { ")
                when {
                    production.counter in setOf(170, 174, 185) -> write("undefined /* TODO global variable */")
                    production.counter in setOf(162, 163, 164, 165, 166, 183, 187) -> write("undefined /* TODO other */")
                    name.endsWith("≪") || name.endsWith("≤") -> writeLessFun()
                    production.expression is ReferenceExpression -> visitor.writeFun(production.expression)
                    production.expression is RepeatedExpression -> {
                        if (!hasInternalFunRefs) write(" = ") // ugly
                        visitor.writeFun(production.expression)
                    }
                    production.expression is SequenceExpression -> {
                        if (!hasInternalFunRefs) write(" = ") // ugly
                        visitor.writeFun(production.expression)
                    }
                    production.expression is AlternativesExpression -> {
                        if (!hasInternalFunRefs) write(" = ") // ugly
                        visitor.writeFun(production.expression)
                    }
                    production.expression is SwitchExpression -> {
                        if (!hasInternalFunRefs) write(" = ") // ugly
                        visitor.writeFun(production.expression)
                    }
                    else -> throw UnsupportedOperationException("factory function for ${production.expression::class.simpleName}")
                }
                if (hasInternalFunRefs) write(" }")
                write("\n")
            }

            private val isRecursive get() = production.references.values.any { it == production }
            /** Productions that reference other local functions need to be protected from recursion */
            private val hasInternalFunRefs get() = production.references.keys.any { it.name !in spec.externalRefs && it.hasArgs }

            private fun comment(): String {
                return "\n" +
                    "/**\n" +
                    " * ${with(production) { "`$counter` : $key:\n$expression" }.replace("\n", "\n * ")}\n" +
                    " */\n"
            }

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

            private fun writeLessFun() {
                val comparison = production.name[production.name.length - 1]
                val repeatedExpression = production.expression as RepeatedExpression
                assertThat(repeatedExpression.repetitions).isEqualTo("m")
                assertThat(repeatedExpression.comment).isEqualTo("Where m $comparison n")
                write("" +
                    " = token(\"${production.key}\") { reader ->\n" +
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
                    "}")
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
                fun writeFun(reference: ReferenceExpression) = write(refCall(reference))

                override fun visit(reference: ReferenceExpression) = write(externalRefMap[reference.name] ?: refCall(reference))

                private fun refCall(ref: ReferenceExpression): String = "`${ref.name.replace('<', '≪')}`" +
                    if (ref.hasNoArgs) "" else ref.args.joinToString(", ", "(", ")") {
                        when (val value = it.second) {
                            is VariableExpression -> externalRefMap[value.name] ?: value.name
                            is ReferenceExpression -> "`${value.name}`${value.argsKey}"
                            else -> value.toString()
                        }
                    }


                // ------------------------------ repeated
                fun writeFun(repeat: RepeatedExpression) = repeat.guide(this)

                override fun visit(repeated: RepeatedExpression) = this
                override fun leave(repeated: RepeatedExpression) = write(" * ${when (val repetitions = repeated.repetitions) {
                    "?" -> "$zero_or_once"
                    "*" -> "$zero_or_more"
                    "+" -> "$once_or_more"
                    else -> repetitions
                }}")


                // ------------------------------ sequence
                fun writeFun(sequence: SequenceExpression) {
                    sequence.guide(this)
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
                    alternatives.guide(this)
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

            private inner class SwitchVisitor(switch: SwitchExpression) : ProductionVisitor() {
                val variableName: String = checkedVariableName(switch)

                init {
                    write("when ($variableName) {\n")
                }

                private fun checkedVariableName(switch: SwitchExpression): String {
                    assertThat(switch.balanced).isTrue()
                    val name = ((switch.cases[0] as EqualsExpression).left as VariableExpression).name
                    for (i in 0 until switch.cases.size) {
                        val case = switch.cases[i] as EqualsExpression
                        val left = case.left as VariableExpression
                        assertThat(left.name).isEqualTo(name)
                    }
                    return name
                }

                override fun visit(switch: SwitchExpression) = this

                override fun beforeSwitchItem() = write("    ")
                override fun betweenSwitchCaseAndValue() = write(" -> ")
                override fun afterSwitchItem(case: Expression, value: Expression) {
                    if (value is ReferenceExpression && externalRefMap.containsKey(value.name)) write("\n")
                    else write(" named \"${production.name}(\$$variableName)\"\n")
                }

                override fun visit(equals: EqualsExpression) = this
                override fun visit(variable: VariableExpression) {}

                override fun leave(switch: SwitchExpression) = write("" +
                    "    else -> error(\"unexpected `$variableName` value `$$variableName`\")\n" +
                    "}")
            }
        }
    }
}
