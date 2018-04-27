package com.github.t1.yaml.model;

import com.github.t1.yaml.model.MappingNode.Entry;
import com.github.t1.yaml.model.ScalarNode.Line;
import com.github.t1.yaml.model.SequenceNode.Item;
import lombok.RequiredArgsConstructor;

import static com.github.t1.yaml.dump.Tools.spaces;

@RequiredArgsConstructor class ToStringVisitor {
    private static final char NL = '\n';

    private final Document document;
    private final StringBuilder out = new StringBuilder();

    @Override public String toString() {
        directives();
        prefixComments();
        node();
        documentEnd();
        return out.toString();
    }

    private void nl() { out.append(NL); }

    private void prefixComments() {
        if (!document.prefixComments().isEmpty())
            document.prefixComments().forEach(comment -> append(comment).append(NL));
    }

    private void directives() {
        if (!document.directives().isEmpty() || document.hasDirectivesEndMarker()) {
            for (Directive directive : document.directives())
                out.append("%").append(directive.name()).append(" ").append(directive.parameters()).append(NL);
            out.append("---").append(NL);
        }
    }

    private void documentEnd() {
        if (document.hasDocumentEndMarker()) {
            out.append("...");
            if (document.suffixComment() != null)
                append(document.suffixComment());
            nl();
        }
    }

    private StringBuilder append(Comment comment) {
        return out.append(spaces(comment.indent())).append("# ").append(comment.text());
    }

    private void node() {
        if (document.node() != null) {
            document.node().guide(new Node.Visitor() {
                boolean skipNextIndent;
                int indent = 0;

                private String indent() {
                    if (skipNextIndent) {
                        skipNextIndent = false;
                        return "";
                    }
                    return spaces(indent * 2);
                }


                @Override public void visit(AliasNode alias) { out.append(alias); }


                @Override public void visit(SequenceNode sequence) {}

                @Override public void enterSequenceItem(SequenceNode sequence, Item item) {
                    out.append(indent());
                    skipNextIndent = !item.nl();
                    indent++;
                    out.append("-").append(item.nl() ? "\n" : " ");
                }

                @Override public void leaveSequenceItem(SequenceNode sequence, Item item) {
                    if (item != sequence.lastItem())
                        nl();
                    indent--;
                }

                @Override public void leave(SequenceNode sequence) {}


                @Override public void visit(ScalarNode scalar) {
                    if (scalar.tag() != null)
                        out.append(scalar.tag()).append(' ');
                    out.append(scalar.style().quote);
                }

                @Override public void enterScalarLine(ScalarNode node, Line line) {}

                @Override public void visit(Line line) {
                    out.append(indent()).append(spaces(line.indent));
                    out.append(line.text);
                    if (line.comment != null)
                        append(line.comment);
                }

                @Override public void leaveScalarLine(ScalarNode node, Line line) {
                    if (line != node.lastLine())
                        nl();
                }

                @Override public void leave(ScalarNode scalar) { out.append(scalar.style().quote); }


                @Override public void visit(MappingNode mapping) {}

                @Override public void enterMappingEntry(MappingNode node, Entry entry) {}

                @Override public void enterMappingKey(Entry entry, Node key) { out.append(entry.hasMarkedKey() ? "? " : ""); }

                @Override public void leaveMappingKey(Entry entry, Node key) {}

                @Override public void enterMappingValue(Entry entry, Node key) { out.append(":").append(entry.hasNlAfterKey() ? NL : ' '); }

                @Override public void leaveMappingValue(Entry entry, Node key) {}

                @Override public void leaveMappingEntry(MappingNode node, Entry entry) {
                    if (entry != node.lastEntry())
                        nl();
                }

                @Override public void leave(MappingNode mapping) {}
            });
            nl();
        }
    }
}
