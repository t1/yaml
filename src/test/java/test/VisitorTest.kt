package test

import com.github.t1.yaml.model.Alias
import com.github.t1.yaml.model.Mapping
import com.github.t1.yaml.model.Mapping.Entry
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Sequence
import com.github.t1.yaml.model.Sequence.Item
import com.github.t1.yaml.model.Visitor
import com.github.t1.yaml.tools.Scanner
import com.nhaarman.mockitokotlin2.mock
import helpers.catchParseException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

class VisitorTest {
    private val visitor: Visitor = mock()

    @AfterEach fun tearDown() {
        verifyNoMoreInteractions(visitor)
    }

    @Test fun shouldFailUnexpected() {
        val scanner = Scanner("a")

        val exception = catchParseException { scanner.expect("b") }

        assertThat(exception).hasMessage("expected [b][LATIN SMALL LETTER B][0x62] but got [a][LATIN SMALL LETTER A][0x61] at line 1 char 1")
    }


    @Test fun shouldVisitAliasNode() {
        val node = Alias()

        node.guide(visitor)

        verify<Visitor>(visitor).visit(node)
    }


    @Test fun shouldVisitSequenceNode() {
        val alias = Alias()
        val item1 = Item(node = alias)
        val line1 = Line().text("foo")
        val line2 = Line().text("bar")
        val scalar = Scalar().plain().line(line1).line(line2)
        val item2 = Item(node = scalar)
        val node = Sequence().item(item1).item(item2)

        node.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(node)
        inOrder.verify(visitor).enterSequenceItem(node, item1)
        inOrder.verify(visitor).visit(alias)
        inOrder.verify(visitor).leaveSequenceItem(node, item1)
        inOrder.verify(visitor).enterSequenceItem(node, item2)
        inOrder.verify(visitor).visit(scalar)
        inOrder.verify(visitor).enterScalarLine(scalar, line1)
        inOrder.verify(visitor).visit(line1)
        inOrder.verify(visitor).leaveScalarLine(scalar, line1)
        inOrder.verify(visitor).enterScalarLine(scalar, line2)
        inOrder.verify(visitor).visit(line2)
        inOrder.verify(visitor).leaveScalarLine(scalar, line2)
        inOrder.verify(visitor).leave(scalar)
        inOrder.verify(visitor).leaveSequenceItem(node, item2)
        inOrder.verify(visitor).leave(node)
    }


    @Test fun shouldVisitEmptySingleQuotedScalarNode() {
        val node = Scalar().singleQuoted()

        node.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(node)
        inOrder.verify(visitor).leave(node)
    }

    @Test fun shouldVisitDoubleQuotedScalarNode() {
        val line = Line().text("foo")
        val node = Scalar().doubleQuoted().line(line)

        node.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(node)
        inOrder.verify(visitor).enterScalarLine(node, line)
        inOrder.verify(visitor).visit(line)
        inOrder.verify(visitor).leaveScalarLine(node, line)
        inOrder.verify(visitor).leave(node)
    }

    @Test fun shouldVisitPlainTwoLineScalarNode() {
        val line1 = Line().text("foo")
        val line2 = Line().text("bar")
        val node = Scalar().plain().line(line1).line(line2)

        node.guide(visitor)

        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(node)
        inOrder.verify(visitor).enterScalarLine(node, line1)
        inOrder.verify(visitor).visit(line1)
        inOrder.verify(visitor).leaveScalarLine(node, line1)
        inOrder.verify(visitor).enterScalarLine(node, line2)
        inOrder.verify(visitor).visit(line2)
        inOrder.verify(visitor).leaveScalarLine(node, line2)
        inOrder.verify(visitor).leave(node)
    }


    @Test fun shouldVisitMappingNode() {
        val node = Mapping()

        val key1 = Alias()
        val value1line1 = Line().text("foo")
        val value1line2 = Line().text("bar")
        val value1 = Scalar().plain().line(value1line1).line(value1line2)
        node.entry(key1, value1)

        val key2line1 = Line().text("key2")
        val key2 = Scalar().line(key2line1)
        val value2line1 = Line().text("value2")
        val value2 = Scalar().line(value2line1)
        node.entry(key2, value2)


        node.guide(visitor)


        val inOrder = inOrder(visitor)
        inOrder.verify(visitor).visit(node)

        val entry1 = Entry(key = key1, value = value1)
        inOrder.verify(visitor).enterMappingEntry(node, entry1)
        /**/inOrder.verify(visitor).enterMappingKey(entry1, key1)
        /**//**/inOrder.verify(visitor).visit(key1)
        /**/inOrder.verify(visitor).leaveMappingKey(entry1, key1)
        /**/inOrder.verify(visitor).enterMappingValue(entry1, value1)
        /**//**/inOrder.verify(visitor).visit(value1)
        /**//**//**/inOrder.verify(visitor).enterScalarLine(value1, value1line1)
        /**//**//**//**/inOrder.verify(visitor).visit(value1line1)
        /**//**//**/inOrder.verify(visitor).leaveScalarLine(value1, value1line1)
        /**//**//**/inOrder.verify(visitor).enterScalarLine(value1, value1line2)
        /**//**//**//**/inOrder.verify(visitor).visit(value1line2)
        /**//**//**/inOrder.verify(visitor).leaveScalarLine(value1, value1line2)
        /**//**/inOrder.verify(visitor).leave(value1)
        /**/inOrder.verify(visitor).leaveMappingValue(entry1, value1)
        inOrder.verify(visitor).leaveMappingEntry(node, entry1)

        val entry2 = Entry(key = key2, value = value2)
        inOrder.verify(visitor).enterMappingEntry(node, entry2)
        /**/inOrder.verify(visitor).enterMappingKey(entry2, key2)
        /**//**/inOrder.verify(visitor).visit(key2)
        /**//**//**/inOrder.verify(visitor).enterScalarLine(key2, key2line1)
        /**//**//**//**/inOrder.verify(visitor).visit(key2line1)
        /**//**//**/inOrder.verify(visitor).leaveScalarLine(key2, key2line1)
        /**//**/inOrder.verify(visitor).leave(key2)
        /**/inOrder.verify(visitor).leaveMappingKey(entry2, key2)
        /**/inOrder.verify(visitor).enterMappingValue(entry2, value2)
        /**//**/inOrder.verify(visitor).visit(value2)
        /**//**//**/inOrder.verify(visitor).enterScalarLine(value2, value2line1)
        /**//**//**//**/inOrder.verify(visitor).visit(value2line1)
        /**//**//**/inOrder.verify(visitor).leaveScalarLine(value2, value2line1)
        /**//**/inOrder.verify(visitor).leave(value2)
        /**/inOrder.verify(visitor).leaveMappingValue(entry2, value2)
        inOrder.verify(visitor).leaveMappingEntry(node, entry2)

        inOrder.verify(visitor).leave(node)
    }
}
