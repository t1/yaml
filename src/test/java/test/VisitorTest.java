package test;

import com.github.t1.yaml.model.Alias;
import com.github.t1.yaml.model.Mapping;
import com.github.t1.yaml.model.Mapping.Entry;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.model.Sequence;
import com.github.t1.yaml.model.Sequence.Item;
import helpers.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class) class VisitorTest {
    @Mock private Node.Visitor visitor;

    @AfterEach void tearDown() {
        verifyNoMoreInteractions(visitor);
    }


    @Test void shouldVisitAliasNode() {
        Alias node = new Alias();

        node.guide(visitor);

        verify(visitor).visit(node);
    }


    @Test void shouldVisitSequenceNode() {
        Alias alias = new Alias();
        Item item1 = new Item().node(alias);
        Line line1 = new Line().text("foo");
        Line line2 = new Line().text("bar");
        Scalar scalar = new Scalar().plain().line(line1).line(line2);
        Item item2 = new Item().node(scalar);
        Sequence node = new Sequence().item(item1).item(item2);

        node.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(node);
        inOrder.verify(visitor).enterSequenceItem(node, item1);
        inOrder.verify(visitor).visit(alias);
        inOrder.verify(visitor).leaveSequenceItem(node, item1);
        inOrder.verify(visitor).enterSequenceItem(node, item2);
        inOrder.verify(visitor).visit(scalar);
        inOrder.verify(visitor).enterScalarLine(scalar, line1);
        inOrder.verify(visitor).visit(line1);
        inOrder.verify(visitor).leaveScalarLine(scalar, line1);
        inOrder.verify(visitor).enterScalarLine(scalar, line2);
        inOrder.verify(visitor).visit(line2);
        inOrder.verify(visitor).leaveScalarLine(scalar, line2);
        inOrder.verify(visitor).leave(scalar);
        inOrder.verify(visitor).leaveSequenceItem(node, item2);
        inOrder.verify(visitor).leave(node);
    }


    @Test void shouldVisitEmptySingleQuotedScalarNode() {
        Scalar node = new Scalar().singleQuoted();

        node.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(node);
        inOrder.verify(visitor).leave(node);
    }

    @Test void shouldVisitDoubleQuotedScalarNode() {
        Line line = new Line().text("foo");
        Scalar node = new Scalar().doubleQuoted().line(line);

        node.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(node);
        inOrder.verify(visitor).enterScalarLine(node, line);
        inOrder.verify(visitor).visit(line);
        inOrder.verify(visitor).leaveScalarLine(node, line);
        inOrder.verify(visitor).leave(node);
    }

    @Test void shouldVisitPlainTwoLineScalarNode() {
        Line line1 = new Line().text("foo");
        Line line2 = new Line().text("bar");
        Scalar node = new Scalar().plain().line(line1).line(line2);

        node.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(node);
        inOrder.verify(visitor).enterScalarLine(node, line1);
        inOrder.verify(visitor).visit(line1);
        inOrder.verify(visitor).leaveScalarLine(node, line1);
        inOrder.verify(visitor).enterScalarLine(node, line2);
        inOrder.verify(visitor).visit(line2);
        inOrder.verify(visitor).leaveScalarLine(node, line2);
        inOrder.verify(visitor).leave(node);
    }


    @Test void shouldVisitMappingNode() {
        Mapping node = new Mapping();

        Alias key1 = new Alias();
        Line value1line1 = new Line().text("foo");
        Line value1line2 = new Line().text("bar");
        Scalar value1 = new Scalar().plain().line(value1line1).line(value1line2);
        node.entry(key1, value1);

        Line key2line1 = new Line().text("key2");
        Scalar key2 = new Scalar().line(key2line1);
        Line value2line1 = new Line().text("value2");
        Scalar value2 = new Scalar().line(value2line1);
        node.entry(key2, value2);


        node.guide(visitor);


        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(node);

        Entry entry1 = new Entry().key(key1).value(value1);
        inOrder.verify(visitor).enterMappingEntry(node, entry1);
        /**/
        inOrder.verify(visitor).enterMappingKey(entry1, key1);
        /**//**/
        inOrder.verify(visitor).visit(key1);
        /**/
        inOrder.verify(visitor).leaveMappingKey(entry1, key1);
        /**/
        inOrder.verify(visitor).enterMappingValue(entry1, value1);
        /**//**/
        inOrder.verify(visitor).visit(value1);
        /**//**//**/
        inOrder.verify(visitor).enterScalarLine(value1, value1line1);
        /**//**//**//**/
        inOrder.verify(visitor).visit(value1line1);
        /**//**//**/
        inOrder.verify(visitor).leaveScalarLine(value1, value1line1);
        /**//**//**/
        inOrder.verify(visitor).enterScalarLine(value1, value1line2);
        /**//**//**//**/
        inOrder.verify(visitor).visit(value1line2);
        /**//**//**/
        inOrder.verify(visitor).leaveScalarLine(value1, value1line2);
        /**//**/
        inOrder.verify(visitor).leave(value1);
        /**/
        inOrder.verify(visitor).leaveMappingValue(entry1, value1);
        inOrder.verify(visitor).leaveMappingEntry(node, entry1);

        Entry entry2 = new Entry().key(key2).value(value2);
        inOrder.verify(visitor).enterMappingEntry(node, entry2);
        /**/
        inOrder.verify(visitor).enterMappingKey(entry2, key2);
        /**//**/
        inOrder.verify(visitor).visit(key2);
        /**//**//**/
        inOrder.verify(visitor).enterScalarLine(key2, key2line1);
        /**//**//**//**/
        inOrder.verify(visitor).visit(key2line1);
        /**//**//**/
        inOrder.verify(visitor).leaveScalarLine(key2, key2line1);
        /**//**/
        inOrder.verify(visitor).leave(key2);
        /**/
        inOrder.verify(visitor).leaveMappingKey(entry2, key2);
        /**/
        inOrder.verify(visitor).enterMappingValue(entry2, value2);
        /**//**/
        inOrder.verify(visitor).visit(value2);
        /**//**//**/
        inOrder.verify(visitor).enterScalarLine(value2, value2line1);
        /**//**//**//**/
        inOrder.verify(visitor).visit(value2line1);
        /**//**//**/
        inOrder.verify(visitor).leaveScalarLine(value2, value2line1);
        /**//**/
        inOrder.verify(visitor).leave(value2);
        /**/
        inOrder.verify(visitor).leaveMappingValue(entry2, value2);
        inOrder.verify(visitor).leaveMappingEntry(node, entry2);

        inOrder.verify(visitor).leave(node);
    }
}
