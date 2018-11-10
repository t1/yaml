package spec.generator

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

/** The actual visitor interface used by [Expression.guide]. Implement [OrFailVisitor] or [OrIgnoreVisitor] instead. */
interface Visitor {
    ///////////// no subexpression

    fun visit(codePoint: CodePointExpression)
    fun visit(variable: VariableExpression)
    fun visit(reference: ReferenceExpression)

    fun beforeCollection(expression: ContainerExpression) {}
    fun afterCollection(expression: ContainerExpression) {}


    ///////////// fixed number of subexpressions

    fun visit(repeated: RepeatedExpression): Visitor
    fun leave(repeated: RepeatedExpression) {}

    fun visit(range: RangeExpression): Visitor
    fun betweenRange(rangeExpression: RangeExpression) {}
    fun leave(range: RangeExpression) {}

    fun visit(equals: EqualsExpression): Visitor
    fun leave(equals: EqualsExpression) {}


    ///////////// arbitrary number of subexpressions

    fun visit(minus: MinusExpression): Visitor
    fun beforeMinuend(expression: Expression) {}
    fun afterMinuend(expression: Expression) {}
    fun beforeSubtrahend(expression: Expression) {}
    fun afterSubtrahend(expression: Expression) {}
    fun leave(minus: MinusExpression) {}

    fun visit(alternatives: AlternativesExpression): Visitor
    fun beforeAlternativesItem(expression: Expression) {}
    fun afterAlternativesItem(expression: Expression) {}
    fun betweenAlternativesItems() {}
    fun leave(alternatives: AlternativesExpression) {}

    fun visit(sequence: SequenceExpression): Visitor
    fun beforeSequenceItem(expression: Expression) {}
    fun afterSequenceItem(expression: Expression) {}
    fun betweenSequenceItems() {}
    fun leave(sequence: SequenceExpression) {}

    fun visit(switch: SwitchExpression): Visitor
    fun beforeSwitchItem() {}
    fun betweenSwitchCaseAndValue() {}
    fun afterSwitchItem() {}
    fun leave(switch: SwitchExpression) {}
}

/**
 * A [Visitor] that fails when visiting an unexpected [Expression], i.e. when it's not overloaded.
 * Leaving and before/after can safely be ignored
 */
abstract class OrFailVisitor : Visitor {
    override fun visit(codePoint: CodePointExpression): Unit = missing("CodePointExpression")
    override fun visit(variable: VariableExpression): Unit = missing("VariableExpression")
    override fun visit(reference: ReferenceExpression): Unit = missing("ReferenceExpression")
    override fun visit(repeated: RepeatedExpression): Visitor = missing("RepeatedExpression")
    override fun visit(range: RangeExpression): Visitor = missing("RangeExpression")
    override fun visit(equals: EqualsExpression): Visitor = missing("EqualsExpression")
    override fun visit(minus: MinusExpression): Visitor = missing("MinusExpression")
    override fun visit(alternatives: AlternativesExpression): Visitor = missing("AlternativesExpression")
    override fun visit(sequence: SequenceExpression): Visitor = missing("SequenceExpression")
    override fun visit(switch: SwitchExpression): Visitor = missing("SwitchExpression")

    private fun missing(type: String): Nothing = error("no visit($type) in ${this::class}")
}

/**
 * A [Visitor] that ignores all [Expression]s by default. Normally use [OrFailVisitor], or you may miss something!
 */
open class OrIgnoreVisitor : Visitor {
    override fun visit(codePoint: CodePointExpression) {}
    override fun visit(variable: VariableExpression) {}
    override fun visit(reference: ReferenceExpression) {}
    override fun visit(repeated: RepeatedExpression): Visitor = this
    override fun visit(range: RangeExpression): Visitor = this
    override fun visit(equals: EqualsExpression): Visitor = this
    override fun visit(minus: MinusExpression): Visitor = this
    override fun visit(alternatives: AlternativesExpression): Visitor = this
    override fun visit(sequence: SequenceExpression): Visitor = this
    override fun visit(switch: SwitchExpression): Visitor = this
}

val IGNORE_VISITOR = OrIgnoreVisitor()
