package spec.generator

import com.github.t1.codepoint.CodePoint
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import spec.generator.Expression.AlternativesExpression
import spec.generator.Expression.CodePointExpression
import spec.generator.Expression.MinusExpression
import spec.generator.Expression.RangeExpression
import spec.generator.Expression.ReferenceExpression
import spec.generator.Expression.RepeatedExpression
import spec.generator.Expression.SequenceExpression
import spec.generator.Expression.SwitchExpression
import spec.generator.Expression.VariableExpression

class ExpressionVisitorTest {
    private val visitor: Visitor = mock()

    @AfterEach fun tearDown() {
        verifyNoMoreInteractions(visitor)
    }


    @Test fun visitCodePoint() {
        val expression = CodePointExpression(CodePoint.of("x"))

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test fun visitVariable() {
        val expression = VariableExpression("foo")

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test fun visitReference() {
        val expression = ReferenceExpression("foo")

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test fun visitRepeated() {
        val variable = VariableExpression("foo")
        val expression = RepeatedExpression(variable, "4")
        given(visitor.visit(expression)).willReturn(visitor)

        expression.guide(visitor)

        visitor.inOrder {
            verify(visitor).visit(expression)
            verify(visitor).visit(variable)
            verify(visitor).leave(expression)
        }
    }

    @Test fun visitRepeatedWithSub() {
        val variable = VariableExpression("foo")
        val expression = RepeatedExpression(variable, "4")
        val sub: Visitor = mock()
        given(visitor.visit(expression)).willReturn(sub)

        expression.guide(visitor)

        inOrder(visitor, sub) {
            verify(visitor).visit(expression)
            verify(sub).visit(variable)
            verify(visitor).leave(expression)
        }
    }


    @Test fun visitRange() {
        val variable1 = VariableExpression("foo")
        val variable2 = VariableExpression("bar")
        val expression = RangeExpression(variable1, variable2)
        given(visitor.visit(expression)).willReturn(visitor)

        expression.guide(visitor)

        visitor.inOrder {
            verify(visitor).visit(expression)
            verify(visitor).visit(variable1)
            verify(visitor).betweenRange(expression)
            verify(visitor).visit(variable2)
            verify(visitor).leave(expression)
        }
    }

    @Test fun visitRangeWithSub() {
        val variable1 = VariableExpression("foo")
        val variable2 = VariableExpression("bar")
        val expression = RangeExpression(variable1, variable2)
        val sub: Visitor = mock()
        given(visitor.visit(expression)).willReturn(sub)

        expression.guide(visitor)

        inOrder(visitor, sub) {
            verify(visitor).visit(expression)
            verify(sub).visit(variable1)
            verify(sub).visit(variable2)
            verify(visitor).leave(expression)
        }
    }


    @Test fun visitMinus() {
        val variable1 = VariableExpression("foo")
        val variable2 = VariableExpression("bar")
        val variable3 = VariableExpression("baz")
        val expression = MinusExpression.of(variable1, variable2).minus(variable3)
        given(visitor.visit(expression)).willReturn(visitor)

        expression.guide(visitor)

        visitor.inOrder {
            verify(visitor).visit(expression)
            verify(visitor).beforeMinuend(variable1)
            verify(visitor).visit(variable1)
            verify(visitor).afterMinuend(variable1)
            verify(visitor).beforeSubtrahend(variable2)
            verify(visitor).visit(variable2)
            verify(visitor).afterSubtrahend(variable2)
            verify(visitor).beforeSubtrahend(variable3)
            verify(visitor).visit(variable3)
            verify(visitor).afterSubtrahend(variable3)
            verify(visitor).leave(expression)
        }
    }

    @Test fun visitMinusWithSub() {
        val variable1 = VariableExpression("foo")
        val variable2 = VariableExpression("bar")
        val variable3 = VariableExpression("baz")
        val expression = MinusExpression.of(variable1, variable2).minus(variable3)
        val sub: Visitor = mock()
        given(visitor.visit(expression)).willReturn(sub)

        expression.guide(visitor)

        inOrder(visitor, sub) {
            verify(visitor).visit(expression)
            verify(sub).visit(variable1)
            verify(sub).visit(variable2)
            verify(sub).visit(variable3)
            verify(visitor).leave(expression)
        }
    }


    @Test fun visitAlternatives() {
        val variable1 = VariableExpression("foo")
        val variable2 = VariableExpression("bar")
        val expression = AlternativesExpression.of(variable1, variable2)
        given(visitor.visit(expression)).willReturn(visitor)

        expression.guide(visitor)

        visitor.inOrder {
            verify(visitor).beforeCollection(expression)
            verify(visitor).visit(expression)
            verify(visitor).beforeAlternativesItem(variable1)
            verify(visitor).visit(variable1)
            verify(visitor).afterAlternativesItem(variable1)
            verify(visitor).betweenAlternativesItems()
            verify(visitor).beforeAlternativesItem(variable2)
            verify(visitor).visit(variable2)
            verify(visitor).afterAlternativesItem(variable2)
            verify(visitor).leave(expression)
            verify(visitor).afterCollection(expression)
        }
    }

    @Test fun visitAlternativesWithSub() {
        val variable1 = VariableExpression("foo")
        val variable2 = VariableExpression("bar")
        val expression = AlternativesExpression.of(variable1, variable2)
        val sub: Visitor = mock()
        given(visitor.visit(expression)).willReturn(sub)

        expression.guide(visitor)

        inOrder(visitor, sub) {
            verify(visitor).beforeCollection(expression)
            verify(visitor).visit(expression)
            verify(sub).visit(variable1)
            verify(sub).visit(variable2)
            verify(visitor).leave(expression)
            verify(visitor).afterCollection(expression)
        }
    }


    @Test fun visitSequence() {
        val variable1 = VariableExpression("foo")
        val variable2 = VariableExpression("bar")
        val expression = SequenceExpression.of(variable1, variable2)
        given(visitor.visit(expression)).willReturn(visitor)

        expression.guide(visitor)

        visitor.inOrder {
            verify(visitor).beforeCollection(expression)
            verify(visitor).visit(expression)
            verify(visitor).beforeSequenceItem(variable1)
            verify(visitor).visit(variable1)
            verify(visitor).afterSequenceItem(variable1)
            verify(visitor).betweenSequenceItems()
            verify(visitor).beforeSequenceItem(variable2)
            verify(visitor).visit(variable2)
            verify(visitor).afterSequenceItem(variable2)
            verify(visitor).leave(expression)
            verify(visitor).afterCollection(expression)
        }
    }

    @Test fun visitSequenceWithSub() {
        val variable1 = VariableExpression("foo")
        val variable2 = VariableExpression("bar")
        val expression = SequenceExpression.of(variable1, variable2)
        val sub: Visitor = mock()
        given(visitor.visit(expression)).willReturn(sub)

        expression.guide(visitor)

        inOrder(visitor, sub) {
            verify(visitor).beforeCollection(expression)
            verify(visitor).visit(expression)
            verify(sub).beforeSequenceItem(variable1)
            verify(sub).visit(variable1)
            verify(sub).afterSequenceItem(variable1)
            verify(sub).betweenSequenceItems()
            verify(sub).beforeSequenceItem(variable2)
            verify(sub).visit(variable2)
            verify(sub).afterSequenceItem(variable2)
            verify(visitor).leave(expression)
            verify(visitor).afterCollection(expression)
        }
    }

    @Test fun visitSwitch() {
        val key1 = VariableExpression("key1")
        val value1 = VariableExpression("value1")
        val key2 = VariableExpression("key2")
        val value2 = VariableExpression("value2")
        val expression = SwitchExpression.of(key1, value1)
            .addCase(key2).merge(value2)
        given(visitor.visit(expression)).willReturn(visitor)

        expression.guide(visitor)

        visitor.inOrder {
            verify(visitor).beforeCollection(expression)
            verify(visitor).visit(expression)
            verify(visitor).beforeSwitchItem()
            verify(visitor).visit(key1)
            verify(visitor).betweenSwitchCaseAndValue()
            verify(visitor).visit(value1)
            verify(visitor).afterSwitchItem(key1, value1)
            verify(visitor).beforeSwitchItem()
            verify(visitor).visit(key2)
            verify(visitor).betweenSwitchCaseAndValue()
            verify(visitor).visit(value2)
            verify(visitor).afterSwitchItem(key2, value2)
            verify(visitor).leave(expression)
            verify(visitor).afterCollection(expression)
        }
    }

    @Test fun visitSwitchWithSub() {
        val key1 = VariableExpression("key1")
        val value1 = VariableExpression("value1")
        val key2 = VariableExpression("key2")
        val value2 = VariableExpression("value2")
        val expression = SwitchExpression.of(key1, value1)
            .addCase(key2).merge(value2)
        val sub: Visitor = mock()
        given(visitor.visit(expression)).willReturn(sub)

        expression.guide(visitor)

        inOrder(visitor, sub) {
            verify(visitor).beforeCollection(expression)
            verify(visitor).visit(expression)
            verify(sub).beforeSwitchItem()
            verify(sub).visit(key1)
            verify(sub).betweenSwitchCaseAndValue()
            verify(sub).visit(value1)
            verify(sub).afterSwitchItem(key1, value1)
            verify(sub).beforeSwitchItem()
            verify(sub).visit(key2)
            verify(sub).betweenSwitchCaseAndValue()
            verify(sub).visit(value2)
            verify(sub).afterSwitchItem(key2, value2)
            verify(visitor).leave(expression)
            verify(visitor).afterCollection(expression)
        }
    }

    @Test fun visitUnbalancedSwitch() {
        val key1 = VariableExpression("key1")
        val value1 = VariableExpression("value1")
        val key2 = VariableExpression("key2")
        val expression = SwitchExpression.of(key1, value1)
            .addCase(key2)
        given(visitor.visit(expression)).willReturn(visitor)

        expression.guide(visitor)

        visitor.inOrder {
            verify(visitor).beforeCollection(expression)
            verify(visitor).visit(expression)
            verify(visitor).beforeSwitchItem()
            verify(visitor).visit(key1)
            verify(visitor).betweenSwitchCaseAndValue()
            verify(visitor).visit(value1)
            verify(visitor).afterSwitchItem(key1,value1)
            verify(visitor).beforeSwitchItem()
            verify(visitor).visit(key2)
            verify(visitor).betweenSwitchCaseAndValue()
            verify(visitor).leave(expression)
            verify(visitor).afterCollection(expression)
        }
    }


    @Test fun visitNested() {
        val variable1 = VariableExpression("var1")
        val variable2 = VariableExpression("var2")
        val variable3 = VariableExpression("var3")
        val variable4 = VariableExpression("var4")
        val variable5 = VariableExpression("var5")
        val sequence1 = SequenceExpression.of(variable1, variable2)
        val minus = MinusExpression.of(variable4, variable5)
        val sequence2 = SequenceExpression.of(variable3, minus)
        val alternatives = AlternativesExpression.of(sequence1, sequence2)

        given(visitor.visit(minus)).willReturn(visitor)
        given(visitor.visit(sequence1)).willReturn(visitor)
        given(visitor.visit(sequence2)).willReturn(visitor)
        given(visitor.visit(alternatives)).willReturn(visitor)

        alternatives.guide(visitor)

        visitor.inOrder {
            /* 01 */ verify(visitor).beforeCollection(alternatives)
            /* 02 */ verify(visitor).visit(alternatives)
            /* 03 */ verify(visitor).beforeAlternativesItem(sequence1)
            /* 04 */ verify(visitor).beforeCollection(sequence1)
            /* 05 */ verify(visitor).visit(sequence1)
            /* 06 */ verify(visitor).beforeSequenceItem(variable1)
            /* 07 */ verify(visitor).visit(variable1)
            /* 08 */ verify(visitor).afterSequenceItem(variable1)
            /* 09 */ verify(visitor).betweenSequenceItems()
            /* 10 */ verify(visitor).beforeSequenceItem(variable2)
            /* 11 */ verify(visitor).visit(variable2)
            /* 12 */ verify(visitor).afterSequenceItem(variable2)
            /* 13 */ verify(visitor).leave(sequence1)
            /* 14 */ verify(visitor).afterCollection(sequence1)
            /* 15 */ verify(visitor).afterAlternativesItem(sequence1)
            /* 16 */ verify(visitor).betweenAlternativesItems()
            /* 17 */ verify(visitor).beforeAlternativesItem(sequence2)
            /* 18 */ verify(visitor).beforeCollection(sequence2)
            /* 19 */ verify(visitor).visit(sequence2)
            /* 20 */ verify(visitor).beforeSequenceItem(variable3)
            /* 21 */ verify(visitor).visit(variable3)
            /* 22 */ verify(visitor).afterSequenceItem(variable3)
            /* 23 */ verify(visitor).betweenSequenceItems()
            /* 24 */ verify(visitor).beforeSequenceItem(minus)
            /* 25 */ verify(visitor).visit(minus)
            /* 26 */ verify(visitor).beforeMinuend(variable4)
            /* 27 */ verify(visitor).visit(variable4)
            /* 28 */ verify(visitor).afterMinuend(variable4)
            /* 29 */ verify(visitor).beforeSubtrahend(variable5)
            /* 30 */ verify(visitor).visit(variable5)
            /* 31 */ verify(visitor).afterSubtrahend(variable5)
            /* 32 */ verify(visitor).leave(minus)
            /* 33 */ verify(visitor).afterSequenceItem(minus)
            /* 34 */ verify(visitor).leave(sequence2)
            /* 35 */ verify(visitor).afterCollection(sequence2)
            /* 36 */ verify(visitor).afterAlternativesItem(sequence2)
            /* 37 */ verify(visitor).leave(alternatives)
            /* 38 */ verify(visitor).afterCollection(alternatives)
        }
    }
}
