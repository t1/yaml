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

class YamlTokenGenerator(private val spec: Spec) {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val spec = SpecLoader().load()
            YamlTokenGenerator(spec).generateCode()
        }

        private val SOURCE_FILE = Paths.get("src/main/java/com/github/t1/yaml/parser/YamlTokens.kt")

        val HEADER = "" +
            "@file:Generated(\"${YamlTokenGenerator::class.qualifiedName}\")\n" +
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
            "import com.github.t1.yaml.parser.ChompMode.clip\n" +
            "import com.github.t1.yaml.parser.ChompMode.keep\n" +
            "import com.github.t1.yaml.parser.ChompMode.strip\n" +
            "import com.github.t1.yaml.parser.InOutMode.`block-in`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`block-key`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`block-out`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`flow-in`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`flow-key`\n" +
            "import com.github.t1.yaml.parser.InOutMode.`flow-out`\n" +
            "import com.github.t1.yaml.parser.ScalarParser.Companion.autoDetectIndentation\n" +
            "import com.github.t1.yaml.tools.CodePoint\n" +
            "import com.github.t1.yaml.tools.CodePointReader\n" +
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
            "\n" +
            "private fun CodePointReader.accept(char: Char): Boolean = accept(symbol(char))\n" +
            "private fun CodePointReader.accept(token: Token): Boolean {\n" +
            "    val (matches, codePoints) = token.match(this)\n" +
            "    acceptedCodePoints = codePoints\n" +
            "    return matches\n" +
            "}\n" +
            "\n" +
            "private var acceptedCodePoints: List<CodePoint>? = null\n" +
            "private val acceptedCodePoint: CodePoint\n" +
            "    get() = with(acceptedCodePoints) {\n" +
            "        require(this != null) { \"require a successful call to `CodePointReader.accept(token: Token)`\" }\n" +
            "        require(this.size == 1) { \"require acceptedCodePoints to match one CodePoint but found \$this\" }\n" +
            "        return this[0]\n" +
            "    }\n" +
            "\n" +
            "private val anNsCharPreceding = undefined\n" +
            "private val atMost1024CharactersAltogether = undefined\n" +
            "private val excludingCForbiddenContent = undefined\n" +
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
            "Followed by an ns-plain-safe(c)" to "/** TODO Followed by an */ `ns-plain-safe`(c)",
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
            "auto-detect()" to "autoDetectIndentation(reader)",
            "strip" to "strip",
            "keep" to "keep",
            "clip" to "clip",
            "For some fixed auto-detected m > 0" to "forSomeFixedAutoDetectedMgt0",
            "Excluding c-forbidden content" to "excludingCForbiddenContent"
        )

        private val specials = mapOf(
            162 to "" +
                "fun `c-b-block-header`(reader: CodePointReader): Pair<Int, ChompMode> {\n" +
                "    var t = `c-chomping-indicator`(reader)\n" +
                "    val m = `c-indentation-indicator`(reader)\n" +
                "    if (t == clip) t = `c-chomping-indicator`(reader)\n" +
                "    // TODO `s-b-comment`\n" +
                "    return m to t\n" +
                "}\n"
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
            private val isSpecial = production.counter in specials.keys
            private val visitor = ProductionVisitor()
            private val isRecursive = production.references.values.any { it == production }
            /** Productions that reference other local functions need to be protected from recursion */
            private val hasInternalFunRefs = production.references.keys.any { it.name !in spec.externalRefs && it.hasArgs }
            private val hasOutArg = with(production.expression) { this is SwitchExpression && this.values[0] is EqualsExpression }

            fun write() {
                writeComment()
                when {
                    isSpecial -> writeSpecial()
                    production.hasArgs -> writeTokenFactory()
                    else -> writeToken()
                }
            }

            private fun writeSpecial() = write(specials[production.counter]!!)

            private fun writeComment() = write("\n" +
                "/**\n" +
                " * ${with(production) { "`$counter` : $key:\n$expression" }.replace("\n", "\n * ")}\n" +
                " */\n")

            private fun writeToken() {
                write("val `${production.name}` = token(\"${production.name}\", ")
                production.expression.guide(visitor)
                write(")\n")
            }

            private fun writeTokenFactory() {
                val name = production.name.replace('<', '≪') // '<' is not allowed in method names
                write("fun `$name`(")
                writeArgs()
                write(")")
                writeReturnType()
                if (hasInternalFunRefs) write(" = tokenGenerator(\"$name\") { ") else write(" = ")
                when {
                    production.counter in setOf(170, 174, 183, 185, 187) -> write("undefined /* TODO not yet supported */")
                    name.endsWith("≪") || name.endsWith("≤") -> writeLessFun()
                    production.expression is ReferenceExpression -> visitor.writeFun(production.expression)
                    production.expression is RepeatedExpression -> visitor.writeFun(production.expression)
                    production.expression is SequenceExpression -> visitor.writeFun(production.expression)
                    production.expression is AlternativesExpression -> visitor.writeFun(production.expression)
                    production.expression is SwitchExpression -> visitor.writeFun(production.expression, indented = hasInternalFunRefs)
                    else -> error("factory function for ${production.expression::class.simpleName}")
                }
                if (hasInternalFunRefs) write(" }")
                write("\n")
            }

            private fun writeArgs() {
                if (hasOutArg) write("reader: CodePointReader")
                else production.args.forEach {
                    if (it != production.args.first())
                        write(", ")
                    write("$it: ${argType(it)}")
                }
            }

            private fun writeReturnType() {
                if (hasOutArg) {
                    write(": ${argType(production.args[0])}")
                } else if (isRecursive) write(": Token")
            }

            private fun argType(argName: String): String = when (argName) {
                "n" -> "Int"
                "m" -> "Int"
                "t" -> "ChompMode"
                "c" -> "InOutMode"
                else -> error("unsupported variable name '$argName' (i.e. unknown type)")
            }

            private fun writeLessFun() {
                val comparison = production.name[production.name.length - 1]
                val repeatedExpression = production.expression as RepeatedExpression
                assertThat(repeatedExpression.repetitions).isEqualTo("m")
                assertThat(repeatedExpression.comment).isEqualTo("Where m $comparison n")
                write("" +
                    "token(\"${production.key}\") { reader ->\n" +
                    "    val match = reader.mark { reader.readWhile { reader -> ")
                repeatedExpression.expression.guide(visitor)
                val negatedComparison = when (comparison) {
                    '<' -> ">="
                    '≤' -> ">"
                    else -> error("unsupported comparison: '$comparison'")
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

                override fun visit(repeated: RepeatedExpression): Visitor = this
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

                override fun visit(alternatives: AlternativesExpression): Visitor = this
                override fun betweenAlternativesItems() = write(" or ")


                // ------------------------------ other
                override fun visit(codePoint: CodePointExpression) {
                    val quote = if (codePoint.codePoint.isSupplementary) '\"' else '\''
                    write("$quote${codePoint.codePoint.escaped}$quote")
                }


                override fun visit(minus: MinusExpression): Visitor = this
                override fun beforeSubtrahend(expression: Expression) = write(" - ")


                override fun visit(range: RangeExpression): Visitor = this
                override fun betweenRange(rangeExpression: RangeExpression) = write("..")


                fun writeFun(switch: SwitchExpression, indented: Boolean) = switch.guide(SwitchVisitor(switch, indented))
            }

            private inner class SwitchVisitor(
                private val switch: SwitchExpression,
                private val indented: Boolean
            ) : ProductionVisitor() {
                private val variableName: String = checkedVariableName(switch)
                private val indent = if (indented) "    " else ""
                private var hasElse = false

                init {
                    if (indented) write("\n    ")
                    write("when ${if (hasOutArg) "" else "($variableName) "}{\n")
                }

                private fun checkedVariableName(switch: SwitchExpression): String {
                    assertThat(switch.balanced).isTrue()
                    val equalsExpressions = if (hasOutArg) switch.values else switch.cases
                    val name = ((equalsExpressions[0] as EqualsExpression).left as VariableExpression).name
                    for (i in 0 until equalsExpressions.size) {
                        val case = equalsExpressions[i] as EqualsExpression
                        val left = case.left as VariableExpression
                        assertThat(left.name).isEqualTo(name)
                    }
                    return name
                }

                override fun visit(switch: SwitchExpression) = this

                override fun beforeSwitchItem() = write("    $indent")

                override fun visit(codePoint: CodePointExpression) = aroundCase(codePoint) { super.visit(codePoint) }

                override fun visit(reference: ReferenceExpression) = aroundCase(reference) {
                    if (elseCase(reference)) {
                        hasElse = true
                        write("else")
                    } else super.visit(reference)
                }

                private fun aroundCase(expression: Expression, block: () -> Unit) {
                    val surroundWithReaderAccept = hasOutArg && expression in switch.cases && !elseCase(expression)
                    if (surroundWithReaderAccept) write("reader.accept(")
                    block()
                    if (surroundWithReaderAccept) write(")")
                }

                private fun elseCase(expression: Expression) =
                    expression == switch.cases.last() && expression is ReferenceExpression && expression.key == "Empty"

                override fun betweenSwitchCaseAndValue() = write(" -> ")

                override fun visit(equals: EqualsExpression) = this
                override fun visit(variable: VariableExpression) {}
                override fun visit(minus: MinusExpression) =
                    if (matchedCaseMinusConstant(minus)) {
                        val codePoint = (minus.subtrahends[0] as CodePointExpression).codePoint
                        write("acceptedCodePoint.toInt() - 0x${codePoint.HEX}")
                        IGNORE_VISITOR
                    } else super.visit(minus)

                private fun matchedCaseMinusConstant(minus: MinusExpression) =
                    hasOutArg && minus.minuend is ReferenceExpression && minus.subtrahends.size == 1 && minus.subtrahends[0] is CodePointExpression

                override fun afterSwitchItem(case: Expression, value: Expression) {
                    if (hasOutArg || value is ReferenceExpression && externalRefMap.containsKey(value.name)) write("\n")
                    else write(" named \"${production.name}(\$$variableName)\"\n")
                }

                override fun leave(switch: SwitchExpression) {
                    if (!hasElse) write("$indent    else -> error(\"unexpected `$variableName` value `$$variableName`\")\n")
                    write("$indent}${if (indented) "\n" else ""}")
                }
            }
        }
    }
}
