package com.github.t1.yaml.dump

import com.github.t1.yaml.model.Directive.Companion.YAML_VERSION
import com.github.t1.yaml.model.Document
import com.github.t1.yaml.model.Mapping
import com.github.t1.yaml.model.Mapping.Entry
import com.github.t1.yaml.model.Scalar
import com.github.t1.yaml.model.Scalar.Line
import com.github.t1.yaml.model.Scalar.Style.DOUBLE_QUOTED
import com.github.t1.yaml.model.Scalar.Style.PLAIN
import com.github.t1.yaml.model.Scalar.Style.SINGLE_QUOTED
import com.github.t1.yaml.model.Stream
import com.github.t1.yaml.model.Visitor
import java.util.stream.Collectors.joining

class Canonicalizer : Visitor {
    override fun visit(stream: Stream) {
        stream.documents.removeIf { it.isEmpty }
    }

    override fun visit(document: Document) {
        if (document.directives.stream().noneMatch(YAML_VERSION::matchName))
            document.directives.add(YAML_VERSION)
        document.prefixComments.clear()
        if (document.node == null)
            document.node(Scalar())
        document.suffixComment = null
    }

    override fun visit(scalar: Scalar) {
        val lines = scalar.lines
        if (!lines.isEmpty()) {
            var singleLine = lines.stream().map { it.text }.collect(joining(" "))
            singleLine = escape(scalar, singleLine)
            lines.clear()
            lines.add(Line().text(singleLine))
        }
        scalar.doubleQuoted()
    }

    private fun escape(scalar: Scalar, text: String): String =
        when (scalar.style) {
            DOUBLE_QUOTED -> text
            SINGLE_QUOTED -> text.replace("''", "'")
            PLAIN -> text.replace("\"", "\\\"")
        }

    override fun leave(scalar: Scalar) {
        scalar.tag = if (scalar.isEmpty) "!!null" else "!!str"
    }

    override fun enterMappingEntry(mapping: Mapping, entry: Entry) {
        entry.hasMarkedKey = true
        // entry.setHasNlAfterKey(true);
    }

    override fun leave(stream: Stream) {
        stream.fixDocumentEndMarkers()
        if (stream.hasDocuments())
            stream.lastDocument().hasDocumentEndMarker = false
    }
}
