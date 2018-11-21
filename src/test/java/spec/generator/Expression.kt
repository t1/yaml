package spec.generator

import com.github.t1.yaml.tools.CodePoint

import java.util.ArrayList

import java.util.stream.Collectors.joining
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.primaryConstructor

abstract class Expression {
    abstract fun guide(visitor: Visitor)

    protected open fun last(): Expression = throw UnsupportedOperationException()

    protected open fun replaceLastWith(expression: Expression): Unit = throw UnsupportedOperationException()

    abstract class ContainerExpression : Expression() {
        val expressions: MutableList<Expression> = ArrayList()

        override fun last(): Expression = lastOf(expressions).last()
        override fun replaceLastWith(expression: Expression) {
            if (lastOf(expressions) is ContainerExpression) lastOf(expressions).replaceLastWith(expression)
            else setLastOf(expressions, expression)
        }

        open fun add(expression: Expression): ContainerExpression {
            expressions.add(expression)
            return this
        }

        open fun merge(expression: Expression): SwitchExpression? {
            if (expression is ContainerExpression) expressions.addAll(expression.expressions)
            else expressions.add(expression)
            return null
        }

        companion object {
            fun <T : ContainerExpression> of(left: Expression, right: Expression, type: KClass<T>): T {
                val result: ContainerExpression
                try {
                    result = if (type.isInstance(left))
                        left as ContainerExpression
                    else
                        type.primaryConstructor!!.call().add(left)
                } catch (e: InstantiationException) {
                    throw RuntimeException(e)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                }

                if (type.isInstance(right))
                    result.expressions.addAll((right as ContainerExpression).expressions)
                else result.add(right)
                return type.cast(result)
            }
        }
    }

    open class AlternativesExpression : ContainerExpression() {
        override fun toString(): String =
            expressions.stream().map { it.toString() }.collect(joining(" |\n   ", "[", "]"))

        override fun guide(visitor: Visitor) {
            visitor.beforeCollection(this)
            val sub = visitor.visit(this)
            var first = true
            expressions.forEach {
                if (first) first = false else sub.betweenAlternativesItems()
                sub.beforeAlternativesItem(it)
                it.guide(sub)
                sub.afterAlternativesItem(it)
            }
            visitor.leave(this)
            visitor.afterCollection(this)
        }

        companion object {
            fun of(left: Expression, right: Expression): AlternativesExpression = of(left, right, AlternativesExpression::class)
        }
    }

    open class SequenceExpression : ContainerExpression() {
        override fun toString() =
            if (expressions.isEmpty()) "<empty sequence>"
            else expressions.stream().map { it.toString() }.collect(joining(" + "))!!

        override fun guide(visitor: Visitor) {
            visitor.beforeCollection(this)
            val sub = visitor.visit(this)
            var first = true
            expressions.forEach {
                if (first) first = false else sub.betweenSequenceItems()
                sub.beforeSequenceItem(it)
                it.guide(sub)
                sub.afterSequenceItem(it)
            }
            visitor.leave(this)
            visitor.afterCollection(this)
        }

        companion object {
            fun of(left: Expression, right: Expression): SequenceExpression = of(left, right, SequenceExpression::class)
        }
    }

    open class CodePointExpression(val codePoint: CodePoint) : Expression() {
        override fun toString(): String = "<${codePoint.info}>"
        override fun guide(visitor: Visitor) = visitor.visit(this)
    }

    open class VariableExpression(val name: String) : Expression() {
        override fun toString(): String = "<$name>"
        override fun guide(visitor: Visitor) = visitor.visit(this)
    }

    open class RangeExpression(val left: Expression, val right: Expression) : Expression() {
        override fun toString(): String = "[$left-$right]"
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            left.guide(sub)
            sub.betweenRange(this)
            right.guide(sub)
            visitor.leave(this)
        }
    }

    open class ReferenceExpression(val name: String, val args: List<Pair<String, Expression>> = listOf()) : Expression() {
        override fun toString() = "->$name" + inParentheses {
            if (with(it.second) { this is VariableExpression && this.name == it.first }) it.first else "${it.first} = ${it.second}"
        }

        val key get() = name + argsKey

        val argsKey get() = inParentheses { it.first }

        private fun inParentheses(transform: (Pair<String, Expression>) -> String) =
            if (args.isEmpty()) "" else args.joinToString(",", "(", ")", transform = transform)

        override fun guide(visitor: Visitor) = visitor.visit(this)
    }

    open class MinusExpression(val minuend: Expression) : Expression() {
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

        override fun toString() = "($minuend - ${subtrahends.joinToString(" - ")})"

        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            sub.beforeMinuend(minuend)
            minuend.guide(sub)
            sub.afterMinuend(minuend)
            subtrahends.forEach {
                sub.beforeSubtrahend(it)
                it.guide(sub)
                sub.afterSubtrahend(it)
            }
            visitor.leave(this)
        }

        companion object {
            fun of(minuend: Expression, subtrahend: Expression): MinusExpression =
                MinusExpression(minuend).minus(subtrahend)
        }
    }

    open class RepeatedExpression(val expression: Expression, val repetitions: String, val comment: String? = null) : Expression() {
        override fun toString(): String = "($expression × $repetitions${if (comment == null) "" else " /* $comment */"})"
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            expression.guide(sub)
            visitor.leave(this)
        }
    }

    open class EqualsExpression(val left: Expression, val right: Expression) : Expression() {
        override fun toString(): String = "$left = $right"
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            left.guide(sub)
            right.guide(sub)
            visitor.leave(this)
        }
    }

    open class SwitchExpression : ContainerExpression() {
        val cases: MutableList<Expression> = ArrayList()

        val balanced: Boolean get() = cases.size == expressions.size

        override fun add(expression: Expression): SwitchExpression =
            if (balanced) addCase(expression)
            else merge(expression)

        fun addCase(expression: Expression): SwitchExpression {
            if (balanced) cases.add(expression)
            else setLastOf(cases, expression)
            return this
        }

        override fun merge(expression: Expression): SwitchExpression {
            if (balanced) replaceLastWith(expression)
            else {
                assert(cases.size == expressions.size + 1)
                expressions.add(expression)
            }
            return this
        }

        override fun toString(): String {
            val out = StringBuilder()
            for (i in cases.indices) {
                if (i > 0) out.append("\n")
                out.append("${cases[i]} ⇒ ${expressionOrNull(i) ?: "?"}")
            }
            return out.toString()
        }

        private fun expressionOrNull(i: Int): Expression? = if (i < expressions.size) expressions[i] else null

        override fun guide(visitor: Visitor) {
            visitor.beforeCollection(this)
            val sub = visitor.visit(this)
            for (i in cases.indices) {
                sub.beforeSwitchItem()
                cases[i].guide(sub)
                sub.betweenSwitchCaseAndValue()
                if (expressions.size > i) {
                    expressions[i].guide(sub)
                    sub.afterSwitchItem(cases[i], expressions[i])
                }
            }
            visitor.leave(this)
            visitor.afterCollection(this)
        }

        companion object {
            fun of(left: Expression, right: Expression): SwitchExpression = of(left, right, SwitchExpression::class)
        }
    }

    companion object {
        private fun lastOf(expressions: List<Expression>): Expression = expressions[expressions.size - 1]
        private fun setLastOf(expressions: MutableList<Expression>, expression: Expression) {
            expressions[expressions.size - 1] = expression
        }
    }
}
