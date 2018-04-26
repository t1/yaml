package com.github.t1.yaml.parser;

import com.github.t1.yaml.dump.CodePoint;
import com.github.t1.yaml.parser.Expression.AlternativesExpression;
import com.github.t1.yaml.parser.Expression.CodePointExpression;
import com.github.t1.yaml.parser.Expression.LiteralExpression;
import com.github.t1.yaml.parser.Expression.MinusExpression;
import com.github.t1.yaml.parser.Expression.NullExpression;
import com.github.t1.yaml.parser.Expression.RangeExpression;
import com.github.t1.yaml.parser.Expression.ReferenceExpression;
import com.github.t1.yaml.parser.Expression.RepeatedExpression;
import com.github.t1.yaml.parser.Expression.SequenceExpression;
import com.github.t1.yaml.parser.Expression.SwitchExpression;
import com.github.t1.yaml.parser.Expression.Visitor;
import helpers.MockitoExtension;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) class ExpressionVisitorTest {
    @Mock private Visitor visitor;

    @AfterEach void tearDown() {
        verifyNoMoreInteractions(visitor);
    }


    @Test void visitNull() {
        val expression = new NullExpression();

        expression.guide(visitor);

        verify(visitor).visit(expression);
    }


    @Test void visitCodePoint() {
        val expression = new CodePointExpression(CodePoint.of("x"));

        expression.guide(visitor);

        verify(visitor).visit(expression);
    }


    @Test void visitLiteral() {
        val expression = new LiteralExpression("foo");

        expression.guide(visitor);

        verify(visitor).visit(expression);
    }


    @Test void visitReference() {
        val expression = new ReferenceExpression("foo");

        expression.guide(visitor);

        verify(visitor).visit(expression);
    }


    @Test void visitRepeated() {
        val literal = new LiteralExpression("foo");
        val expression = new RepeatedExpression(literal, "4");
        when(visitor.visit(expression)).thenReturn(visitor);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(visitor).visit(literal);
        inOrder.verify(visitor).leave(expression);
    }

    @Test void visitRepeatedWithSub() {
        val literal = new LiteralExpression("foo");
        val expression = new RepeatedExpression(literal, "4");
        Visitor sub = mock(Visitor.class);
        when(visitor.visit(expression)).thenReturn(sub);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor, sub);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(sub).visit(literal);
        inOrder.verify(sub).leave(expression);
    }


    @Test void visitRange() {
        val literal1 = new LiteralExpression("foo");
        val literal2 = new LiteralExpression("bar");
        val expression = new RangeExpression(literal1, literal2);
        when(visitor.visit(expression)).thenReturn(visitor);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(visitor).visit(literal1);
        inOrder.verify(visitor).visit(literal2);
        inOrder.verify(visitor).leave(expression);
    }

    @Test void visitRangeWithSub() {
        val literal1 = new LiteralExpression("foo");
        val literal2 = new LiteralExpression("bar");
        val expression = new RangeExpression(literal1, literal2);
        Visitor sub = mock(Visitor.class);
        when(visitor.visit(expression)).thenReturn(sub);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor, sub);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(sub).visit(literal1);
        inOrder.verify(sub).visit(literal2);
        inOrder.verify(sub).leave(expression);
    }


    @Test void visitMinus() {
        val literal1 = new LiteralExpression("foo");
        val literal2 = new LiteralExpression("bar");
        val literal3 = new LiteralExpression("baz");
        val expression = MinusExpression.of(literal1, literal2).minus(literal3);
        when(visitor.visit(expression)).thenReturn(visitor);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(visitor).visit(literal1);
        inOrder.verify(visitor).visit(literal2);
        inOrder.verify(visitor).visit(literal3);
        inOrder.verify(visitor).leave(expression);
    }

    @Test void visitMinusWithSub() {
        val literal1 = new LiteralExpression("foo");
        val literal2 = new LiteralExpression("bar");
        val literal3 = new LiteralExpression("baz");
        val expression = MinusExpression.of(literal1, literal2).minus(literal3);
        Visitor sub = mock(Visitor.class);
        when(visitor.visit(expression)).thenReturn(sub);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor, sub);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(sub).visit(literal1);
        inOrder.verify(sub).visit(literal2);
        inOrder.verify(sub).visit(literal3);
        inOrder.verify(sub).leave(expression);
    }


    @Test void visitAlternatives() {
        val literal1 = new LiteralExpression("foo");
        val literal2 = new LiteralExpression("bar");
        val expression = AlternativesExpression.of(literal1, literal2);
        when(visitor.visit(expression)).thenReturn(visitor);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(visitor).visit(literal1);
        inOrder.verify(visitor).visit(literal2);
        inOrder.verify(visitor).leave(expression);
    }

    @Test void visitAlternativesWithSub() {
        val literal1 = new LiteralExpression("foo");
        val literal2 = new LiteralExpression("bar");
        val expression = AlternativesExpression.of(literal1, literal2);
        Visitor sub = mock(Visitor.class);
        when(visitor.visit(expression)).thenReturn(sub);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor, sub);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(sub).visit(literal1);
        inOrder.verify(sub).visit(literal2);
        inOrder.verify(sub).leave(expression);
    }


    @Test void visitSequence() {
        val literal1 = new LiteralExpression("foo");
        val literal2 = new LiteralExpression("bar");
        val expression = SequenceExpression.of(literal1, literal2);
        when(visitor.visit(expression)).thenReturn(visitor);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(visitor).visit(literal1);
        inOrder.verify(visitor).visit(literal2);
        inOrder.verify(visitor).leave(expression);
    }

    @Test void visitSequenceWithSub() {
        val literal1 = new LiteralExpression("foo");
        val literal2 = new LiteralExpression("bar");
        val expression = SequenceExpression.of(literal1, literal2);
        Visitor sub = mock(Visitor.class);
        when(visitor.visit(expression)).thenReturn(sub);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor, sub);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(sub).visit(literal1);
        inOrder.verify(sub).visit(literal2);
        inOrder.verify(sub).leave(expression);
    }

    @Test void visitSwitch() {
        val key1 = new LiteralExpression("key1");
        val value1 = new LiteralExpression("value1");
        val key2 = new LiteralExpression("key2");
        val value2 = new LiteralExpression("value2");
        val expression = new SwitchExpression()
                .addCase(key1).merge(value1)
                .addCase(key2).merge(value2);
        when(visitor.visit(expression)).thenReturn(visitor);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(visitor).visit(key1);
        inOrder.verify(visitor).visit(value1);
        inOrder.verify(visitor).visit(key2);
        inOrder.verify(visitor).visit(value2);
        inOrder.verify(visitor).leave(expression);
    }

    @Test void visitSwitchWithSub() {
        val key1 = new LiteralExpression("key1");
        val value1 = new LiteralExpression("value1");
        val key2 = new LiteralExpression("key2");
        val value2 = new LiteralExpression("value2");
        val expression = new SwitchExpression()
                .addCase(key1).merge(value1)
                .addCase(key2).merge(value2);
        Visitor sub = mock(Visitor.class);
        when(visitor.visit(expression)).thenReturn(sub);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor, sub);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(sub).visit(key1);
        inOrder.verify(sub).visit(value1);
        inOrder.verify(sub).visit(key2);
        inOrder.verify(sub).visit(value2);
        inOrder.verify(sub).leave(expression);
    }

    @Test void visitUnbalancedSwitch() {
        val key1 = new LiteralExpression("key1");
        val value1 = new LiteralExpression("value1");
        val key2 = new LiteralExpression("key2");
        val expression = new SwitchExpression()
                .addCase(key1).merge(value1)
                .addCase(key2);
        when(visitor.visit(expression)).thenReturn(visitor);

        expression.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(expression);
        inOrder.verify(visitor).visit(key1);
        inOrder.verify(visitor).visit(value1);
        inOrder.verify(visitor).visit(key2);
        inOrder.verify(visitor).leave(expression);
    }


    @Test void visitNested() {
        val literal1 = new LiteralExpression("literal-1");
        val literal2 = new LiteralExpression("literal-2");
        val literal3 = new LiteralExpression("literal-3");
        val literal4 = new LiteralExpression("literal-4");
        val literal5 = new LiteralExpression("literal-5");
        val sequence1 = SequenceExpression.of(literal1, literal2);
        val minus = MinusExpression.of(literal4, literal5);
        val sequence2 = SequenceExpression.of(literal3, minus);
        val alternatives = AlternativesExpression.of(sequence1, sequence2);

        when(visitor.visit(minus)).thenReturn(visitor);
        when(visitor.visit(sequence1)).thenReturn(visitor);
        when(visitor.visit(sequence2)).thenReturn(visitor);
        when(visitor.visit(alternatives)).thenReturn(visitor);

        alternatives.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(alternatives);
        inOrder.verify(visitor).visit(sequence1);
        inOrder.verify(visitor).visit(literal1);
        inOrder.verify(visitor).visit(literal2);
        inOrder.verify(visitor).leave(sequence1);
        inOrder.verify(visitor).visit(sequence2);
        inOrder.verify(visitor).visit(literal3);
        inOrder.verify(visitor).visit(minus);
        inOrder.verify(visitor).visit(literal4);
        inOrder.verify(visitor).visit(literal5);
        inOrder.verify(visitor).leave(minus);
        inOrder.verify(visitor).leave(sequence2);
        inOrder.verify(visitor).leave(alternatives);
    }
}
