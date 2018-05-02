package com.github.t1.yaml.dump;

import com.github.t1.yaml.model.Alias;
import com.github.t1.yaml.model.Comment;
import com.github.t1.yaml.model.Directive;
import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Mapping;
import com.github.t1.yaml.model.Mapping.Entry;
import com.github.t1.yaml.model.Node;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.model.Sequence;
import com.github.t1.yaml.model.Sequence.Item;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.model.Visitor;
import lombok.RequiredArgsConstructor;

import static com.github.t1.yaml.tools.Tools.spaces;

@RequiredArgsConstructor public class Presenter {
    private static final char NL = '\n';

    private final StringBuilder out = new StringBuilder();
    private Visitor visitor = new Visitor() {
        boolean skipNextIndent;
        boolean skipIndent;
        int indent = 0;

        private String indent() {
            if (skipIndent)
                return "";
            if (skipNextIndent) {
                skipNextIndent = false;
                return "";
            }
            return spaces(indent * 2);
        }

        @Override public void visit(Directive directive) { out.append("%").append(directive.name()).append(" ").append(directive.parameters()).append(NL); }

        @Override public void leaveDirectives(Document document) { out.append("---").append(NL); }

        @Override public void visitPrefixComment(Comment prefixComment) { append(prefixComment).append(NL); }

        @Override public void leaveBody(Node node) { nl(); }

        @Override public void enterDocumentEnd() { out.append("..."); }

        @Override public void visitSuffixCommend(Comment suffixComment) { append(suffixComment); }

        @Override public void leaveDocumentEnd() { nl(); }


        @Override public void visit(Alias alias) { out.append(alias); }


        @Override public void enterSequenceItem(Sequence sequence, Item item) {
            switch (sequence.style()) {
                case FLOW:
                    skipIndent = true;
                    out.append((item == sequence.firstItem()) ? "[" : ", ");
                    break;
                case BLOCK:
                    out.append(indent());
                    skipNextIndent = !item.nl();
                    indent++;
                    out.append("-").append(item.nl() ? "\n" : " ");
            }
        }

        @Override public void leaveSequenceItem(Sequence sequence, Item item) {
            switch (sequence.style()) {
                case FLOW:
                    if (item == sequence.lastItem())
                        out.append("]");
                    skipIndent = false;
                    break;
                case BLOCK:
                    if (item != sequence.lastItem())
                        nl();
                    indent--;
            }
        }


        @Override public void visit(Scalar scalar) {
            if (scalar.tag() != null)
                out.append(scalar.tag()).append(' ');
            out.append(scalar.style().quote());
        }

        @Override public void visit(Line line) {
            out.append(indent()).append(spaces(line.indent()));
            out.append(line.text());
            if (line.comment() != null)
                append(line.comment());
        }

        @Override public void leaveScalarLine(Scalar node, Line line) {
            if (line != node.lastLine())
                nl();
        }

        @Override public void leave(Scalar scalar) { out.append(scalar.style().quote()); }


        @Override public void enterMappingKey(Entry entry, Node key) { out.append(entry.hasMarkedKey() ? "? " : ""); }

        @Override public void enterMappingValue(Entry entry, Node key) {
            indent++;
            skipNextIndent = !entry.hasNlAfterKey();
            out.append(":").append(entry.hasNlAfterKey() ? NL : ' ');
        }

        @Override public void leaveMappingValue(Entry entry, Node key) { indent--; }

        @Override public void leaveMappingEntry(Mapping node, Entry entry) {
            if (entry != node.lastEntry())
                nl();
        }

        private StringBuilder append(Comment comment) {
            return out.append(spaces(comment.indent())).append("# ").append(comment.text());
        }

        private void nl() { out.append(NL); }
    };

    public String present(Stream stream) {
        stream.guide(visitor);
        return out.toString();
    }
}
