package com.github.t1.yaml.parser

import com.github.t1.yaml.dump.CodePoint
import com.github.t1.yaml.parser.Expression.AlternativesExpression
import com.github.t1.yaml.parser.Expression.CodePointExpression
import com.github.t1.yaml.parser.Expression.LiteralExpression
import com.github.t1.yaml.parser.Expression.MinusExpression
import com.github.t1.yaml.parser.Expression.NullExpression
import com.github.t1.yaml.parser.Expression.RangeExpression
import com.github.t1.yaml.parser.Expression.ReferenceExpression
import com.github.t1.yaml.parser.Expression.RepeatedExpression
import com.github.t1.yaml.parser.Expression.SequenceExpression
import com.github.t1.yaml.parser.Expression.SwitchExpression
import com.github.t1.yaml.parser.Expression.Visitor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import java.io.IOException

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


    @Test @Throws(IOException::class)
    fun visitCodePoint() {
        val expression = CodePointExpression(CodePoint.of("x"))

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test @Throws(IOException::class)
    fun visitLiteral() {
        val expression = LiteralExpression("foo")

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test @Throws(IOException::class)
    fun visitReference() {
        val expression = ReferenceExpression("foo")

        expression.guide(visitor)

        verify<Visitor>(visitor).visit(expression)
    }


    @Test @Throws(IOException::class)
    fun visitRepeated() {
        val literal = LiteralExpression("foo")
        val expression = RepeatedExpression(literal, "4")
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(literal)
        inOrder.verify(visitor).leave(expression)
    }

    @Test @Throws(IOException::class)
    fun visitRepeatedWithSub() {
        val literal = LiteralExpression("foo")
        val expression = RepeatedExpression(literal, "4")
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(literal)
        inOrder.verify(sub).leave(expression)
    }


    @Test @Throws(IOException::class)
    fun visitRange() {
        val literal1 = LiteralExpression("foo")
        val literal2 = LiteralExpression("bar")
        val expression = RangeExpression(literal1, literal2)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(literal1)
        inOrder.verify(visitor).visit(literal2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test @Throws(IOException::class)
    fun visitRangeWithSub() {
        val literal1 = LiteralExpression("foo")
        val literal2 = LiteralExpression("bar")
        val expression = RangeExpression(literal1, literal2)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(literal1)
        inOrder.verify(sub).visit(literal2)
        inOrder.verify(sub).leave(expression)
    }


    @Test @Throws(IOException::class)
    fun visitMinus() {
        val literal1 = LiteralExpression("foo")
        val literal2 = LiteralExpression("bar")
        val literal3 = LiteralExpression("baz")
        val expression = MinusExpression.of(literal1, literal2).minus(literal3)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(literal1)
        inOrder.verify(visitor).visit(literal2)
        inOrder.verify(visitor).visit(literal3)
        inOrder.verify(visitor).leave(expression)
    }

    @Test @Throws(IOException::class)
    fun visitMinusWithSub() {
        val literal1 = LiteralExpression("foo")
        val literal2 = LiteralExpression("bar")
        val literal3 = LiteralExpression("baz")
        val expression = MinusExpression.of(literal1, literal2).minus(literal3)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(literal1)
        inOrder.verify(sub).visit(literal2)
        inOrder.verify(sub).visit(literal3)
        inOrder.verify(sub).leave(expression)
    }


    @Test @Throws(IOException::class)
    fun visitAlternatives() {
        val literal1 = LiteralExpression("foo")
        val literal2 = LiteralExpression("bar")
        val expression = AlternativesExpression.of(literal1, literal2)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(literal1)
        inOrder.verify(visitor).visit(literal2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test @Throws(IOException::class)
    fun visitAlternativesWithSub() {
        val literal1 = LiteralExpression("foo")
        val literal2 = LiteralExpression("bar")
        val expression = AlternativesExpression.of(literal1, literal2)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(literal1)
        inOrder.verify(sub).visit(literal2)
        inOrder.verify(sub).leave(expression)
    }


    @Test @Throws(IOException::class)
    fun visitSequence() {
        val literal1 = LiteralExpression("foo")
        val literal2 = LiteralExpression("bar")
        val expression = SequenceExpression.of(literal1, literal2)
        `when`(visitor.visit(expression)).thenReturn(visitor)

        expression.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(visitor).visit(literal1)
        inOrder.verify(visitor).visit(literal2)
        inOrder.verify(visitor).leave(expression)
    }

    @Test @Throws(IOException::class)
    fun visitSequenceWithSub() {
        val literal1 = LiteralExpression("foo")
        val literal2 = LiteralExpression("bar")
        val expression = SequenceExpression.of(literal1, literal2)
        val sub = mock(Visitor::class.java)
        `when`(visitor.visit(expression)).thenReturn(sub)

        expression.guide(visitor)

        val inOrder = inOrder(visitor, sub)
        inOrder.verify(visitor).visit(expression)
        inOrder.verify(sub).visit(literal1)
        inOrder.verify(sub).visit(literal2)
        inOrder.verify(sub).leave(expression)
    }

    @Test @Throws(IOException::class)
    fun visitSwitch() {
        val key1 = LiteralExpression("key1")
        val value1 = LiteralExpression("value1")
        val key2 = LiteralExpression("key2")
        val value2 = LiteralExpression("value2")
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

    @Test @Throws(IOException::class)
    fun visitSwitchWithSub() {
        val key1 = LiteralExpression("key1")
        val value1 = LiteralExpression("value1")
        val key2 = LiteralExpression("key2")
        val value2 = LiteralExpression("value2")
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
        inOrder.verify(sub).leave(expression)
    }

    @Test @Throws(IOException::class)
    fun visitUnbalancedSwitch() {
        val key1 = LiteralExpression("key1")
        val value1 = LiteralExpression("value1")
        val key2 = LiteralExpression("key2")
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


    @Test @Throws(IOException::class)
    fun visitNested() {
        val literal1 = LiteralExpression("literal-1")
        val literal2 = LiteralExpression("literal-2")
        val literal3 = LiteralExpression("literal-3")
        val literal4 = LiteralExpression("literal-4")
        val literal5 = LiteralExpression("literal-5")
        val sequence1 = SequenceExpression.of(literal1, literal2)
        val minus = MinusExpression.of(literal4, literal5)
        val sequence2 = SequenceExpression.of(literal3, minus)
        val alternatives = AlternativesExpression.of(sequence1, sequence2)

        `when`(visitor.visit(minus)).thenReturn(visitor)
        `when`(visitor.visit(sequence1)).thenReturn(visitor)
        `when`(visitor.visit(sequence2)).thenReturn(visitor)
        `when`(visitor.visit(alternatives)).thenReturn(visitor)

        alternatives.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(alternatives)
        inOrder.verify(visitor).visit(sequence1)
        inOrder.verify(visitor).visit(literal1)
        inOrder.verify(visitor).visit(literal2)
        inOrder.verify(visitor).leave(sequence1)
        inOrder.verify(visitor).visit(sequence2)
        inOrder.verify(visitor).visit(literal3)
        inOrder.verify(visitor).visit(minus)
        inOrder.verify(visitor).visit(literal4)
        inOrder.verify(visitor).visit(literal5)
        inOrder.verify(visitor).leave(minus)
        inOrder.verify(visitor).leave(sequence2)
        inOrder.verify(visitor).leave(alternatives)
    }
}
