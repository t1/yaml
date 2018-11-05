package spec.generator

import com.github.t1.yaml.tools.CodePoint

import java.util.ArrayList

import java.util.stream.Collectors.joining
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class Expression {
    abstract fun guide(visitor: Visitor)

    abstract class Visitor {
        ///////////// no subexpression

        open fun visit(expression: NullExpression) {}
        open fun visit(codePoint: CodePointExpression) {}
        open fun visit(label: LabelExpression) {}
        open fun visit(reference: ReferenceExpression) {}


        ///////////// fixed number of subexpressions

        open fun visit(repeated: RepeatedExpression): Visitor = this
        open fun leave(repeated: RepeatedExpression) {}

        open fun visit(range: RangeExpression): Visitor = this
        open fun leave(range: RangeExpression) {}


        ///////////// arbitrary number of subexpressions

        open fun visit(minus: MinusExpression): Visitor = this
        open fun leave(minus: MinusExpression) {}

        open fun visit(alternatives: AlternativesExpression): Visitor = this
        open fun leave(alternatives: AlternativesExpression) {}

        open fun visit(sequence: SequenceExpression): Visitor = this
        open fun leave(sequence: SequenceExpression) {}

        open fun visit(switch: SwitchExpression): Visitor = this
        open fun leave(switch: SwitchExpression) {}
    }

    protected open fun last(): Expression = throw UnsupportedOperationException()

    protected open fun replaceLastWith(expression: Expression): Unit = throw UnsupportedOperationException()

    open class NullExpression : Expression() {
        override fun toString(): String = "<empty>"
        override fun guide(visitor: Visitor) = visitor.visit(this)
    }

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
                    result = if (type.java.isInstance(left)) // TODO broken in Kotlin 1.2.71
                        left as ContainerExpression
                    else
                        type.primaryConstructor!!.call().add(left)
                } catch (e: InstantiationException) {
                    throw RuntimeException(e)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                }

                if (type.java.isInstance(right)) // TODO broken in Kotlin 1.2.71
                    result.expressions.addAll((right as ContainerExpression).expressions)
                else result.add(right)
                return type.cast(result)
            }
        }
    }

    open class AlternativesExpression : ContainerExpression() {
        override fun toString(): String =
            expressions.stream().map { it.toString() }.collect(joining(" ||\n   ", "[", "]"))

        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            expressions.forEach { it.guide(sub) }
            visitor.leave(this)
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
            val sub = visitor.visit(this)
            expressions.forEach { it.guide(sub) }
            visitor.leave(this)
        }

        companion object {
            fun of(left: Expression, right: Expression): SequenceExpression = of(left, right, SequenceExpression::class)
        }
    }

    open class CodePointExpression(val codePoint: CodePoint) : Expression() {
        override fun toString(): String = "<${codePoint.info}>"
        override fun guide(visitor: Visitor) = visitor.visit(this)
    }

    open class LabelExpression(private val label: String) : Expression() {
        override fun toString(): String = "<$label>"
        override fun guide(visitor: Visitor) = visitor.visit(this)
    }

    open class RangeExpression(val left: Expression, val right: Expression) : Expression() {
        override fun toString(): String = "[$left-$right]"
        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            left.guide(sub)
            right.guide(sub)
            visitor.leave(this)
        }
    }

    open class ReferenceExpression(val ref: String) : Expression() {
        override fun toString(): String = "->$ref"
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

        override fun toString(): String {
            return minuend.toString() + " - " + subtrahends.stream().map { it.toString() }.collect(joining(" - "))
        }

        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            minuend.guide(sub)
            subtrahends.forEach { it.guide(sub) }
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

    open class SwitchExpression : ContainerExpression() {
        private val cases: MutableList<Expression> = ArrayList()

        open fun balanced(): Boolean = cases.size == expressions.size

        open fun addCase(expression: Expression): SwitchExpression {
            if (balanced()) cases.add(expression)
            else setLastOf(cases, expression)
            return this
        }

        override fun merge(expression: Expression): SwitchExpression {
            if (balanced()) replaceLastWith(expression)
            else {
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

        override fun guide(visitor: Visitor) {
            val sub = visitor.visit(this)
            for (i in cases.indices) {
                cases[i].guide(sub)
                if (expressions.size > i)
                    expressions[i].guide(sub)
            }
            visitor.leave(this)
        }
    }

    companion object {
        private fun lastOf(expressions: List<Expression>): Expression = expressions[expressions.size - 1]
        private fun setLastOf(expressions: MutableList<Expression>, expression: Expression) {
            expressions[expressions.size - 1] = expression
        }
    }
}

private fun <T : Any> KClass<T>.cast(result: Any): T = this.java.cast(result) // TODO maybe this will become part of KClass some day
