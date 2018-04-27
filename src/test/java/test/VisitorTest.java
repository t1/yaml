package test;

import com.github.t1.yaml.model.AliasNode;
import com.github.t1.yaml.model.MappingNode;
import com.github.t1.yaml.model.MappingNode.Entry;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.ScalarNode;
import com.github.t1.yaml.model.ScalarNode.Line;
import com.github.t1.yaml.model.SequenceNode;
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
        AliasNode node = new AliasNode();

        node.guide(visitor);

        verify(visitor).visit(node);
    }


    @Test void shouldVisitSequenceNode() {
        AliasNode item1 = new AliasNode();
        Line line1 = new Line().text("foo");
        Line line2 = new Line().text("bar");
        ScalarNode item2 = new ScalarNode().plain().line(line1).line(line2);
        SequenceNode node = new SequenceNode().item(item1).item(item2);

        node.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(node);
        inOrder.verify(visitor).enterSequenceItem(node, item1);
        inOrder.verify(visitor).visit(item1);
        inOrder.verify(visitor).leaveSequenceItem(node, item1);
        inOrder.verify(visitor).enterSequenceItem(node, item2);
        inOrder.verify(visitor).visit(item2);
        inOrder.verify(visitor).enterScalarLine(item2, line1);
        inOrder.verify(visitor).visit(line1);
        inOrder.verify(visitor).leaveScalarLine(item2, line1);
        inOrder.verify(visitor).enterScalarLine(item2, line2);
        inOrder.verify(visitor).visit(line2);
        inOrder.verify(visitor).leaveScalarLine(item2, line2);
        inOrder.verify(visitor).leave(item2);
        inOrder.verify(visitor).leaveSequenceItem(node, item2);
        inOrder.verify(visitor).leave(node);
    }


    @Test void shouldVisitEmptySingleQuotedScalarNode() {
        ScalarNode node = new ScalarNode().singleQuoted();

        node.guide(visitor);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(node);
        inOrder.verify(visitor).leave(node);
    }

    @Test void shouldVisitDoubleQuotedScalarNode() {
        Line line = new Line().text("foo");
        ScalarNode node = new ScalarNode().doubleQuoted().line(line);

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
        ScalarNode node = new ScalarNode().plain().line(line1).line(line2);

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
        MappingNode node = new MappingNode();

        AliasNode key1 = new AliasNode();
        Line value1line1 = new Line().text("foo");
        Line value1line2 = new Line().text("bar");
        ScalarNode value1 = new ScalarNode().plain().line(value1line1).line(value1line2);
        node.entry(key1, value1);

        Line key2line1 = new Line().text("key2");
        ScalarNode key2 = new ScalarNode().line(key2line1);
        Line value2line1 = new Line().text("value2");
        ScalarNode value2 = new ScalarNode().line(value2line1);
        node.entry(key2, value2);


        node.guide(visitor);


        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visit(node);

        Entry entry1 = new Entry().key(key1).value(value1);
        inOrder.verify(visitor).enterMappingEntry(node, entry1);
        /**/inOrder.verify(visitor).enterMappingKey(entry1, key1);
        /**//**/inOrder.verify(visitor).visit(key1);
        /**/inOrder.verify(visitor).leaveMappingKey(entry1, key1);
        /**/inOrder.verify(visitor).enterMappingValue(entry1, value1);
        /**//**/inOrder.verify(visitor).visit(value1);
        /**//**//**/inOrder.verify(visitor).enterScalarLine(value1, value1line1);
        /**//**//**//**/inOrder.verify(visitor).visit(value1line1);
        /**//**//**/inOrder.verify(visitor).leaveScalarLine(value1, value1line1);
        /**//**//**/inOrder.verify(visitor).enterScalarLine(value1, value1line2);
        /**//**//**//**/inOrder.verify(visitor).visit(value1line2);
        /**//**//**/inOrder.verify(visitor).leaveScalarLine(value1, value1line2);
        /**//**/inOrder.verify(visitor).leave(value1);
        /**/inOrder.verify(visitor).leaveMappingValue(entry1, value1);
        inOrder.verify(visitor).leaveMappingEntry(node, entry1);

        Entry entry2 = new Entry().key(key2).value(value2);
        inOrder.verify(visitor).enterMappingEntry(node, entry2);
        /**/inOrder.verify(visitor).enterMappingKey(entry2, key2);
        /**//**/inOrder.verify(visitor).visit(key2);
        /**//**//**/inOrder.verify(visitor).enterScalarLine(key2, key2line1);
        /**//**//**//**/inOrder.verify(visitor).visit(key2line1);
        /**//**//**/inOrder.verify(visitor).leaveScalarLine(key2, key2line1);
        /**//**/inOrder.verify(visitor).leave(key2);
        /**/inOrder.verify(visitor).leaveMappingKey(entry2, key2);
        /**/inOrder.verify(visitor).enterMappingValue(entry2, value2);
        /**//**/inOrder.verify(visitor).visit(value2);
        /**//**//**/inOrder.verify(visitor).enterScalarLine(value2, value2line1);
        /**//**//**//**/inOrder.verify(visitor).visit(value2line1);
        /**//**//**/inOrder.verify(visitor).leaveScalarLine(value2, value2line1);
        /**//**/inOrder.verify(visitor).leave(value2);
        /**/inOrder.verify(visitor).leaveMappingValue(entry2, value2);
        inOrder.verify(visitor).leaveMappingEntry(node, entry2);

        inOrder.verify(visitor).leave(node);
    }
}
