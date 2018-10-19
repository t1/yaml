package spec.generator

import com.github.t1.yaml.tools.CodePoint

import java.io.IOException
import java.util.ArrayList

import java.util.stream.Collectors.joining

abstract class Expression {
    @Throws(IOException::class)
    abstract fun guide(visitor: Visitor)

    abstract class Visitor {
        ///////////// no subexpression
        open fun visit(nullExpression: NullExpression) {}

        @Throws(IOException::class)
        open fun visit(codePointExpression: CodePointExpression) {
        }

        @Throws(IOException::class)
        open fun visit(literalExpression: LiteralExpression) {
        }

        @Throws(IOException::class)
        open fun visit(referenceExpression: ReferenceExpression) {
        }


        ///////////// fixed number of subexpressions
        open fun visit(repeatedExpression: RepeatedExpression): Visitor {
            return this
        }

        open fun leave(repeatedExpression: RepeatedExpression) {}


        open fun visit(rangeExpression: RangeExpression): Visitor {
            return this
        }

        open fun leave(rangeExpression: RangeExpression) {}


        ///////////// arbitrary subexpressions
        open fun visit(minusExpression: MinusExpression): Visitor {
            return this
        }

        open fun leave(minusExpression: MinusExpression) {}


        @Throws(IOException::class)
        open fun visit(alternativesExpression: AlternativesExpression): Visitor {
            return this
        }

        @Throws(IOException::class)
        open fun leave(alternativesExpression: AlternativesExpression) {
        }


        open fun visit(sequenceExpression: SequenceExpression): Visitor {
            return this
        }

        open fun leave(sequenceExpression: SequenceExpression) {}


        open fun visit(switchExpression: SwitchExpression): Visitor {
            return this
        }

        open fun leave(switchExpression: SwitchExpression) {}
    }

    protected open fun last(): Expression {
        throw UnsupportedOperationException()
    }

    protected open fun replaceLastWith(expression: Expression) {
        throw UnsupportedOperationException()
    }

    open class NullExpression : Expression() {
        override fun toString(): String {
            return "<empty>"
        }

        override fun guide(visitor: Visitor) {
            visitor.visit(this)
        }
    }

    abstract class ContainerExpression : Expression() {
        val expressions: MutableList<Expression> = ArrayList()

        override fun last(): Expression {
            return lastOf(expressions).last()
        }

        override fun replaceLastWith(expression: Expression) {
            if (lastOf(expressions) is ContainerExpression)
                lastOf(expressions).replaceLastWith(expression)
            else
                setLastOf(expressions, expression)
        }

        open fun add(expression: Expression): ContainerExpression {
            expressions.add(expression)
            return this
        }

        open fun merge(expression: Expression): SwitchExpression? {
            if (expression is ContainerExpression)
                expressions.addAll(expression.expressions)
            else
                expressions.add(expression)
            return null
        }

        companion object {

            fun <T : ContainerExpression> of(left: Expression, right: Expression, type: Class<T>): T {
                val result: ContainerExpression
                try {
                    result = if (type.isInstance(left))
                        left as ContainerExpression
                    else
                        type.newInstance().add(left)
                } catch (e: InstantiationException) {
                    throw RuntimeException(e)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                }

                if (type.isInstance(right))
                    result.expressions.addAll((right as ContainerExpression).expressions)
                else
                    result.add(right)
                return type.cast(result)
            }
        }
    }

    open class AlternativesExpression : ContainerExpression() {

        override fun toString(): String {
            return expressions.stream().map { it.toString() }.collect(joining(" ||\n   ", "[", "]"))
        }

        @Throws(IOException::class)
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            expressions.forEach { expression ->
                try {
                    expression.guide(sub)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            sub.leave(this)
        }

        companion object {
            fun of(left: Expression, right: Expression): AlternativesExpression {
                return of(left, right, AlternativesExpression::class.java)
            }
        }
    }

    open class SequenceExpression : ContainerExpression() {

        override fun toString(): String {
            return if (expressions.isEmpty())
                "<empty sequence>"
            else
                expressions.stream().map { it.toString() }.collect(joining(" + "))
        }

        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            expressions.forEach { expression ->
                try {
                    expression.guide(sub)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            sub.leave(this)
        }

        companion object {
            fun of(left: Expression, right: Expression): SequenceExpression {
                return of(left, right, SequenceExpression::class.java)
            }
        }
    }

    open class CodePointExpression(val codePoint: CodePoint) : Expression() {

        override fun toString(): String {
            return "<" + codePoint.xinfo() + ">"
        }

        @Throws(IOException::class)
        override fun guide(visitor: Visitor) {
            visitor.visit(this)
        }
    }

    open class LiteralExpression(val literal: String) : Expression() {

        override fun toString(): String {
            return "<$literal>"
        }

        @Throws(IOException::class)
        override fun guide(visitor: Visitor) {
            visitor.visit(this)
        }
    }

    open class RangeExpression(private val left: Expression, private val right: Expression) : Expression() {

        override fun toString(): String {
            return "[$left-$right]"
        }

        @Throws(IOException::class)
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            left.guide(sub)
            right.guide(sub)
            sub.leave(this)
        }
    }

    open class ReferenceExpression(val ref: String) : Expression() {

        override fun toString(): String {
            return "->$ref"
        }

        @Throws(IOException::class)
        override fun guide(visitor: Visitor) {
            visitor.visit(this)
        }
    }

    open class MinusExpression(private val minuend: Expression) : Expression() {
        private val subtrahends = ArrayList<Expression>()

        operator fun minus(subtrahend: Expression): MinusExpression {
            if (subtrahend is MinusExpression) {
                subtrahends.add(subtrahend.minuend)
                subtrahends.addAll(subtrahend.subtrahends)
            } else {
                subtrahends.add(subtrahend)
            }
            return this
        }

        override fun toString(): String {
            return minuend.toString() + " - " +
                subtrahends.stream().map { it.toString() }.collect(joining(" - "))
        }

        @Throws(IOException::class)
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            minuend.guide(sub)
            subtrahends.forEach { subtrahend ->
                try {
                    subtrahend.guide(sub)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            sub.leave(this)
        }

        companion object {

            fun of(minuend: Expression, subtrahend: Expression): MinusExpression {
                return MinusExpression(minuend).minus(subtrahend)
            }
        }
    }

    open class RepeatedExpression(private val expression: Expression, private val repetitions: String) : Expression() {

        override fun toString(): String {
            return "($expression × $repetitions)"
        }

        @Throws(IOException::class)
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            expression.guide(sub)
            sub.leave(this)
        }
    }

    open class SwitchExpression : ContainerExpression() {
        private val cases: MutableList<Expression> = ArrayList()

        open fun balanced(): Boolean {
            return cases.size == expressions.size
        }

        open fun addCase(expression: Expression): SwitchExpression {
            if (balanced())
                cases.add(expression)
            else
                setLastOf(cases, expression)
            return this
        }

        override fun merge(expression: Expression): SwitchExpression {
            if (balanced()) {
                replaceLastWith(expression)
            } else {
                assert(cases.size == expressions.size + 1)
                expressions.add(expression)
            }
            return this
        }

        override fun toString(): String {
            val out = StringBuilder()
            for (i in cases.indices) {
                if (i > 0)
                    out.append("\n  ")
                out.append(cases[i]).append(" ⇒ ")
                    .append(if (i < expressions.size) expressions[i] else "?")
            }
            return out.toString()
        }

        @Throws(IOException::class)
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            for (i in cases.indices) {
                cases[i].guide(sub)
                if (expressions.size > i)
                    expressions[i].guide(sub)
            }
            sub.leave(this)
        }
    }

    companion object {

        private fun lastOf(expressions: List<Expression>): Expression {
            return expressions[expressions.size - 1]
        }

        private fun setLastOf(expressions: MutableList<Expression>, expression: Expression) {
            expressions[expressions.size - 1] = expression
        }
    }
}
