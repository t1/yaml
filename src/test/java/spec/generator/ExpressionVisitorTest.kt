package spec.generator

import com.github.t1.yaml.tools.CodePoint
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.LabelExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.NullExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression
import spec.generator.Expression.Visitor

class ExpressionVisitorTest {
    private val visitor: Visitor = mock(Visitor::class.java)

    @AfterEach fun tearDown() {
        verifyNoMoreInteractions(visitor)
    }


    @Test fun visitNull() {
        val expression = NullExpression()

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test fun visitCodePoint() {
        val expression = CodePointExpression(CodePoint.of("x"))

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test fun visitLabel() {
        val expression = LabelExpression("foo")

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test fun visitReference() {
        val expression = ReferenceExpression("foo")

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test fun visitRepeated() {
        val label = LabelExpression("foo")
        val expression = RepeatedExpression(label, "4")
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(label)
        inOrder.verify(visitor).leave(expression)
    }

    @Test fun visitRepeatedWithSub() {
        val label = LabelExpression("foo")
        val expression = RepeatedExpression(label, "4")
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(label)
        inOrder.verify(visitor).leave(expression)
    }


    @Test fun visitRange() {
        val label1 = LabelExpression("foo")
        val label2 = LabelExpression("bar")
        val expression = RangeExpression(label1, label2)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(label1)
        inOrder.verify(visitor).visit(label2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test fun visitRangeWithSub() {
        val label1 = LabelExpression("foo")
        val label2 = LabelExpression("bar")
        val expression = RangeExpression(label1, label2)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(label1)
        inOrder.verify(sub).visit(label2)
        inOrder.verify(visitor).leave(expression)
    }


    @Test fun visitMinus() {
        val label1 = LabelExpression("foo")
        val label2 = LabelExpression("bar")
        val label3 = LabelExpression("baz")
        val expression = MinusExpression.of(label1, label2).minus(label3)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(label1)
        inOrder.verify(visitor).visit(label2)
        inOrder.verify(visitor).visit(label3)
        inOrder.verify(visitor).leave(expression)
    }

    @Test fun visitMinusWithSub() {
        val label1 = LabelExpression("foo")
        val label2 = LabelExpression("bar")
        val label3 = LabelExpression("baz")
        val expression = MinusExpression.of(label1, label2).minus(label3)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(label1)
        inOrder.verify(sub).visit(label2)
        inOrder.verify(sub).visit(label3)
        inOrder.verify(visitor).leave(expression)
    }


    @Test fun visitAlternatives() {
        val label1 = LabelExpression("foo")
        val label2 = LabelExpression("bar")
        val expression = AlternativesExpression.of(label1, label2)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(label1)
        inOrder.verify(visitor).visit(label2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test fun visitAlternativesWithSub() {
        val label1 = LabelExpression("foo")
        val label2 = LabelExpression("bar")
        val expression = AlternativesExpression.of(label1, label2)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(label1)
        inOrder.verify(sub).visit(label2)
        inOrder.verify(visitor).leave(expression)
    }


    @Test fun visitSequence() {
        val label1 = LabelExpression("foo")
        val label2 = LabelExpression("bar")
        val expression = SequenceExpression.of(label1, label2)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(label1)
        inOrder.verify(visitor).visit(label2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test fun visitSequenceWithSub() {
        val label1 = LabelExpression("foo")
        val label2 = LabelExpression("bar")
        val expression = SequenceExpression.of(label1, label2)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(label1)
        inOrder.verify(sub).visit(label2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test fun visitSwitch() {
        val key1 = LabelExpression("key1")
        val value1 = LabelExpression("value1")
        val key2 = LabelExpression("key2")
        val value2 = LabelExpression("value2")
        val expression = SwitchExpression()
            .addCase(key1).merge(value1)
            .addCase(key2).merge(value2)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(key1)
        inOrder.verify(visitor).visit(value1)
        inOrder.verify(visitor).visit(key2)
        inOrder.verify(visitor).visit(value2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test fun visitSwitchWithSub() {
        val key1 = LabelExpression("key1")
        val value1 = LabelExpression("value1")
        val key2 = LabelExpression("key2")
        val value2 = LabelExpression("value2")
        val expression = SwitchExpression()
            .addCase(key1).merge(value1)
            .addCase(key2).merge(value2)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(key1)
        inOrder.verify(sub).visit(value1)
        inOrder.verify(sub).visit(key2)
        inOrder.verify(sub).visit(value2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test fun visitUnbalancedSwitch() {
        val key1 = LabelExpression("key1")
        val value1 = LabelExpression("value1")
        val key2 = LabelExpression("key2")
        val expression = SwitchExpression()
            .addCase(key1).merge(value1)
            .addCase(key2)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(key1)
        inOrder.verify(visitor).visit(value1)
        inOrder.verify(visitor).visit(key2)
        inOrder.verify(visitor).leave(expression)
    }


    @Test fun visitNested() {
        val label1 = LabelExpression("label-1")
        val label2 = LabelExpression("label-2")
        val label3 = LabelExpression("label-3")
        val label4 = LabelExpression("label-4")
        val label5 = LabelExpression("label-5")
        val sequence1 = SequenceExpression.of(label1, label2)
        val minus = MinusExpression.of(label4, label5)
        val sequence2 = SequenceExpression.of(label3, minus)
        val alternatives = AlternativesExpression.of(sequence1, sequence2)

        `when`(visitor.visit(minus)).thenReturn(visitor)
        `when`(visitor.visit(sequence1)).thenReturn(visitor)
        `when`(visitor.visit(sequence2)).thenReturn(visitor)
        `when`(visitor.visit(alternatives)).thenReturn(visitor)

        alternatives.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(alternatives)
        inOrder.verify(visitor).visit(sequence1)
        inOrder.verify(visitor).visit(label1)
        inOrder.verify(visitor).visit(label2)
        inOrder.verify(visitor).leave(sequence1)
        inOrder.verify(visitor).visit(sequence2)
        inOrder.verify(visitor).visit(label3)
        inOrder.verify(visitor).visit(minus)
        inOrder.verify(visitor).visit(label4)
        inOrder.verify(visitor).visit(label5)
        inOrder.verify(visitor).leave(minus)
        inOrder.verify(visitor).leave(sequence2)
        inOrder.verify(visitor).leave(alternatives)
    }
}
