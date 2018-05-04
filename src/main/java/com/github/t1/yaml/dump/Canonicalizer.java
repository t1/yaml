package com.github.t1.yaml.dump;

import com.github.t1.yaml.model.Document;
import com.github.t1.yaml.model.Mapping;
import com.github.t1.yaml.model.Mapping.Entry;
import com.github.t1.yaml.model.Scalar;
import com.github.t1.yaml.model.Scalar.Line;
import com.github.t1.yaml.model.Stream;
import com.github.t1.yaml.model.Visitor;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.github.t1.yaml.model.Directive.YAML_VERSION;
import static com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public class Canonicalizer implements Visitor {
    @Override public void visit(Stream stream) {
        stream.documents().removeIf(Document::isEmpty);
    }

    @Override public void visit(Document document) {
        if (document.directives().stream().noneMatch(YAML_VERSION::matchName))
            document.directives().add(YAML_VERSION);
        document.prefixComments().clear();
        if (document.node() == null)
            document.node(new Scalar());
        document.suffixComment(null);
    }

    @Override public void visit(Scalar scalar) {
        List<Line> lines = scalar.lines();
        if (!lines.isEmpty()) {
            String singleLine = lines.stream().map(Line::text).collect(joining(" "));
            if (scalar.style() == SINGLE_QUOTED)
                singleLine = singleLine.replace("''", "'");
            lines.clear();
            lines.add(new Line().text(singleLine));
        }
        scalar.doubleQuoted();
    }

    @Override public void leave(Scalar scalar) {
        scalar.tag((scalar.isEmpty()) ? "!!null" : "!!str");
    }

    @Override public void enterMappingEntry(Mapping mapping, Entry entry) {
        entry.hasMarkedKey(true);
        // entry.hasNlAfterKey(true);
    }

    @Override public void leave(Stream stream) {
        stream.fixDocumentEndMarkers();
        if (stream.hasDocuments())
            stream.lastDocument().hasDocumentEndMarker(false);
    }
}
